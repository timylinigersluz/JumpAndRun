package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Merkt sich, aus welcher Welt/Position ein Spieler kommt,
 * wenn er in ein JumpAndRun wechselt, um später zurückzuteleportieren.
 * Stoppt außerdem laufende StopWatches, wenn Spieler die Welt verlässt.
 * Gibt beim Betreten einer JnR-Welt ein "Aufgeben"-Item.
 */
public class WorldSwitchListener implements Listener {

    private static final Map<UUID, Location> originLocations = new HashMap<>();

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String newWorld = player.getWorld().getName();
        String oldWorld = event.getFrom().getName();

        // === Alte Welt verlassen ===
        if (WorldRepository.exists(oldWorld) && !WorldRepository.exists(newWorld)) {
            // Spieler verlässt eine JnR-Welt -> StopWatch stoppen
            TimeManager.stopWatch(player);
            if (Bukkit.getConsoleSender() != null) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] StopWatch von "
                        + player.getName() + " beim Verlassen der Welt " + oldWorld + " gestoppt.");
            }
        }

        // === Neue Welt betreten ===
        if (WorldRepository.exists(newWorld)) {
            Location fromSpawn = event.getFrom().getSpawnLocation();
            originLocations.put(player.getUniqueId(), fromSpawn.clone());

            // Inventar leeren und Aufgeben-Item setzen
            giveLeaveItem(player);

            if (Bukkit.getConsoleSender() != null) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler " + player.getName()
                        + " wechselt von " + oldWorld
                        + " nach " + newWorld + " → Herkunft gespeichert: "
                        + formatLocation(fromSpawn));
            }
        }
    }

    /** Herkunft explizit setzen (z. B. aus Commands heraus). */
    public static void setOrigin(Player player, Location loc) {
        if (loc != null) {
            originLocations.put(player.getUniqueId(), loc.clone());
        }
    }

    /** Holt die gespeicherte Herkunfts-Location für einen Spieler. */
    public static Location getOrigin(Player player) {
        return originLocations.get(player.getUniqueId());
    }

    /** Entfernt gespeicherte Herkunft (z. B. nach Teleport zurück). */
    public static void clearOrigin(Player player) {
        originLocations.remove(player.getUniqueId());
    }

    private static String formatLocation(Location loc) {
        return String.format("(%s | x=%.1f, y=%.1f, z=%.1f)",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ());
    }

    /** Gibt dem Spieler ein "Aufgeben"-Item in den letzten Hotbar-Slot. */
    private void giveLeaveItem(Player player) {
        player.getInventory().clear();

        ItemStack leaveItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = leaveItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "» Aufgeben & Welt verlassen «");
            leaveItem.setItemMeta(meta);
        }

        player.getInventory().setItem(8, leaveItem); // Slot 8 = letzter Hotbar-Slot
        player.updateInventory();
    }
}
