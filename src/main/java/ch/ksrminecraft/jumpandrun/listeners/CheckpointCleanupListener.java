package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Listener, der gespeicherte Checkpoints eines Spielers löscht,
 * sobald dieser den Server verlässt, gekickt wird oder stirbt.
 * Aktiv nur in JumpAndRun-Welten.
 */
public class CheckpointCleanupListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getWorld() == null) return;

        String worldName = player.getWorld().getName();
        if (!WorldRepository.exists(worldName)) return; // ✅ Nur in JnR-Welten aktiv

        cleanup(player);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getWorld() == null) return;

        String worldName = player.getWorld().getName();
        if (!WorldRepository.exists(worldName)) return;

        cleanup(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player == null || player.getWorld() == null) return;

        String worldName = player.getWorld().getName();
        if (!WorldRepository.exists(worldName)) return; // ✅ Nur in JnR-Welten aktiv

        Location checkpoint = PressurePlateListener.getLastCheckpoint(player);

        if (checkpoint != null) {
            // Spieler wird nach Respawn zum letzten Checkpoint gesetzt
            Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), () -> {
                player.teleport(checkpoint.clone().add(0, 1, 0));
                player.sendMessage("§eDu wurdest zu deinem letzten Checkpoint zurückgesetzt.");
            }, 1L); // 1 Tick warten, damit Respawn abgeschlossen ist
        } else {
            // Fallback: kein Checkpoint → zum Start
            Location start = WorldRepository.getStartLocation(worldName);
            if (start != null) {
                Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), () -> {
                    player.teleport(start.clone().add(0, 1, 0));
                    player.sendMessage("§eDu wurdest zum Start zurückgesetzt.");
                }, 1L);
            }
        }
    }

    private void cleanup(Player player) {
        UUID uuid = player.getUniqueId();
        if (PressurePlateListener.getLastCheckpoint(player) != null) {
            PressurePlateListener.clearCheckpoint(uuid);
            Bukkit.getConsoleSender().sendMessage("[JNR] Checkpoint von " + player.getName() + " entfernt (Cleanup).");
        }
    }
}
