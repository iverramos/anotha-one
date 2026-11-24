package audio;

import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Procedural retro sound synthesizer and sound manager using javax.sound.sampled.
 * Generates and caches authentic 8/16-bit arcade sound effects modeled after Bfxr parameters
 * (square/sawtooth waves, frequency slides, bitcrushing/noise bursts, and pitch jumps)
 * with zero external dependencies. Also provides a looping retro chiptune BGM generator.
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
        DOUBLE_JUMP,
        PURCHASE,
        UI_CLICK,
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

    // Background Music
    private static Clip bgmClip;
    private static byte[] bgmData;
    private static boolean bgmInitialized = false;

    static {
        try {
            generateAllSounds();
            generateBgmTrack();
        } catch (Throwable t) {
            soundEnabled = false;
        }
    }

    private SoundManager() {}

    public static void init() {
        startBgm();
    }

    public static void play(Sound s) {
        if (!soundEnabled) return;
        double vol = AudioSettings.getEffectiveSfxVolume();
        if (vol <= 0.001) return;

        byte[] raw = soundData.get(s);
        if (raw == null) return;

        // Apply volume scaling to 8-bit unsigned PCM samples
        byte[] scaled = new byte[raw.length];
        for (int i = 0; i < raw.length; i++) {
            int original = (raw[i] & 0xFF) - 128;
            int scaledVal = (int) Math.round(original * vol);
            scaled[i] = (byte) Math.max(0, Math.min(255, 128 + scaledVal));
        }

        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, false, false);
                Clip clip = AudioSystem.getClip();
                clip.open(format, scaled, 0, scaled.length);
                clip.start();
            } catch (Throwable ignored) {
                // Ignore audio hardware unavailability
            }
        }).start();
    }

    public static synchronized void startBgm() {
        if (!soundEnabled || bgmData == null) return;
        if (bgmClip != null && bgmClip.isRunning()) {
            applyAudioSettings();
            return;
        }

        try {
            if (bgmClip == null) {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, false, false);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(format, bgmData, 0, bgmData.length);
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            applyAudioSettings();
            if (AudioSettings.getEffectiveBgmVolume() > 0.001) {
                bgmClip.start();
            }
            bgmInitialized = true;
        } catch (Throwable ignored) {
            // Audio hardware unavailable
        }
    }

    public static synchronized void stopBgm() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    public static synchronized void applyAudioSettings() {
        if (bgmClip == null) return;
        try {
            double vol = AudioSettings.getEffectiveBgmVolume();
            if (vol <= 0.001) {
                if (bgmClip.isRunning()) bgmClip.stop();
                return;
            }

            if (!bgmClip.isRunning()) {
                bgmClip.start();
            }

            if (bgmClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gain.getMinimum();
                float max = gain.getMaximum();
                float dB = (float) (Math.log10(Math.max(0.0001, vol)) * 20.0);
                gain.setValue(Math.max(min, Math.min(max, dB)));
            }
        } catch (Throwable ignored) {}
    }

    private static void generateAllSounds() {
        // Bfxr: Jump - rising square wave sweep with fast attack
        soundData.put(Sound.JUMP, synthBfxrJump(0.12, 160, 440));

        // Bfxr: Double Jump - two-step rising pitch jump (square + sub harmonic)
        soundData.put(Sound.DOUBLE_JUMP, synthBfxrDoubleJump(0.15, 280, 580));

        // Bfxr: Dash - low-pass filtered noise burst + rapid downward sweep whoosh
        soundData.put(Sound.DASH, synthBfxrDash(0.18));

        // Bfxr: Melee Slash - filtered noise sweep with crisp metallic bite
        soundData.put(Sound.SLASH, synthBfxrSlash(0.12, 900, 240));

        // Bfxr: Bow Shoot - twangy triangle wave sweep down
        soundData.put(Sound.BOW_SHOOT, synthToneSweep(0.14, 460, 160, true));

        // Bfxr: Gunshot - punchy explosive crack with low rumble
        soundData.put(Sound.GUN_SHOOT, synthGunshot(0.16));

        // Bfxr: Magic Cast - shimmering dual-frequency sine chime
        soundData.put(Sound.MAGIC_CAST, synthMagic(0.24));

        // Bfxr: Hit - low sub-thump + sharp impact noise
        soundData.put(Sound.HIT, synthBfxrHit(0.11));

        // Bfxr: Player Hurt - crunchy distorted square wave drop with bitcrush
        soundData.put(Sound.PLAYER_HURT, synthBfxrPlayerHurt(0.22));

        // Bfxr: Purchase Item - happy ascending 3-note arpeggio chime
        soundData.put(Sound.PURCHASE, synthBfxrPurchase());

        // Bfxr: UI Click - crisp high square blip
        soundData.put(Sound.UI_CLICK, synthBfxrUiClick(0.04, 880));

        // Bfxr: Coin Pickup - classic rapid 2-tone pitch jump
        soundData.put(Sound.COIN, synthChime(new double[]{987.77, 1318.51}, 0.14));

        // Bfxr: Chest Open - major chord arpeggio fanfare
        soundData.put(Sound.CHEST_OPEN, synthChime(new double[]{523.25, 659.25, 783.99, 1046.50}, 0.35));

        // Bfxr: Boss Roar - deep growling sawtooth wave through low-pass filter
        soundData.put(Sound.BOSS_ROAR, synthBossRoar(0.65));

        // Bfxr: Room Clear - triumphant 4-note victory chime
        soundData.put(Sound.ROOM_CLEAR, synthChime(new double[]{440, 554, 659, 880}, 0.45));

        // Bfxr: Food Eat - retro crunch + bell tone
        soundData.put(Sound.FOOD_EAT, synthFoodEat(0.25));

        // Bfxr: Explosion - heavy low-end rumble with noise decay
        soundData.put(Sound.EXPLOSION, synthExplosion(0.38));
    }

    // =========================================================================
    // BFXR-Style Synthesizers
    // =========================================================================

    /** Bfxr Jump: Square wave with rapid upward frequency slide and exponential decay */
    private static byte[] synthBfxrJump(double duration, double fStart, double fEnd) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        double phase = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double f = fStart + (fEnd - fStart) * Math.pow(t, 0.7);
            phase += 2.0 * Math.PI * f / SAMPLE_RATE;
            double env = (1.0 - t) * (t < 0.05 ? t / 0.05 : 1.0);
            // Square wave with 50% duty cycle
            double wave = Math.sin(phase) >= 0 ? 1.0 : -1.0;
            // Add subtle 2nd harmonic
            wave += 0.3 * (Math.sin(phase * 2) >= 0 ? 1.0 : -1.0);
            int val = (int) (128 + 90 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    /** Bfxr Double Jump: Sudden upward pitch jump midway with pulse wave sparkle */
    private static byte[] synthBfxrDoubleJump(double duration, double f1, double f2) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        double phase = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            // Pitch jump at 35% mark
            double f = (t < 0.35) ? (f1 + 100 * t) : (f2 + 150 * (t - 0.35));
            phase += 2.0 * Math.PI * f / SAMPLE_RATE;
            double env = (1.0 - t) * (t < 0.04 ? t / 0.04 : 1.0);
            // 25% duty pulse wave for crisp arcade pop
            double wave = (phase % (2.0 * Math.PI) < 0.5 * Math.PI) ? 1.0 : -0.33;
            int val = (int) (128 + 95 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    /** Bfxr Dash: Noise sweep + high-speed resonant frequency slide */
    private static byte[] synthBfxrDash(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        Random r = new Random(42);
        double filter = 0;
        double phase = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.sin(t * Math.PI);
            double noise = r.nextDouble() * 2 - 1;
            // Dynamic lowpass cutoff sliding down
            double cutoff = 0.35 * (1.0 - t * 0.7);
            filter += cutoff * (noise - filter);
            // Downward pitch slide tone for whoosh
            double f = 600 - 350 * t;
            phase += 2.0 * Math.PI * f / SAMPLE_RATE;
            double whoosh = Math.sin(phase);
            double wave = filter * 0.7 + whoosh * 0.5;
            int val = (int) (128 + 105 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    /** Bfxr Slash: Metallic noise burst sweep */
    private static byte[] synthBfxrSlash(double duration, double f0, double f1) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        Random r = new Random(7);
        double filter = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.pow(1.0 - t, 1.2) * (t < 0.05 ? t / 0.05 : 1.0);
            double cutoff = (f0 + (f1 - f0) * t) / SAMPLE_RATE;
            filter += cutoff * (r.nextDouble() * 2 - 1 - filter);
            // Bitcrushed edge
            int steps = 12;
            double stepped = Math.round(filter * steps) / (double) steps;
            int val = (int) (128 + 115 * stepped * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    /** Bfxr Player Hurt: Heavy distorted square wave drop with crunch */
    private static byte[] synthBfxrPlayerHurt(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        Random r = new Random(99);
        double phase = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.exp(-t * 9);
            double f = 190 - 110 * t;
            phase += 2.0 * Math.PI * f / SAMPLE_RATE;
            double sq = Math.sin(phase) >= 0 ? 0.7 : -0.7;
            double noise = (r.nextDouble() * 2 - 1) * 0.4;
            double wave = sq + noise;
            int val = (int) (128 + 110 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    /** Bfxr Hit: Punchy low-end impact with sub thump */
    private static byte[] synthBfxrHit(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        Random r = new Random(15);
        double phase = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double env = Math.exp(-t * 24);
            double f = 140 - 80 * t;
            phase += 2.0 * Math.PI * f / SAMPLE_RATE;
            double sub = Math.sin(phase);
            double noise = (r.nextDouble() * 2 - 1) * 0.45;
            double wave = sub * 0.75 + noise * 0.45;
            int val = (int) (128 + 120 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    /** Bfxr Purchase: Happy ascending arpeggio chime (C5 -> E5 -> G5) */
    private static byte[] synthBfxrPurchase() {
        double[] freqs = {523.25, 659.25, 783.99};
        double totalDuration = 0.28;
        int samples = (int) (totalDuration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        int noteSamples = samples / freqs.length;

        for (int n = 0; n < freqs.length; n++) {
            double f = freqs[n];
            int start = n * noteSamples;
            int end = (n == freqs.length - 1) ? samples : (n + 1) * noteSamples;
            for (int i = start; i < end; i++) {
                double t = (double) (i - start) / (samples - start);
                double env = Math.exp(-t * 6);
                double phase = 2.0 * Math.PI * f * (double) i / SAMPLE_RATE;
                // Triangle wave + subtle chime harmonic
                double tri = (2.0 / Math.PI) * Math.asin(Math.sin(phase));
                double bell = 0.25 * Math.sin(phase * 2);
                int val = (int) (128 + 95 * (tri + bell) * env);
                b[i] = (byte) Math.max(0, Math.min(255, val));
            }
        }
        return b;
    }

    /** Bfxr UI Click: Crisp high square wave blip */
    private static byte[] synthBfxrUiClick(double duration, double freq) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        double phase = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            phase += 2.0 * Math.PI * freq / SAMPLE_RATE;
            double env = Math.exp(-t * 40);
            double wave = Math.sin(phase) >= 0 ? 0.8 : -0.8;
            int val = (int) (128 + 80 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    // =========================================================================
    // Existing Synthesizer Helpers
    // =========================================================================

    private static byte[] synthToneSweep(double duration, double fStart, double fEnd, boolean triangle) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        double phase = 0;
        for (int i = 0; i < samples; i++) {
            double t = (double) i / samples;
            double f = fStart + (fEnd - fStart) * t;
            phase += 2.0 * Math.PI * f / SAMPLE_RATE;
            double env = 1.0 - t;
            double wave = triangle ? (2.0 / Math.PI) * Math.asin(Math.sin(phase)) : Math.sin(phase);
            int val = (int) (128 + 100 * wave * env);
            b[i] = (byte) Math.max(0, Math.min(255, val));
        }
        return b;
    }

    private static byte[] synthGunshot(double duration) {
        int samples = (int) (duration * SAMPLE_RATE);
        byte[] b = new byte[samples];
        Random r = new Random(99);
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
        Random r = new Random(666);
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
        Random r = new Random(123);
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
        Random r = new Random(888);
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

    // =========================================================================
    // Retro Chiptune BGM Generator
    // =========================================================================

    /**
     * Synthesizes a seamless 8-second 8-bit chiptune loop (D minor dungeon theme):
     * Channel 1: Pulse wave lead melody
     * Channel 2: Triangle wave walking bassline
     * Channel 3: Fast 16th-note arpeggio chord texture
     * Channel 4: Periodic noise snare and hi-hats
     */
    private static void generateBgmTrack() {
        double bpm = 120.0;
        int beats = 16; // 4 bars in 4/4
        double duration = beats * (60.0 / bpm); // 8.0 seconds
        int totalSamples = (int) (duration * SAMPLE_RATE);
        bgmData = new byte[totalSamples];

        // Notes in Hz
        double D3 = 146.83, F3 = 174.61, G3 = 196.00, A3 = 220.00, Bb3 = 233.08, C4 = 261.63;
        double D4 = 293.66, E4 = 329.63, F4 = 349.23, G4 = 392.00, A4 = 440.00, Bb4 = 466.16, C5 = 523.25, D5 = 587.33;

        // 16-step melody notes (one per beat)
        double[] melody = {
                D4, F4, A4, F4,
                G4, Bb4, A4, G4,
                F4, A4, D5, C5,
                Bb4, A4, G4, E4
        };

        // Bass roots for 4 bars (4 beats per bar)
        double[] bassRoots = {
                D3, D3, D3, A3,
                Bb3, Bb3, F3, C4,
                D3, D3, F3, G3,
                Bb3, A3, G3, A3
        };

        // Chord triads for arpeggios
        double[][] chords = {
                {D4, F4, A4}, {D4, F4, A4}, {D4, F4, A4}, {A3, C4, E4},
                {Bb3, D4, F4}, {Bb3, D4, F4}, {F3, A3, C4}, {C4, E4, G4},
                {D4, F4, A4}, {D4, F4, A4}, {F3, A3, C4}, {G3, Bb3, D4},
                {Bb3, D4, F4}, {A3, C4, E4}, {G3, Bb3, D4}, {A3, C4, E4}
        };

        double beatDuration = 60.0 / bpm;
        int samplesPerBeat = (int) (beatDuration * SAMPLE_RATE);
        Random r = new Random(314);

        double melPhase = 0;
        double bassPhase = 0;
        double arpPhase = 0;

        for (int i = 0; i < totalSamples; i++) {
            int beatIdx = Math.min(beats - 1, i / samplesPerBeat);
            double beatFraction = (double) (i % samplesPerBeat) / samplesPerBeat;

            // 1. Lead Melody (Pulse Wave, 25% duty cycle)
            double melFreq = melody[beatIdx];
            melPhase += 2.0 * Math.PI * melFreq / SAMPLE_RATE;
            double melEnv = Math.exp(-beatFraction * 2.2);
            double melWave = ((melPhase % (2.0 * Math.PI)) < 0.5 * Math.PI) ? 1.0 : -0.33;
            melWave *= melEnv * 0.32;

            // 2. Bassline (Triangle wave)
            double bassFreq = bassRoots[beatIdx];
            bassPhase += 2.0 * Math.PI * bassFreq / SAMPLE_RATE;
            double bassWave = (2.0 / Math.PI) * Math.asin(Math.sin(bassPhase));
            bassWave *= 0.38;

            // 3. Arpeggio (16th notes: 4 notes per beat)
            int arpSub = (int) (beatFraction * 4.0);
            double[] currentChord = chords[beatIdx];
            double arpFreq = currentChord[arpSub % currentChord.length];
            arpPhase += 2.0 * Math.PI * arpFreq / SAMPLE_RATE;
            double arpWave = (Math.sin(arpPhase) >= 0 ? 0.6 : -0.6) * 0.16;

            // 4. Drums / Percussion
            double drum = 0;
            // Snare on beats 1 and 3 (0-indexed: beats 1, 3, 5, 7, etc.)
            if (beatIdx % 2 == 1 && beatFraction < 0.22) {
                drum += (r.nextDouble() * 2 - 1) * Math.exp(-beatFraction * 16.0) * 0.25;
            }
            // Hi-hat on 8th notes
            if ((beatFraction < 0.08 || (beatFraction > 0.5 && beatFraction < 0.58))) {
                drum += (r.nextDouble() * 2 - 1) * 0.08;
            }

            double mix = melWave + bassWave + arpWave + drum;
            int val = (int) (128 + 115 * Math.max(-1.0, Math.min(1.0, mix)));
            bgmData[i] = (byte) Math.max(0, Math.min(255, val));
        }
    }
}
