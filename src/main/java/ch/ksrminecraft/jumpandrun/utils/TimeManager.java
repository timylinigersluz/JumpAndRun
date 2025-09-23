package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.TimeRepository;
import ch.ksrminecraft.jumpandrun.db.RunRepository;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Verwaltet die Zeitmessung eines Jump-and-Run Durchgangs,
 * indem Start- und Endzeiten gesammelt und in der Datenbank abgelegt werden.
 */
public class TimeManager {

    public static void inputStartTime(World world, Player player) {
        RunRepository.inputStartTime(world, player);
    }

    public static void calcTime(World world, Player endPlayer) {
        Long startTime = RunRepository.getStartTime(world, endPlayer);

        if (startTime == null) {
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Keine Startzeit für Spieler "
                        + endPlayer.getName() + " in Welt " + world.getName());
            }
            return;
        }

        long completionTime = System.currentTimeMillis() - startTime;

        // Zeit speichern
        TimeRepository.inputTime(world, endPlayer, completionTime);

        // Beste Zeit + Leader laden
        Long bestTime = TimeRepository.getBestTime(world.getName());
        String leader = TimeRepository.getLeader(world.getName());

        // Prüfen, ob neuer Rekord
        if (bestTime == null || completionTime < bestTime) {
            SignUpdater.updateLeaderSigns(world.getName());
            PointsService.awardRecordPoints(endPlayer);

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Neuer Leader: "
                        + endPlayer.getName() + " in Welt " + world.getName()
                        + " mit " + TimeRepository.formatTime(completionTime));
            }
        }

        // Debug: Run abgeschlossen
        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler "
                    + endPlayer.getName() + " hat den Run in "
                    + TimeRepository.formatTime(completionTime) + " abgeschlossen.");
        }

        // ActiveRun löschen
        RunRepository.clearRun(world, endPlayer);
    }
}
