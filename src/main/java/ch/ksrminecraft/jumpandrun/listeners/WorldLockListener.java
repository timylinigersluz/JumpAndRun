package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.WorldLockManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Stellt sicher, dass JumpAndRun-Welten wieder freigegeben werden,
 * wenn ein Spieler sie verlässt, gekickt wird oder die Welt wechselt.
 * Aktiv nur für registrierte JumpAndRun-Welten.
 */
public class WorldLockListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getWorld() == null) return;

        // ✅ Nur wenn Spieler sich in einer JumpAndRun-Welt befindet
        if (!WorldRepository.exists(player.getWorld().getName())) return;

        WorldLockManager.leave(player);
        debug(player.getName() + " hat den Server verlassen → Lock freigegeben.");
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getWorld() == null) return;

        if (!WorldRepository.exists(player.getWorld().getName())) return;

        WorldLockManager.leave(player);
        debug(player.getName() + " wurde gekickt → Lock freigegeben.");
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (player == null || event.getFrom() == null) return;

        // ✅ Nur reagieren, wenn die alte Welt eine JumpAndRun-Welt war
        String oldWorld = event.getFrom().getName();
        if (!WorldRepository.exists(oldWorld)) return;

        WorldLockManager.leave(player);
        debug(player.getName() + " hat die Welt gewechselt → Lock in " + oldWorld + " freigegeben.");
    }

    private void debug(String msg) {
        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] " + msg);
        }
    }
}
