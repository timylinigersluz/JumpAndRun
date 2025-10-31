package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Dienstmethoden für Teleportationen im JumpAndRun.
 */
public class TeleportUtils {

    /**
     * Teleportiert den Spieler zur Startinsel einer JumpAndRun-Welt.
     *
     * @param player    Spieler
     * @param worldName Name der JumpAndRun-Welt
     */
    public static void teleportToIsland(Player player, String worldName) {
        Location start = WorldRepository.getStartLocation(worldName);

        if (start != null && start.getWorld() != null) {
            // etwas oberhalb teleportieren
            Location safe = start.clone().add(0, 1, 0);

            // 🟢 Fallschaden & Bewegung zurücksetzen
            player.setVelocity(new Vector(0, 0, 0));
            player.setFallDistance(0);
            player.setNoDamageTicks(20); // 1 Sekunde Immunität

            player.teleport(safe);
            player.sendMessage("Du wurdest zur JumpAndRun-Insel teleportiert!");
        } else {
            player.sendMessage("Startpunkt konnte nicht gefunden werden.");
        }
    }

    /**
     * Teleportiert den Spieler zur Hauptwelt ("world").
     *
     * @param player Spieler
     */
    public static void teleportToMainWorld(Player player) {
        World mainWorld = Bukkit.getWorld("world");
        if (mainWorld != null) {
            // 🟢 Fallschaden & Bewegung zurücksetzen
            player.setVelocity(new Vector(0, 0, 0));
            player.setFallDistance(0);
            player.setNoDamageTicks(20); // kurze Immunität, falls Spieler fiel

            player.teleport(mainWorld.getSpawnLocation());
            player.sendMessage("Du wurdest zur Hauptwelt teleportiert!");
        } else {
            player.sendMessage("Die Hauptwelt existiert nicht.");
        }
    }
}
