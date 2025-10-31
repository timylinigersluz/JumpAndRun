package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Verhindert das Wegwerfen oder Verschieben des Barrier-Items
 * ("» Aufgeben & Welt verlassen «") in JumpAndRun-Welten.
 */
public class BarrierProtectionListener implements Listener {

    private boolean isBarrierItem(ItemStack item) {
        if (item == null || item.getType() != Material.BARRIER) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name != null && name.contains("Aufgeben");
    }

    /** 🔹 Verhindert Wegwerfen mit Q */
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();

        if (WorldRepository.exists(worldName)) {
            ItemStack dropped = event.getItemDrop().getItemStack();
            if (isBarrierItem(dropped)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.GRAY + "Dieses Item kannst du hier nicht wegwerfen.");
            }
        }
    }

    /** 🔹 Verhindert Verschieben im Inventar */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String worldName = player.getWorld().getName();

        if (WorldRepository.exists(worldName)) {
            ItemStack clicked = event.getCurrentItem();
            if (isBarrierItem(clicked)) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
    }

    /** 🔹 Verhindert Drag-and-Drop von Barrier-Item */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String worldName = player.getWorld().getName();

        if (WorldRepository.exists(worldName)) {
            for (ItemStack item : event.getNewItems().values()) {
                if (isBarrierItem(item)) {
                    event.setCancelled(true);
                    player.updateInventory();
                    break;
                }
            }
        }
    }
}
