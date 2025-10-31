package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.SignUpdater;
import ch.ksrminecraft.jumpandrun.utils.TimeManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            player.setVelocity(new Vector(0, 0, 0));
            player.setFallDistance(0);
            player.setNoDamageTicks(20);

            if (!skipClear.getOrDefault(player.getUniqueId(), false)) {
                player.getInventory().clear();
                player.updateInventory();
            } else {
                skipClear.remove(player.getUniqueId());
            }

            PressurePlateListener.clearCheckpoint(player.getUniqueId());

            if (!WorldRepository.isPublished(oldWorld)) {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(ChatColor.GRAY + "Dein Entwurf wurde verlassen – du bist wieder im Überlebensmodus.");
            }

            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] " + player.getName()
                    + " hat Welt " + oldWorld + " verlassen (StopWatch gestoppt, Checkpoint gelöscht).");
        }

        // --- Falls neue Welt keine JnR-Welt ist (z. B. nach Weltlöschung) ---
        if (!WorldRepository.exists(newWorld)) {
            String fallbackWorldName = JumpAndRun.getConfigManager().getFallbackWorld();
            World fallback = Bukkit.getWorld(fallbackWorldName);
            if (fallback != null) {
                player.teleport(fallback.getSpawnLocation());
                player.setGameMode(GameMode.SURVIVAL);
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler " + player.getName()
                        + " wurde nach Weltlöschung in Fallback-Welt teleportiert.");
            }
            return;
        }

        // --- Neue Welt betreten ---
        Location fromSpawn = event.getFrom().getSpawnLocation();
        originLocations.put(player.getUniqueId(), fromSpawn.clone());

        giveLeaveItem(player);

        if (!WorldRepository.isPublished(newWorld)) {
            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage(ChatColor.AQUA + "Du bist jetzt im §lCreative-Modus §rfür deinen JumpAndRun-Entwurf.");
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }

        Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler " + player.getName()
                + " wechselt von " + oldWorld + " nach " + newWorld
                + " → Herkunft gespeichert: " + formatLocation(fromSpawn));

        Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), () -> {
            SignUpdater.loadAllFromDatabase();
            SignUpdater.updateLeaderSigns(newWorld);
        }, 40L);
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
