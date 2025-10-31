package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.PlayerUtils;
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
 * Setzt ausserdem Spielerwerte zurück, aber stoppt NICHT den aktiven Run.
 */
public class FallDownListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        String worldName = world.getName();

        // 🟢 NEU: Falls Spieler nicht in einer registrierten JumpAndRun-Welt ist,
        // werden alte Checkpoints gelöscht, um Cross-World-Teleports zu verhindern.
        if (!WorldRepository.exists(worldName)) {
            PressurePlateListener.clearCheckpoint(player.getUniqueId());
            return;
        }

        // Wenn Debug aktiv ist, dürfen auch Nicht-JnR-Welten geprüft werden (z. B. Testzwecke)
        if (!WorldRepository.exists(worldName) && !JumpAndRun.getConfigManager().isDebug()) {
            return;
        }

        int yLimit = WorldRepository.getYLimit(worldName);

        // Spieler unterhalb der Grenze?
        if (player.getY() < yLimit) {
            Location teleportLocation = null;

            // 1️⃣ Prüfen, ob Spieler einen Checkpoint hat
            Location checkpoint = PressurePlateListener.getLastCheckpoint(player);
            if (checkpoint != null) {
                teleportLocation = checkpoint.clone().add(0, 1, 0);
            }

            // 2️⃣ Fallback: Mitte der Startinsel
            if (teleportLocation == null) {
                Location start = WorldRepository.getStartLocation(worldName);
                if (start != null) {
                    teleportLocation = start.clone().add(0, JumpAndRun.height + 1, 0);

                    // Blickrichtung zur Zielinsel setzen
                    Location goal = new Location(world, start.getX() + 20, start.getY(), start.getZ());
                    teleportLocation.setDirection(goal.toVector().subtract(teleportLocation.toVector()));
                }
            }

            if (teleportLocation == null) {
                if (JumpAndRun.getConfigManager().isDebug()) {
                    Bukkit.getConsoleSender().sendMessage(
                            "[JNR-DEBUG] Keine gültige Teleport-Location für Welt " + worldName + " gefunden."
                    );
                }
                return;
            }

            // Spieler zurücksetzen (Effekte, Velocity etc.) und teleportieren
            PlayerUtils.resetState(player);
            player.teleport(teleportLocation);

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage(
                        "[JNR-DEBUG] Spieler " + player.getName() +
                                " ist unter Y=" + yLimit +
                                " gefallen → Teleport nach " + formatLocation(teleportLocation) +
                                " (Run bleibt aktiv)."
                );
            }
        }
    }

    private String formatLocation(Location loc) {
        return String.format("(%s | x=%.1f, y=%.1f, z=%.1f)",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ()
        );
    }
}
