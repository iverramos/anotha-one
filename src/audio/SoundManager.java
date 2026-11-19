package audio;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Procedural retro sound synthesizer and sound manager using javax.sound.sampled.
 * Generates and caches authentic 8/16-bit arcade sound effects in memory
 * with zero external dependencies.
 */
public final class SoundManager {
    public enum Sound {
        SLASH,
        BOW_SHOOT,
        GUN_SHOOT,
        MAGIC_CAST,
        DASH,
        HIT,
        PLAYER_HURT,
        JUMP,
        COIN,
        CHEST_OPEN,
        BOSS_ROAR,
        ROOM_CLEAR,
        FOOD_EAT,
        EXPLOSION
    }

    private static final int SAMPLE_RATE = 22050;
    private static final Map<Sound, byte[]> soundData = new HashMap<>();
    private static boolean soundEnabled = true;

    static {
        try {
            generateAllSounds();
        } catch (Throwable t) {
            soundEnabled = false;
        }
    }

    private SoundManager() {}

    public static void play(Sound s) {
        if (!soundEnabled) return;
        byte[] data = soundData.get(s);
        if (data == null) return;

        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, false, false);
                Clip clip = AudioSystem.getClip();
                clip.open(format, data, 0, data.length);
                clip.start();
            } catch (Throwable ignored) {
                // Ignore audio hardware unavailability
            }
        }).start();
    }

    private static void generateAllSounds() {
        soundData.put(Sound.SLASH, synthNoiseSweep(0.12, 800, 200));
        soundData.put(Sound.BOW_SHOOT, synthToneSweep(0.14, 450, 150, true));
        soundData.put(Sound.GUN_SHOOT, synthGunshot(0.16));
        soundData.put(Sound.MAGIC_CAST, synthMagic(0.25));
        soundData.put(Sound.DASH, synthDash(0.18));
        soundData.put(Sound.HIT, synthHit(0.10));
        soundData.put(Sound.PLAYER_HURT, synthPlayerHurt(0.20));
        soundData.put(Sound.JUMP, synthToneSweep(0.12, 220, 520, false));
        soundData.put(Sound.COIN, synthChime(new double[]{987.77, 1318.51}, 0.15));
        soundData.put(Sound.CHEST_OPEN, synthChime(new double[]{523.25, 659.25, 783.99, 1046.50}, 0.35));
        soundData.put(Sound.BOSS_ROAR, synthBossRoar(0.6));
        soundData.put(Sound.ROOM_CLEAR, synthChime(new double[]{440, 554, 659, 880}, 0.45));
        soundData.put(Sound.FOOD_EAT, synthFoodEat(0.25));
        soundData.put(Sound.EXPLOSION, synthExplosion(0.35));
    }

    private static byte[] synthToneSweep(double duration, double fStart, double fEnd, boolean triangle) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        double phase = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double f = fStart + (fEnd - fStart) * t;
            phase += 2.0 * Math.PI * f / SAMPLE_RATE;
            double env = 1.0 - t;
            double wave;
            if (triangle) {
                wave = (2.0 / Math.PI) * Math.asin(Math.sin(phase));
            } else {
                wave = Math.sin(phase);
            }
            int val = (int) (128 + 100 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    private static byte[] synthNoiseSweep(double duration, double f0, double f1) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        java.util.Random r = new java.util.Random(42);
        double filter = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = (1.0 - t) * Math.sin(t * Math.PI);
            double cutoff = (f0 + (f1 - f0) * t) / SAMPLE_RATE;
            filter += cutoff * (r.nextDouble() * 2 - 1 - filter);
            int val = (int) (128 + 110 * filter * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    private static byte[] synthGunshot(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        java.util.Random r = new java.util.Random(99);
        double low = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.exp(-t * 18);
            double noise = r.nextDouble() * 2 - 1;
            low += 0.15 * (noise - low);
            double wave = noise * 0.7 + low * 0.8 + Math.sin(2 * Math.PI * 90 * t) * 0.6;
            int val = (int) (128 + 120 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    private static byte[] synthMagic(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.sin(t * Math.PI) * (1.0 - t * 0.4);
            double f1 = 580 + 300 * Math.sin(t * 15);
            double f2 = 880 + 200 * t;
            double wave = 0.5 * Math.sin(2 * Math.PI * f1 * t) + 0.5 * Math.sin(2 * Math.PI * f2 * t);
            int val = (int) (128 + 100 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    private static byte[] synthDash(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        java.util.Random r = new java.util.Random(77);
        double filter = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.sin(t * Math.PI);
            double noise = r.nextDouble() * 2 - 1;
            filter += 0.25 * (noise - filter);
            int val = (int) (128 + 100 * filter * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    private static byte[] synthHit(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        java.util.Random r = new java.util.Random(13);
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.exp(-t * 30);
            double noise = r.nextDouble() * 2 - 1;
            double sub = Math.sin(2 * Math.PI * 110 * (1.0 - t * 0.5) * t);
            double wave = noise * 0.5 + sub * 0.7;
            int val = (int) (128 + 120 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    private static byte[] synthPlayerHurt(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.exp(-t * 12);
            double f = 160 - 60 * t;
            double wave = Math.sin(2 * Math.PI * f * t) + ((i % 8 < 4) ? 0.3 : -0.3);
            int val = (int) (128 + 110 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    private static byte[] synthChime(double[] freqs, double totalDuration) {
        int samples = (int) (totalDuration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        int noteSamples = samples / freqs.length;

        for (int n = 0; n < freqs.length; n++) {
            double f = freqs[n];
            int start = n * noteSamples;
            int end = (n == freqs.length - 1) ? samples : (n + 1) * noteSamples;
            for (int i = start; i < end; i++) {
                double t = (double) (i - start) / (samples - start);
                double env = Math.exp(-t * 5);
                double wave = Math.sin(2 * Math.PI * f * (double) i / SAMPLE_RATE);
                // Soft bell harmonic
                wave += 0.3 * Math.sin(4 * Math.PI * f * (double) i / SAMPLE_RATE);
                int val = (int) (128 + 90 * wave * env);
                b[i] = (byte) Math.max(0, Math.min(255, val));
            }
        }
        return b;
    }

    private static byte[] synthBossRoar(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        java.util.Random r = new java.util.Random(666);
        double filter = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.sin(t * Math.PI);
            double saw = ((i % 240) / 120.0) - 1.0;
            double noise = r.nextDouble() * 2 - 1;
            filter += 0.08 * ((saw * 0.7 + noise * 0.6) - filter);
            int val = (int) (128 + 120 * filter * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    private static byte[] synthFoodEat(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        java.util.Random r = new java.util.Random(123);
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.exp(-t * 10);
            double crunch = (r.nextDouble() * 2 - 1) * ((i / 400) % 2 == 0 ? 1 : 0);
            double chime = Math.sin(2 * Math.PI * 659.25 * t);
            double wave = crunch * 0.6 + chime * 0.5;
            int val = (int) (128 + 95 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    private static byte[] synthExplosion(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        java.util.Random r = new java.util.Random(888);
        double low = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.exp(-t * 8);
            double noise = r.nextDouble() * 2 - 1;
            low += 0.05 * (noise - low);
            double wave = low * 1.2 + noise * 0.3 + Math.sin(2 * Math.PI * 55 * t) * 0.5;
            int val = (int) (128 + 120 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }
}
