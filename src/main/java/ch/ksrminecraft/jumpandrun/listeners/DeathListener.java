package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Meldet den Tod eines Spielers in einer JumpAndRun-Welt,
 * ohne den Run abzubrechen. Der Spieler wird beim Respawn
 * automatisch zum letzten Checkpoint (oder Startpunkt)
 * teleportiert, und die Zeit läuft weiter.
 */
public class DeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player == null || player.getWorld() == null) return;

        String worldName = player.getWorld().getName();

        // ✅ Nur reagieren, wenn die Welt in WorldRepository registriert ist
        if (!WorldRepository.exists(worldName)) return;

        // Nur Hinweis an den Spieler – kein Abbruch, keine Zeitunterbrechung
        player.sendMessage(ChatColor.YELLOW + "Du bist gestorben! "
                + ChatColor.GRAY + "Du wirst zu deinem letzten Checkpoint zurückgesetzt.");

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage(
                    "[JNR-DEBUG] Spieler " + player.getName() +
                            " ist in Welt " + worldName + " gestorben → Respawn ohne Abbruch, Zeit läuft weiter."
            );
        }
    }
}
