package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.TimeRepository;
import ch.ksrminecraft.jumpandrun.db.RunRepository;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.Time;

/**
 * Verwaltet die Zeitmessung eines Jump-and-Run Durchgangs,
 * indem Start- und Endzeiten gesammelt und in der Datenbank abgelegt werden.
 */
public class TimeManager {

    /**
     * Persistiert Startzeit und Spieler in der Datenbank.
     */
    public static void inputStartTime(World world, Player player) {
        RunRepository.inputStartTime(world, player);
    }

    /**
     * Berechnet die Differenz zwischen Start und Ende eines Durchgangs.
     * Speichert die Zeit in der DB, prüft auf neuen Rekord
     * und triggert ggf. Leader-Schild-Update.
     */
    public static void calcTime(World world, Player endPlayer) {
        Time endTime = Time.valueOf(java.time.LocalTime.now());

        Time startTime = RunRepository.getStartTime(world);
        Player startPlayer = RunRepository.getStartPlayer(world);

        if (startPlayer != null && startPlayer.equals(endPlayer) && startTime != null) {
            // Differenz in Millisekunden
            long completionTime = endTime.getTime() - startTime.getTime();

            // Zeit speichern
            TimeRepository.inputTime(world, endPlayer, completionTime);

            // Aktuelle Bestzeit und Leader laden
            Long bestTime = TimeRepository.getBestTime(world.getName());
            String leader = TimeRepository.getLeader(world.getName());

            // Prüfen, ob Spieler neuer Leader mit Rekord ist
            if (bestTime != null && leader != null) {
                if (completionTime <= bestTime &&
                        leader.equals(endPlayer.getUniqueId().toString())) {

                    // Leader-Schilder aktualisieren
                    SignUpdater.updateLeaderSigns(world.getName());

                    // Punkte vergeben
                    PointsService.awardRecordPoints(endPlayer);

                    if (JumpAndRun.getConfigManager().isDebug()) {
                        Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Neuer Rekord von "
                                + endPlayer.getName() + " in Welt " + world.getName()
                                + ": " + completionTime + "ms → Leader-Schild aktualisiert.");
                    }
                }
            }

            // Debug: Run abgeschlossen
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler "
                        + endPlayer.getName() + " hat den Run in "
                        + completionTime + "ms abgeschlossen.");
            }
        } else {
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Kein gültiger Startspieler oder Startzeit für Welt "
                        + world.getName());
            }
        }
    }
}
