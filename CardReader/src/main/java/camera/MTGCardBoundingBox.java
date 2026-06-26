package camera;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConfigLength;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import com.github.sarxos.webcam.Webcam;
import georegression.struct.point.Point2D_I32;
import ocr.OCRImage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class MTGCardBoundingBox extends JPanel {
    private static final double CARD_ASPECT_RATIO = 2.5/3.5;
    private static final double ASPECT_RATIO_TOLERANCE = 0.15;
    private static final double MIN_AREA_FRACTION = 0.05;

    private BufferedImage displayImage;
    private BufferedImage ocrImage;
    private Rectangle ocrBox;

    private OCRImage ocrValue;

    private final SmoothBox smoother = new SmoothBox();

    public BufferedImage detectAndDraw(BufferedImage input){
        int width = input.getWidth();
        int height = input.getHeight();
        double frameArea = width * height;

        // --- Step 1: Convert to BoofCV GrayF32 ---
        GrayF32 gray = new GrayF32(width, height);
        ConvertBufferedImage.convertFrom(input, gray, true);

        // --- Step 2: Gaussian blur to reduce noise ---
        GrayF32 blurred = new GrayF32(width, height);
        GBlurImageOps.gaussian(gray, blurred, -1, 2, null); // sigma=2

        // --- Step 3: Adaptive threshold → binary image ---
        // Adaptive thresholding handles uneven lighting (common with cards on tables)
        GrayU8 binary = new GrayU8(width, height);
        GThresholdImageOps.localMean(blurred, binary, ConfigLength.fixed(40), 1.0, true, null, null, null);

        // --- Step 4: Morphological cleanup ---
        GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
        filtered = BinaryImageOps.dilate8(filtered, 1, null);

        // --- Step 5: Find external contours ---
        GrayS32 labeled = new GrayS32(width, height);
        List<Contour> contours =
                BinaryImageOps.contour(filtered, ConnectRule.EIGHT, labeled);

        // --- Step 6: Draw result ---
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        output.createGraphics().drawImage(input, 0, 0, null);
        Graphics2D g2d = output.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle bestBox = null;
        double bestScore = Double.MAX_VALUE;

        for (boofcv.alg.filter.binary.Contour contour : contours) {
            List<Point2D_I32> points = contour.external;

            // Compute axis-aligned bounding box from contour points
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

            for (Point2D_I32 p : points) {
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
                maxX = Math.max(maxX, p.x);
                maxY = Math.max(maxY, p.y);
            }

            int boxWidth  = maxX - minX;
            int boxHeight = maxY - minY;
            double area   = boxWidth * boxHeight;

            // --- Step 7: Filter by size ---
            if (area < frameArea * MIN_AREA_FRACTION) continue;
            if (boxWidth < 50 || boxHeight < 50) continue;

            // --- Step 8: Filter by aspect ratio ---
            double aspectRatio = (double) boxWidth / boxHeight;
            // Check both portrait and landscape orientations
            double portraitDiff  = Math.abs(aspectRatio - CARD_ASPECT_RATIO);
            double landscapeDiff = Math.abs(aspectRatio - (1.0 / CARD_ASPECT_RATIO));
            double aspectDiff    = Math.min(portraitDiff, landscapeDiff);

            if (aspectDiff > ASPECT_RATIO_TOLERANCE) continue;

            // Score: prefer largest card-like region closest to center
            double cx = (minX + maxX) / 2.0;
            double cy = (minY + maxY) / 2.0;
            double distToCenter = Math.hypot(cx - width / 2.0, cy - height / 2.0);
            double score = distToCenter / area; // lower = better

            if (score < bestScore) {
                bestScore = bestBox == null ? score : bestScore;
                bestBox = new Rectangle(minX, minY, boxWidth, boxHeight);
                bestScore = score;
            }

        }

        smoother.smoothBox(bestBox);
        Rectangle displayBox = smoother.getBox();
        drawBox(displayBox, g2d);

        if(smoother.isSmoothed()){
            if(ocrBox == null){
                int ocrX = displayBox.x;
                int ocrY = (int)(0.9 * displayBox.y);
                int ocrWidth = (int)(0.5 * displayBox.width);
                int ocrHeight = (int)(0.1 * displayBox.height);

                ocrBox = new Rectangle(ocrX, ocrY, ocrWidth, ocrHeight);
                ocrImage = input.getSubimage(ocrX, ocrY, ocrWidth, ocrHeight);

                drawBox(ocrBox, g2d);

                ocrValue = new OCRImage(ocrImage);
            }
            else{
                drawBox(ocrBox, g2d);
            }
        }

        return null;
    }

    public boolean isSmoothed(){
        return smoother.isSmoothed();
    }

    private void drawBox(Rectangle box, Graphics2D g2d){
        if (box != null) {
            // Outer glow effect
            g2d.setColor(new Color(0, 255, 0, 80));
            g2d.setStroke(new BasicStroke(6));
            g2d.drawRect(box.x - 3, box.y - 3,
                    box.width + 6, box.height + 6);

            // Main bounding box
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(box.x, box.y, box.width, box.height);

            // Label — show "locked" when smoother has rejected outliers
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.setColor(Color.GREEN);
            g2d.drawString("MTG Card",
                    box.x, box.y - 8);

            // Corner markers
            int m = 12;
            g2d.setStroke(new BasicStroke(3));
            drawCornerMarker(g2d, box.x, box.y, m, m);
            drawCornerMarker(g2d, box.x + box.width, box.y, -m, m);
            drawCornerMarker(g2d, box.x, box.y + box.height, m, -m);
            drawCornerMarker(g2d, box.x + box.width,
                    box.y + box.height, -m, -m);
        } else {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("No card detected", 10, 20);
        }
    }

    private void drawCornerMarker(Graphics2D g, int x, int y, int dx, int dy) {
        g.drawLine(x, y, x + dx, y);
        g.drawLine(x, y, x, y + dy);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (displayImage != null) {
            g.drawImage(displayImage, 0, 0, getWidth(), getHeight(), null);
        }
    }
}
