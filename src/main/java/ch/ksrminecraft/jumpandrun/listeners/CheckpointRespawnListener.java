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
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Stellt sicher, dass Spieler nach einem Tod in einer JumpAndRun-Welt
 * (auch Drafts oder KeepWorlds) immer in derselben Welt respawnen –
 * am letzten Checkpoint oder am Startpunkt.
 */
public class CheckpointRespawnListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();
        String worldName = world.getName();

        // 🟡 Nur reagieren, wenn Welt eine JumpAndRun-Welt ist (Name oder DB-Eintrag)
        boolean isJumpAndRunWorld =
                worldName.toLowerCase().startsWith("jnr_") || WorldRepository.exists(worldName);

        if (!isJumpAndRunWorld) {
            // Normale Welten ignorieren (z. B. Lobby, Bedwars etc.)
            return;
        }

        // Ziel bestimmen: Checkpoint oder Startpunkt
        Location checkpoint = PressurePlateListener.getLastCheckpoint(player);
        Location target = null;

        if (checkpoint != null) {
            target = checkpoint.clone().add(0, 1, 0);
        } else {
            Location start = WorldRepository.getStartLocation(worldName);
            if (start != null) {
                target = start.clone().add(0, 1, 0);
            } else {
                // Wenn Welt keine gespeicherten Punkte hat, fallback = Weltspawn
                target = world.getSpawnLocation().clone().add(0, 1, 0);
            }
        }

        Location finalTarget = target;

        // 1 Tick delay, um Vanilla-Respawn abzuschliessen
        Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), () -> {
            if (!player.isOnline()) return;

            // 🟢 Garantieren, dass der Spieler in derselben Welt bleibt
            if (!player.getWorld().equals(world)) {
                player.teleport(finalTarget);
            }

            // Spieler zurücksetzen (Health, Hunger, Velocity)
            PlayerUtils.resetState(player);
            player.teleport(finalTarget);
            player.sendMessage("§eDu wurdest zu deinem letzten Checkpoint zurückgesetzt.");

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage(
                        "[JNR-DEBUG] Respawn: " + player.getName() +
                                " → " + formatLocation(finalTarget)
                                + " (Welt: " + worldName + ")"
                );
            }
        }, 1L);
    }

    private String formatLocation(Location loc) {
        return String.format("(%s | x=%.1f, y=%.1f, z=%.1f)",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ());
    }
}
