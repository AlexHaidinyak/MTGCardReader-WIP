package camera;

import java.awt.*;

public class SmoothBox {
    private double smoothX, smoothY, smoothW, smoothH;
    private boolean initialized = false;
    private boolean isSmoothed = false;
    private int missedFrames = 0;
    private int smoothFrame = 0;

    private static final double ALPHA          = 0.1; // smoothing rate
    private static final double MAX_JUMP_RATIO = 0.20; // reject if box moves >30% of its size
    private static final int    MAX_MISS       = 10;   // frames before box is discarded

    public void smoothBox(Rectangle detected){
        if(detected == null){
            missedFrames++;
            if(missedFrames >= MAX_MISS){
                reset();
            }
            return;
        }

        if(!initialized){
            smoothX = detected.x;
            smoothY = detected.y;
            smoothW = detected.width;
            smoothH = detected.height;
            initialized = true;
            return;
        }

        double jumpX = Math.abs(smoothX - detected.x) / smoothW;
        double jumpY = Math.abs(smoothY - detected.y) / smoothH;
        if(jumpX > MAX_JUMP_RATIO || jumpY > MAX_JUMP_RATIO){
            return;
        }

        smoothX = ALPHA * detected.x + (1 - ALPHA) * smoothX;
        smoothY = ALPHA * detected.y + (1 - ALPHA) * smoothY;
        smoothW = ALPHA * detected.width + (1 - ALPHA) * smoothW;
        smoothH = ALPHA * detected.height + (1 - ALPHA) * smoothH;
        smoothFrame++;

        if(smoothFrame >= 15){
            isSmoothed = true;
        }
    }

    public void reset(){
        initialized = false;
        missedFrames = 0;
        isSmoothed = false;
        smoothFrame = 0;
    }

    public boolean isSmoothed(){
        return isSmoothed;
    }

    public Rectangle getBox(){
        return new Rectangle(
                (int) Math.round(smoothX), (int) Math.round(smoothY),
                (int) Math.round(smoothW), (int) Math.round(smoothH)
        );
    }
    public double[] getExactBox() {

        return new double[]{ smoothX, smoothY, smoothW, smoothH };
    }
}
