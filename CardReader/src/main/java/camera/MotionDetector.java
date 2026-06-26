package camera;

import com.github.sarxos.webcam.Webcam;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MotionDetector {

    private final Rectangle bounds;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private BufferedImage prevImage = null;
    private BufferedImage currentImage = null;

    private final double AREA_THRESHOLD = 0.25;
    private final int PIXEL_THRESHOLD = 15;

    private boolean motionDetected = false;

    public MotionDetector(BufferedImage image, Point startPoint, int width, int height) {
        bounds = new Rectangle(startPoint.x, startPoint.y, width, height);

        executor.scheduleAtFixedRate(() -> checkForMotion(image), 0, 350, TimeUnit.MILLISECONDS);
    }

    public MotionDetector(Point startPoint, int width, int height) {
        bounds = new Rectangle(startPoint.x, startPoint.y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    public boolean motionDetected(){
        return motionDetected;
    }

    public void pauseThread() throws InterruptedException {
        executor.wait();
    }
    public void resumeThread(){
        executor.notifyAll();
    }

    public void checkForMotion(BufferedImage image){

        //currentImage = image.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
        currentImage = image;
//        System.out.println("Checking for motion...");

        if(prevImage != null){
            int currentPixel, pastPixel;
            int changedPixels = 0;
            int imageArea = prevImage.getWidth() * prevImage.getHeight();
            double areaChanged;

//            currentImage = webcam.getImage().getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);

            for(int row = 0; row < currentImage.getHeight(); row++){
                for(int col = 0; col < currentImage.getWidth(); col++){
                    currentPixel = currentImage.getRGB(col, row);
                    pastPixel = prevImage.getRGB(col, row);

                    int pastGray = (((pastPixel >> 16) & 0xFF) + ((pastPixel >> 8) & 0xFF) + (pastPixel & 0xFF)) / 3;
                    int currentGray = (((currentPixel >> 16) & 0xFF) + ((currentPixel >> 8) & 0xFF) + (currentPixel & 0xFF)) / 3;

                    if(Math.abs(pastGray - currentGray) > PIXEL_THRESHOLD){
                        changedPixels++;
                    }//end of if statement for checking if the individual pixel's changed enough
                }//end of for loop for checking the pixels in each row
            }//end of for loop for going through each row of the sub-image

            areaChanged = (double)changedPixels / imageArea;

//            System.out.println(changedPixels + " pixels changed.");

            if(areaChanged > AREA_THRESHOLD){
                motionDetected = true;
                System.out.println("Motion detected");
            }
        }//end of if statement for checking between the current and past sub-images for changes


        prevImage = copyImage(currentImage);
    }

    private BufferedImage copyImage(BufferedImage image){
        BufferedImage copiedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        copiedImage.createGraphics().drawImage(image, 0, 0, null);
        return copiedImage;
    }
}
