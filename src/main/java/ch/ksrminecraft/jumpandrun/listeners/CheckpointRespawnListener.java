package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Listener, der Spieler nach einem Tod zum letzten Checkpoint
 * (oder zum Startpunkt) zurücksetzt, ohne den Timer zurückzusetzen.
 */
public class CheckpointRespawnListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String worldName = player.getWorld().getName();

        // Nur für registrierte JnR-Welten
        if (!WorldRepository.isPublished(worldName) && !JumpAndRun.getConfigManager().isDebug()) {
            return;
        }

        // Prüfen ob Spieler einen Checkpoint hat
        Location checkpoint = PressurePlateListener.getLastCheckpoint(player);

        Location target = null;
        if (checkpoint != null) {
            target = checkpoint.clone().add(0, 1, 0);
        } else {
            target = WorldRepository.getStartLocation(worldName);
            if (target != null) {
                target = target.clone().add(0, 1, 0);
            }
        }

        if (target == null) {
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Keine Respawn-Location für Spieler "
                        + player.getName() + " in Welt " + worldName + " gefunden.");
            }
            return;
        }

        // Teleport nach Respawn (muss delayed passieren!)
        Location finalTarget = target;
        Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), () -> {
            if (player.isOnline() && player.getWorld().getName().equals(worldName)) {
                player.teleport(finalTarget);
                player.sendMessage("§eDu wurdest zu deinem letzten Checkpoint zurückgesetzt.");
            }
        }, 1L);
    }
}
