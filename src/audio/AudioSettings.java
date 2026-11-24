package audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Persisted audio configuration: volumes and mute states for SFX and BGM.
 * Saves to audio_settings.properties in the working directory.
 */
public final class AudioSettings {
    private static final String FILE_NAME = "audio_settings.properties";

    private static double sfxVolume = 0.8;
    private static boolean sfxMuted = false;
    private static double bgmVolume = 0.6;
    private static boolean bgmMuted = false;

    static {
        load();
    }

    private AudioSettings() {}

    public static double getSfxVolume() { return sfxVolume; }
    public static void setSfxVolume(double vol) {
        sfxVolume = Math.max(0.0, Math.min(1.0, vol));
        save();
        SoundManager.applyAudioSettings();
    }

    public static boolean isSfxMuted() { return sfxMuted; }
    public static void setSfxMuted(boolean muted) {
        sfxMuted = muted;
        save();
        SoundManager.applyAudioSettings();
    }

    public static void toggleSfxMute() {
        setSfxMuted(!sfxMuted);
    }

    public static double getBgmVolume() { return bgmVolume; }
    public static void setBgmVolume(double vol) {
        bgmVolume = Math.max(0.0, Math.min(1.0, vol));
        save();
        SoundManager.applyAudioSettings();
    }

    public static boolean isBgmMuted() { return bgmMuted; }
    public static void setBgmMuted(boolean muted) {
        bgmMuted = muted;
        save();
        SoundManager.applyAudioSettings();
    }

    public static void toggleBgmMute() {
        setBgmMuted(!bgmMuted);
    }

    public static double getEffectiveSfxVolume() {
        return sfxMuted ? 0.0 : sfxVolume;
    }

    public static double getEffectiveBgmVolume() {
        return bgmMuted ? 0.0 : bgmVolume;
    }

    public static void load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            sfxVolume = Double.parseDouble(props.getProperty("sfxVolume", "0.8"));
            sfxMuted = Boolean.parseBoolean(props.getProperty("sfxMuted", "false"));
            bgmVolume = Double.parseDouble(props.getProperty("bgmVolume", "0.6"));
            bgmMuted = Boolean.parseBoolean(props.getProperty("bgmMuted", "false"));
        } catch (Exception ignored) {
            // Keep current/default settings if file is malformed
        }
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("sfxVolume", String.format(java.util.Locale.US, "%.2f", sfxVolume));
        props.setProperty("sfxMuted", Boolean.toString(sfxMuted));
        props.setProperty("bgmVolume", String.format(java.util.Locale.US, "%.2f", bgmVolume));
        props.setProperty("bgmMuted", Boolean.toString(bgmMuted));

        try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
            props.store(out, "Gloamroot Audio Settings");
        } catch (IOException ignored) {
            // Ignore write failures gracefully
        }
    }
}
