package ocr;

import boofcv.alg.enhance.EnhanceImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OCRImage {
    private BufferedImage grayscaleImage;

    private Path tempFile;

    private final int PIXEL_TOLERANCE = 15;
    private final double ROW_TOLERANCE = 0.20;

    public OCRImage(BufferedImage image) {
       grayscaleImage(image);
       adjustImage();

       try{
           tempFile = Files.createTempFile(tempFile, "PNG", "TempFile.png");
       }
       catch (IOException e){
           e.printStackTrace();
       }
    }

    public Path getTempFile() {
        return tempFile;
    }

    private int adjustTopFrame(){
        int wrongPixelCount = 0;
        int adjustTop = 0;
        double areaChanged;

        for(int col = 0; col < (grayscaleImage.getHeight() / 2); col++) {
            for(int row = 0; row < grayscaleImage.getWidth(); row++) {
                int rgbValue = grayscaleImage.getRGB(row, col);
                int grayValue = (((rgbValue >> 16) & 0xFF) + ((rgbValue >> 8) & 0xFF) + (rgbValue & 0xFF)) / 3;

                if(grayValue > PIXEL_TOLERANCE && grayValue < (255 - PIXEL_TOLERANCE)) {
                    wrongPixelCount++;
                }

                if(row == grayscaleImage.getWidth() - 1) {
                    areaChanged = (double) wrongPixelCount / (double) grayscaleImage.getWidth();

                    if(areaChanged > ROW_TOLERANCE) {
                        adjustTop++;
                    }
                    else{
                        return adjustTop;
                    }
                }
            }
            wrongPixelCount = 0;
        }
        return adjustTop;
    }
    private int adjustBottomFrame(){
        int wrongPixelCount = 0;
        int adjustBottom = 0;
        double areaChanged;

        for(int col = grayscaleImage.getHeight() - 1; col > (grayscaleImage.getHeight() /2); col--) {
            for(int row = 0; row < grayscaleImage.getWidth(); row++) {
                int rgbValue = grayscaleImage.getRGB(row, col);
                int grayValue = (((rgbValue >> 16) & 0xFF) + ((rgbValue >> 8) & 0xFF) + (rgbValue & 0xFF)) / 3;

                if(grayValue > PIXEL_TOLERANCE && grayValue < (255 - PIXEL_TOLERANCE)) {
                    wrongPixelCount++;
                }

                if(row == grayscaleImage.getWidth() - 1) {
                    areaChanged = (double) wrongPixelCount/ (double) grayscaleImage.getWidth();
                    if(areaChanged > ROW_TOLERANCE) {
                        adjustBottom++;
                    }
                    else{
                        return adjustBottom;
                    }
                }
            }
            wrongPixelCount = 0;
        }
        return adjustBottom;
    }
    private int adjustLeftFrame(){
        int wrongPixelCount = 0;
        int adjustLeft = 0;
        double areaChanged;

        for(int row = 0; row < (grayscaleImage.getWidth() / 2); row++){
            for(int col = 0; col < grayscaleImage.getHeight(); col++){
                int rgbValue = grayscaleImage.getRGB(row, col);
                int grayValue = (((rgbValue >> 16) & 0xFF) + ((rgbValue >> 8) & 0xFF) + (rgbValue & 0xFF)) / 3;

                if(grayValue > PIXEL_TOLERANCE && grayValue < (255 - PIXEL_TOLERANCE)) {
                    wrongPixelCount++;
                }

                if(col == grayscaleImage.getHeight() - 1){
                    areaChanged = (double) wrongPixelCount/ (double) grayscaleImage.getHeight();
                    if(areaChanged > ROW_TOLERANCE) {
                        adjustLeft++;
                    }
                    else{
                        return adjustLeft;
                    }
                }
            }
            wrongPixelCount = 0;
        }
        return adjustLeft;
    }

    private void adjustImage(){
        int adjustTop = adjustTopFrame();
        int adjustBottom = adjustBottomFrame();
        int adjustLeft = adjustLeftFrame();

        int newWidth = grayscaleImage.getWidth() - adjustLeft;
        int newHeight = grayscaleImage.getHeight() - adjustTop - adjustBottom;

        grayscaleImage = grayscaleImage.getSubimage(adjustLeft, adjustTop, newWidth, newHeight);
    }

    private void grayscaleImage(BufferedImage image) {

        // 1. Grayscale F32
        GrayF32 gray = new GrayF32(image.getWidth(), image.getHeight());
        ConvertBufferedImage.convertFrom(image, gray, true);


        // 2. Gaussian denoise
        GrayF32 denoised = gray.createSameShape();
        GBlurImageOps.gaussian(gray, denoised, -1, 2, null);

        // 3. Safe F32 → U8
        BufferedImage tempBuf = new BufferedImage(
                denoised.width, denoised.height, BufferedImage.TYPE_BYTE_GRAY
        );
        ConvertBufferedImage.convertTo(denoised, tempBuf, true);
        GrayU8 gray8 = new GrayU8(denoised.width, denoised.height);
        ConvertBufferedImage.convertFrom(tempBuf, gray8, true);

        // 4. Gamma curve — start at 2.5, tune from there
        GrayU8 gamma = gray8.createSameShape();
        for (int y = 0; y < gray8.height; y++) {
            for (int x = 0; x < gray8.width; x++) {
                float normalized = (gray8.get(x, y) & 0xFF) / 255.0f;
                float curved = (float) Math.pow(normalized, 1.5);
                gamma.set(x, y, (int)(curved * 255));
            }
        }

        // 5. Sharpen
        GrayU8 sharpened = gamma.createSameShape();
        EnhanceImageOps.sharpen8(gamma, sharpened);

        // 6. Second denoise pass in F32
        GrayF32 sharpF32 = new GrayF32(sharpened.width, sharpened.height);
        ConvertBufferedImage.convertFrom(
                ConvertBufferedImage.convertTo(sharpened, null, true), sharpF32, true
        );
        GrayF32 smoothed = sharpF32.createSameShape();
        GBlurImageOps.gaussian(sharpF32, smoothed, -1, 1, null);

        // 7. Back to U8
        BufferedImage smoothBuf = new BufferedImage(
                smoothed.width, smoothed.height, BufferedImage.TYPE_BYTE_GRAY
        );
        ConvertBufferedImage.convertTo(smoothed, smoothBuf, true);
        GrayU8 final8 = new GrayU8(smoothed.width, smoothed.height);
        ConvertBufferedImage.convertFrom(smoothBuf, final8, true);

        // 8. Scale 2x — save AFTER drawing
        BufferedImage ocrReady = new BufferedImage(
                final8.width * 2, final8.height * 2, BufferedImage.TYPE_BYTE_GRAY
        );
        Graphics2D g = ocrReady.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(ConvertBufferedImage.convertTo(final8, null, true),
                0, 0, ocrReady.getWidth(), ocrReady.getHeight(), null);
        g.dispose();

        // 9. Final gamma pass on the scaled image to clean up interpolation haze
        BufferedImage cleaned = new BufferedImage(
                ocrReady.getWidth(), ocrReady.getHeight(), BufferedImage.TYPE_BYTE_GRAY
        );
        WritableRaster srcRaster = ocrReady.getRaster();
        WritableRaster dstRaster = cleaned.getRaster();
        int[] pixel = new int[1];
        for (int y = 0; y < ocrReady.getHeight(); y++) {
            for (int x = 0; x < ocrReady.getWidth(); x++) {
                srcRaster.getPixel(x, y, pixel);
                float normalized = pixel[0] / 255.0f;
                float curved = (float) Math.pow(normalized, 1.5); // same gamma as before
                pixel[0] = (int)(curved * 255);
                dstRaster.setPixel(x, y, pixel);
            }
        }

        grayscaleImage = ocrReady;
    }
}
