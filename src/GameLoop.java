import rendering.Renderer;
import utilities.Constants;

import java.awt.Graphics2D;
import java.util.concurrent.locks.LockSupport;

/**
 * Fixed-timestep loop (60 UPS) on its own thread. Logic never touches Swing components;
 * it renders into the Renderer's back buffer and asks the EDT to repaint.
 */
public final class GameLoop implements Runnable {
    private final Game game;
    private final Renderer renderer;
    private final Runnable requestRepaint;
    private volatile boolean running;
    private Thread thread;

    public GameLoop(Game game, Renderer renderer, Runnable requestRepaint) {
        this.game = game;
        this.renderer = renderer;
        this.requestRepaint = requestRepaint;
    }

    public void start() {
        running = true;
        thread = new Thread(this, "game-loop");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() { running = false; }

    @Override public void run() {
        final long step = 1_000_000_000L / Constants.UPS;
        long prev = System.nanoTime();
        long acc = 0;
        long next = prev + step;
        long fpsClock = prev;
        int frames = 0;

        while (running) {
            long now = System.nanoTime();
            acc += now - prev;
            prev = now;
            if (acc > step * 5) acc = step * 5; // avoid spiral of death after a hitch
            while (acc >= step) {
                game.update(Constants.DT);
                acc -= step;
            }

            Graphics2D g = renderer.begin();
            game.render(g);
            renderer.end();
            requestRepaint.run();
            frames++;

            if (now - fpsClock >= 1_000_000_000L) {
                game.setFps(frames);
                frames = 0;
                fpsClock = now;
            }

            // Sleep most of the remaining frame, then spin the last stretch for precise timing.
            long rem = next - System.nanoTime();
            while (rem > 2_000_000L) {
                LockSupport.parkNanos(rem - 1_500_000L);
                rem = next - System.nanoTime();
            }
            while (System.nanoTime() < next) Thread.onSpinWait();
            next += step;
            if (next < System.nanoTime() - step * 5) next = System.nanoTime() + step;
        }
    }
}
