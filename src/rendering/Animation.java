package rendering;

import java.awt.image.BufferedImage;

/** A timed sequence of frames. Each owner keeps its own Animation instance (it holds its own clock). */
public final class Animation {
    private final BufferedImage[] frames;
    private final double frameTime;
    private final boolean loop;
    private double t;

    public Animation(BufferedImage[] frames, double frameTime, boolean loop) {
        this.frames = frames;
        this.frameTime = frameTime;
        this.loop = loop;
    }

    public void update(double dt) { t += dt; }
    public void reset() { t = 0; }

    public int frameIndex() {
        int i = (int) (t / frameTime);
        return loop ? i % frames.length : Math.min(i, frames.length - 1);
    }

    public BufferedImage frame() { return frames[frameIndex()]; }
    public boolean isFinished() { return !loop && t >= frameTime * frames.length; }
    public int length() { return frames.length; }
}
