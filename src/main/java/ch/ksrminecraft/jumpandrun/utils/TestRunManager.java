package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Verwalten des Testmodus für JumpAndRun-Ersteller.
 * Nutzt den zentralen TimeManager für StopWatches.
 */
public class TestRunManager {

    /** Spieler, die vorbereitet sind (Teleport + Adventure, aber Zeit läuft noch nicht) */
    private static final Set<UUID> preparedTests = new HashSet<>();

    /** Spieler für Testmodus vorbereiten (Teleport, Adventure), aber Zeit noch nicht starten */
    public static void prepareTest(Player player) {
        preparedTests.add(player.getUniqueId());
        player.setGameMode(GameMode.ADVENTURE);

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler " + player.getName() +
                    " ist vorbereitet (Testmodus aktiv, Zeit läuft noch nicht).");
        }
    }

    /** Prüfen, ob Spieler vorbereitet ist */
    public static boolean isPrepared(Player player) {
        return preparedTests.contains(player.getUniqueId());
    }

    /** Startet den Test wirklich (z.B. beim Betreten der Startdruckplatte) */
    public static void startTest(Player player) {
        if (!preparedTests.remove(player.getUniqueId())) {
            return; // Spieler war nicht vorbereitet
        }

        // Startzeit + StopWatch über TimeManager setzen
        World world = player.getWorld();
        TimeManager.inputStartTime(world, player);

        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage("§aLos geht's! Deine Zeit läuft jetzt.");

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Testlauf von " + player.getName() + " gestartet.");
        }
    }

    /** Beendet den Testmodus erfolgreich und setzt die Welt auf Published */
    public static void completeTest(Player player) {
        preparedTests.remove(player.getUniqueId());

        // StopWatch zentral beenden
        TimeManager.stopStopWatch(player);

        String worldName = player.getWorld().getName();
        WorldRepository.setPublished(worldName, true);

        player.setGameMode(GameMode.CREATIVE);
        player.sendMessage("§aDein JumpAndRun wurde veröffentlicht!");

        Bukkit.getConsoleSender().sendMessage("[JNR] Welt " +
                worldName + " wurde veröffentlicht durch " + player.getName());
    }

    /** Bricht einen Test ab */
    public static void abortTest(Player player) {
        preparedTests.remove(player.getUniqueId());

        // StopWatch zentral beenden
        TimeManager.stopStopWatch(player);

        player.setGameMode(GameMode.CREATIVE);

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

    /** Prüft, ob Spieler gerade testet (d.h. Zeit läuft) */
    public static boolean isTesting(Player player) {
        return TimeManager.hasStopWatch(player);
    }

    private static boolean isDebug() {
        return JumpAndRun.getConfigManager().isDebug();
    }

}
