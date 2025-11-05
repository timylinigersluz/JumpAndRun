package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.TestRunManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener, der laufende TestRuns abbricht, wenn ein Spieler
 * den Server verlässt, gekickt wird oder in einer JumpAndRun-Welt stirbt.
 */
public class TestRunAbortListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getWorld() == null) return;

        // ✅ Nur in JumpAndRun-Welten aktiv
        if (!WorldRepository.exists(player.getWorld().getName())) return;

        if (TestRunManager.isTesting(player)) {
            TestRunManager.abortTest(player);
            Bukkit.getConsoleSender().sendMessage("[JNR] TestRun von " + player.getName() + " wegen Disconnect abgebrochen.");
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getWorld() == null) return;

        // ✅ Nur in JumpAndRun-Welten aktiv
        if (!WorldRepository.exists(player.getWorld().getName())) return;

        if (TestRunManager.isTesting(player)) {
            TestRunManager.abortTest(player);
            Bukkit.getConsoleSender().sendMessage("[JNR] TestRun von " + player.getName() + " wegen Kick abgebrochen.");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player == null || player.getWorld() == null) return;

        // ✅ Nur in JumpAndRun-Welten aktiv
        if (!WorldRepository.exists(player.getWorld().getName())) return;

        if (TestRunManager.isTesting(player)) {
            TestRunManager.abortTest(player);
            Bukkit.getConsoleSender().sendMessage("[JNR] TestRun von " + player.getName() + " wegen Tod abgebrochen.");
        }
    }
}
