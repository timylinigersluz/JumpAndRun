package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        String worldName = player.getWorld().getName();

        // Nur in published JnRs reagieren (außer Debugmodus)
        if (!WorldRepository.isPublished(worldName) && !JumpAndRun.getConfigManager().isDebug()) {
            return;
        }

        int yLimit = WorldRepository.getYLimit(worldName);

        // Spieler unter der Grenze?
        if (player.getY() < yLimit) {
            Location teleportLocation = null;

            // 1. Prüfen ob Spieler einen Checkpoint hat
            Location checkpoint = PressurePlateListener.getLastCheckpoint(player);
            if (checkpoint != null) {
                teleportLocation = checkpoint.clone().add(0, 1, 0); // 1 Block höher
            }

            // 2. Fallback: Startlocation
            if (teleportLocation == null) {
                teleportLocation = WorldRepository.getStartLocation(worldName);
                if (teleportLocation != null) {
                    teleportLocation = teleportLocation.clone().add(0, 1, 0); // auch hier 1 Block höher
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
