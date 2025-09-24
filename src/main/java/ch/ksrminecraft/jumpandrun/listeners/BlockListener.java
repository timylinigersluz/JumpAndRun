package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Verhindert Blockabbau und -platzieren in veröffentlichten JumpAndRun-Welten.
 */
public class BlockListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        String worldName = event.getBlock().getWorld().getName();

        // Nur veröffentlichte JumpAndRuns sperren
        if (WorldRepository.isPublished(worldName)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Nöööö, das darfst du hier nicht!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        String worldName = event.getBlock().getWorld().getName();

        if (WorldRepository.isPublished(worldName)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Nöööö, das darfst du hier nicht!");
        }
    }
}
