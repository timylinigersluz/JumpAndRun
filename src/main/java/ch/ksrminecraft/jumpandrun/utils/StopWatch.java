package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Stoppuhr für einen bestimmten Spieler.
 * Zeigt die verstrichene Zeit im Actionbar nur für diesen Spieler an.
 */
public class StopWatch {

    private final Player player;
    private int secondsPassed = 0;
    private boolean running = false;
    private BukkitRunnable timerTask;

    public StopWatch(Player player) {
        this.player = player;
    }

    private boolean isDebug() {
        return JumpAndRun.getConfigManager().isDebug();
    }

    /**
     * Startet die Stoppuhr für den Spieler.
     */
    public void start() {
        if (running) {
            if (isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] StopWatch für "
                        + player.getName() + " läuft bereits.");
            }
            return;
        }

        reset();
        running = true;

        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    stop();
                    cancel();
                    return;
                }
                secondsPassed++;
                updateActionBar();
            }
        };

        timerTask.runTaskTimer(JumpAndRun.getPlugin(), 20, 20); // jede Sekunde

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] StopWatch für "
                    + player.getName() + " gestartet.");
        }
    }

    /**
     * Stoppt die Stoppuhr.
     * @return verstrichene Sekunden
     */
    public int stop() {
        if (!running) {
            if (isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] StopWatch für "
                        + player.getName() + " war nicht aktiv.");
            }
            return 0;
        }

        running = false;
        if (timerTask != null) {
            timerTask.cancel();
        }

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] StopWatch für "
                    + player.getName() + " gestoppt bei " + formatTime(secondsPassed));
        }

        return secondsPassed;
    }

    /**
     * Setzt die interne Zeit zurück.
     */
    public void reset() {
        secondsPassed = 0;
        updateActionBar();
    }

    private void updateActionBar() {
        String display = ChatColor.YELLOW + "⏱ Zeit: " + formatTime(secondsPassed);
        player.sendActionBar(display);
    }

    public boolean isRunning() {
        return running;
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
}
