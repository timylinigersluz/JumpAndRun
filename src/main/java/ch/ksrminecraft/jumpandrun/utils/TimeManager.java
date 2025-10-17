package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.TimeRepository;
import ch.ksrminecraft.jumpandrun.db.RunRepository;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Verwaltet die Zeitmessung eines Jump-and-Run Durchgangs.
 * Nutzt eine eigene StopWatch pro Spieler.
 */
public class TimeManager {

    private static final Map<UUID, StopWatch> stopWatches = new HashMap<>();

    /**
     * Startet einen neuen Run für den Spieler.
     * - merkt sich die Startzeit in der RunRepository
     * - startet eine persönliche StopWatch im ActionBar
     */
    public static void inputStartTime(World world, Player player) {
        RunRepository.inputStartTime(world, player);

        // ggf. alte StopWatch stoppen, damit keine doppelt läuft
        stopWatch(player);

        StopWatch sw = new StopWatch(player);
        sw.start();
        stopWatches.put(player.getUniqueId(), sw);

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Startzeit gesetzt: "
                    + "Spieler=" + player.getName() + ", Welt=" + world.getName()
                    + " (StopWatch gestartet)");
        }
    }

    /**
     * Berechnet die Endzeit eines Runs und speichert sie in der DB.
     * - stoppt die StopWatch
     * - aktualisiert Leaderboard und Punkte
     *
     * @return Dauer in Millisekunden oder -1 wenn keine Startzeit gefunden wurde
     */
    public static long calcTime(World world, Player endPlayer) {
        Long startTime = RunRepository.getStartTime(world, endPlayer);

        if (startTime == null) {
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Keine Startzeit für Spieler "
                        + endPlayer.getName() + " in Welt " + world.getName());
            }
            stopWatch(endPlayer);
            return -1;
        }

        long completionTime = System.currentTimeMillis() - startTime;

        // Zeit in DB speichern
        TimeRepository.inputTime(world, endPlayer, completionTime);

        // Aktuelle Bestzeit abfragen
        Long bestTime = TimeRepository.getBestTime(world.getName());
        boolean newRecord = (bestTime == null || completionTime < bestTime);

        if (newRecord) {
            // 🔹 Alias anhand der Welt ermitteln
            String alias = WorldRepository.getAlias(world.getName());
            if (alias != null && !alias.isEmpty()) {
                SignUpdater.updateLeaderSigns(alias); // ✅ jetzt alias-basiert
                if (JumpAndRun.getConfigManager().isDebug()) {
                    Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Leader-Schilder für Alias '" + alias + "' aktualisiert.");
                }
            } else {
                if (JumpAndRun.getConfigManager().isDebug()) {
                    Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Kein Alias für Welt " + world.getName() + " gefunden, Leader-Signs nicht aktualisiert.");
                }
            }

            // Punkte für neuen Rekord vergeben
            PointsService.awardRecordPoints(endPlayer);

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Neuer Rekord: "
                        + endPlayer.getName() + " (" + TimeRepository.formatTime(completionTime)
                        + ") in Welt=" + world.getName()
                        + (alias != null ? " (Alias=" + alias + ")" : ""));
            }
        }

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler "
                    + endPlayer.getName() + " hat den Run abgeschlossen: "
                    + TimeRepository.formatTime(completionTime)
                    + " (" + completionTime + "ms, Welt=" + world.getName() + ")");
        }

        // Run aufräumen
        RunRepository.clearRun(world, endPlayer);
        stopWatch(endPlayer);

        return completionTime;
    }

    /**
     * Stoppt und entfernt die persönliche StopWatch eines Spielers.
     */
    public static void stopWatch(Player player) {
        StopWatch sw = stopWatches.remove(player.getUniqueId());
        if (sw != null) {
            sw.stop();
        }
    }

    public static void stopStopWatch(Player player) {
        stopWatch(player);
    }

    /**
     * Prüft, ob ein Spieler aktuell eine aktive StopWatch laufen hat.
     */
    public static boolean hasStopWatch(Player player) {
        return stopWatches.containsKey(player.getUniqueId());
    }
}
