package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.utils.WorldLockManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener, der sicherstellt, dass JumpAndRun-Welten wieder
 * freigegeben werden, wenn ein Spieler sie verlässt.
 */
public class WorldLockListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        WorldLockManager.leave(player);
        debug(player.getName() + " hat den Server verlassen → Lock freigegeben.");
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        WorldLockManager.leave(player);
        debug(player.getName() + " wurde gekickt → Lock freigegeben.");
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        WorldLockManager.leave(player);
        debug(player.getName() + " hat die Welt gewechselt → Lock freigegeben.");
    }

    private void debug(String msg) {
        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] " + msg);
        }
    }
}
