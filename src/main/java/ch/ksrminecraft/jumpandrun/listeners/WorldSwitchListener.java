package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.SignUpdater;
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
    private static final Map<UUID, Boolean> skipClear = new HashMap<>();

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String newWorld = player.getWorld().getName();
        String oldWorld = event.getFrom().getName();

        // --- Alte Welt verlassen ---
        if (WorldRepository.exists(oldWorld) && !WorldRepository.exists(newWorld)) {
            TimeManager.stopWatch(player);

            if (!skipClear.getOrDefault(player.getUniqueId(), false)) {
                player.getInventory().clear();
                player.updateInventory();
            } else {
                skipClear.remove(player.getUniqueId());
            }

            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] StopWatch von "
                    + player.getName() + " beim Verlassen der Welt " + oldWorld + " gestoppt.");
        }

        // --- Neue Welt betreten ---
        if (WorldRepository.exists(newWorld)) {
            Location fromSpawn = event.getFrom().getSpawnLocation();
            originLocations.put(player.getUniqueId(), fromSpawn.clone());

            giveLeaveItem(player);

            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler " + player.getName()
                    + " wechselt von " + oldWorld
                    + " nach " + newWorld + " → Herkunft gespeichert: "
                    + formatLocation(fromSpawn));

            // 🔹 Nach kurzer Verzögerung Schilder aus DB laden & aktualisieren
            Bukkit.getScheduler().runTaskLater(ch.ksrminecraft.jumpandrun.JumpAndRun.getPlugin(), () -> {
                SignUpdater.loadAllFromDatabase();                 // Cache aktualisieren
                SignUpdater.updateLeaderSigns(newWorld);           // Schilder dieser Welt neu zeichnen
            }, 40L); // 2 Sekunden Delay – Welt vollständig geladen
        }
    }

    public static void setOrigin(Player player, Location loc) {
        if (loc != null) originLocations.put(player.getUniqueId(), loc.clone());
    }

    public static Location getOrigin(Player player) {
        return originLocations.get(player.getUniqueId());
    }

    public static void clearOrigin(Player player) {
        originLocations.remove(player.getUniqueId());
    }

    private static String formatLocation(Location loc) {
        return String.format("(%s | x=%.1f, y=%.1f, z=%.1f)",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ());
    }

    private void giveLeaveItem(Player player) {
        player.getInventory().clear();

        ItemStack leaveItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = leaveItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "» Aufgeben & Welt verlassen «");
            leaveItem.setItemMeta(meta);
        }

        player.getInventory().setItem(8, leaveItem);
        player.updateInventory();
    }

    public static void markSkipClear(Player player) {
        skipClear.put(player.getUniqueId(), true);
    }
}
