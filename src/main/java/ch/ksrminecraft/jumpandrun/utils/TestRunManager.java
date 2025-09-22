package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Verwalten des Testmodus für JumpAndRun-Ersteller.
 * Hält fest, welche Spieler gerade ihre eigenen JnRs testen
 * und welche StopWatches dazu gehören.
 */
public class TestRunManager {

    /** Map<Spieler-UUID, StopWatch> für laufende Testruns */
    private static final Map<UUID, StopWatch> activeTests = new HashMap<>();

    /** Startet einen Testmodus für den angegebenen Spieler */
    public static void startTest(Player player) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.startStopwatch();
        activeTests.put(player.getUniqueId(), stopWatch);

        player.setGameMode(GameMode.SURVIVAL);

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Testmodus für " + player.getName() + " gestartet.");
        }
    }

    /** Beendet den Testmodus erfolgreich und setzt die Welt auf Published */
    public static void completeTest(Player player) {
        StopWatch stopWatch = activeTests.remove(player.getUniqueId());
        if (stopWatch != null) {
            int seconds = stopWatch.stopStopwatch();

            String worldName = player.getWorld().getName();
            WorldRepository.setPublished(worldName, true);

            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage("§aDein JumpAndRun wurde veröffentlicht! Zeit: " + seconds + "s");

            Bukkit.getConsoleSender().sendMessage("[JNR] Welt " +
                    worldName + " wurde veröffentlicht durch " + player.getName() +
                    " (Zeit=" + seconds + "s)");
        }
    }

    /** Bricht einen Test ab (z.B. Disconnect, Kick oder Tod) */
    public static void abortTest(Player player) {
        StopWatch stopWatch = activeTests.remove(player.getUniqueId());
        if (stopWatch != null) {
            stopWatch.stopStopwatch();

            // Spieler zurücksetzen
            player.setGameMode(GameMode.CREATIVE);

            // Falls möglich zurück zur Startlocation
            String worldName = player.getWorld().getName();
            Location start = WorldRepository.getStartLocation(worldName);
            if (start != null) {
                player.teleport(start.clone().add(0, JumpAndRun.height + 1, 0));
            }

            player.sendMessage("§cDein Test wurde abgebrochen.");
            Bukkit.getConsoleSender().sendMessage("[JNR] TestRun von " + player.getName() +
                    " in Welt " + worldName + " wurde abgebrochen. Welt bleibt im Draft-Modus.");

            if (isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Testmodus für " + player.getName() + " abgebrochen.");
            }
        }
    }

    /** Prüft, ob Spieler gerade testet */
    public static boolean isTesting(Player player) {
        return activeTests.containsKey(player.getUniqueId());
    }

    private static boolean isDebug() {
        return JumpAndRun.getConfigManager().isDebug();
    }
}
