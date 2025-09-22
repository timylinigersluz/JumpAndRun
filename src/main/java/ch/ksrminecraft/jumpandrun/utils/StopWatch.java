package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Einfache Stoppuhr, die im Actionbar jedes Spielers die verstrichene Zeit anzeigt.
 */
public class StopWatch {

    /** Anzahl verstrichener Sekunden seit dem letzten Start. */
    private int secondsPassed = 0;

    /** Kennzeichnet, ob die Stoppuhr aktuell läuft. */
    private boolean running = false;

    /** Referenz auf die geplante Bukkit-Aufgabe zur periodischen Aktualisierung. */
    private BukkitRunnable timerTask;

    /** Prüft, ob Debugmodus aktiv ist. */
    private boolean isDebug() {
        return JumpAndRun.getConfigManager().isDebug();
    }

    /**
     * Startet die Stoppuhr und zeigt die Zeit allen Spielern im Actionbar an.
     */
    public void startStopwatch() {
        if (running) {
            if (isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] StopWatch wurde bereits gestartet.");
            }
            return;
        }

        resetStopwatch(); // Reset der Zeit vor dem Start
        running = true;

        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                secondsPassed++;
                updateTimer();
            }
        };

        timerTask.runTaskTimer(JumpAndRun.getPlugin(), 20, 20); // Update jede Sekunde (20 Ticks)

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] StopWatch gestartet.");
        }
    }

    /**
     * Stoppt die Stoppuhr und liefert die verstrichene Zeit zurück.
     *
     * @return Anzahl Sekunden seit Start oder 0, falls nicht gelaufen.
     */
    public int stopStopwatch() {
        if (!running) {
            if (isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] StopWatch war nicht aktiv.");
            }
            return 0;
        }

        running = false;
        if (timerTask != null) {
            timerTask.cancel();
        }

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] StopWatch gestoppt. Zeit=" + formatTime(secondsPassed));
        }

        return secondsPassed;
    }

    /**
     * Setzt die interne Zeit zurück und aktualisiert die Anzeige.
     */
    public void resetStopwatch() {
        secondsPassed = 0;
        updateTimer();

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] StopWatch zurückgesetzt.");
        }
    }

    /**
     * Sendet die aktuelle Zeit an alle Spieler (Actionbar).
     */
    private void updateTimer() {
        String display = ChatColor.YELLOW + "Time Passed: " + formatTime(secondsPassed);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(display);
        }
    }

    /**
     * @return Gibt zurück, ob die StopWatch aktuell läuft.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Formatiert Sekunden in ein HH:MM:SS-Format.
     */
    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
}
