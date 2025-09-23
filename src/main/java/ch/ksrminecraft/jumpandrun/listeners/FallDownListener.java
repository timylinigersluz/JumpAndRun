package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listener, der Spielerbewegungen überwacht und bei einem Sturz
 * unterhalb der hinterlegten Y-Grenze zurück zum letzten Checkpoint
 * (oder Startpunkt, falls keiner existiert) teleportiert.
 */
public class FallDownListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        String worldName = world.getName();

        // Nur in registrierten JnRs reagieren (außer Debugmodus)
        if (!WorldRepository.exists(worldName) && !JumpAndRun.getConfigManager().isDebug()) {
            return;
        }

        int yLimit = WorldRepository.getYLimit(worldName);

        // Spieler unterhalb der Grenze?
        if (player.getY() < yLimit) {
            Location teleportLocation = null;

            // 1. Prüfen ob Spieler einen Checkpoint hat
            Location checkpoint = PressurePlateListener.getLastCheckpoint(player);
            if (checkpoint != null) {
                teleportLocation = checkpoint.clone().add(0, 1, 0);
            }

            // 2. Fallback: Mitte der Startinsel
            if (teleportLocation == null) {
                Location start = WorldRepository.getStartLocation(worldName);
                if (start != null) {
                    teleportLocation = start.clone().add(0, JumpAndRun.height + 1, 0);

                    // Blickrichtung zur Zielinsel setzen (einfach: +20 Blöcke auf X-Achse)
                    Location goal = new Location(world, start.getX() + 20, start.getY(), start.getZ());
                    teleportLocation.setDirection(goal.toVector().subtract(teleportLocation.toVector()));
                }
            }

            // 3. Sicherheitscheck
            if (teleportLocation == null || teleportLocation.getWorld() == null) {
                if (JumpAndRun.getConfigManager().isDebug()) {
                    Bukkit.getConsoleSender().sendMessage(
                            "[JNR-DEBUG] Keine gültige Teleport-Location für Welt " + worldName + " gefunden."
                    );
                }
                return;
            }

            // Teleport durchführen
            player.setFallDistance(0);
            player.teleport(teleportLocation);

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage(
                        "[JNR-DEBUG] Spieler " + player.getName() +
                                " ist unter Y=" + yLimit + " gefallen und wurde nach " +
                                formatLocation(teleportLocation) + " teleportiert."
                );
            }
        }
    }

    /**
     * Hilfsmethode zur Formatierung einer Location für Debug-Logs.
     */
    private String formatLocation(Location loc) {
        return String.format("(%s | x=%.1f, y=%.1f, z=%.1f)",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ()
        );
    }
}
