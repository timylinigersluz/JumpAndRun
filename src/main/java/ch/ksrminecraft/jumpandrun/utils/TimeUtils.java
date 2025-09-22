package ch.ksrminecraft.jumpandrun.utils;

/**
 * Helferklasse für Zeit-Formatierungen.
 */
public class TimeUtils {
    /**
     * Formatiert Millisekunden in mm:ss.SSS
     */
    public static String formatMs(long ms) {
        long minutes = (ms / 1000) / 60;
        long seconds = (ms / 1000) % 60;
        long millis = ms % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }
}
