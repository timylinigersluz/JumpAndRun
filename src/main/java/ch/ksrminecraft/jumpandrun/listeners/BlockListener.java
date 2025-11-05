package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Verhindert Veränderungen in veröffentlichten JumpAndRun-Welten.
 * Mit 3 Sekunden Cooldown für Chat-Meldungen.
 */
public class BlockListener implements Listener {

    private static final long MESSAGE_COOLDOWN = 3000; // 3 Sekunden
    private final Map<UUID, Long> lastMessage = new HashMap<>();

    /** Prüft, ob Welt existiert und veröffentlicht ist. */
    private boolean isProtectedWorld(String worldName) {
        return WorldRepository.exists(worldName) && WorldRepository.isPublished(worldName);
    }

    private void sendCooldownMessage(Player player, String message) {
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();

        if (!lastMessage.containsKey(uuid) || (now - lastMessage.get(uuid) > MESSAGE_COOLDOWN)) {
            player.sendMessage(ChatColor.RED + message);
            lastMessage.put(uuid, now);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        String worldName = event.getBlock().getWorld().getName();
        if (!WorldRepository.exists(worldName)) return; // ✅ Nur in JnR-Welten prüfen
        if (isProtectedWorld(worldName)) {
            event.setCancelled(true);
            sendCooldownMessage(event.getPlayer(), "Du darfst hier keine Blöcke abbauen!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        String worldName = event.getBlock().getWorld().getName();
        if (!WorldRepository.exists(worldName)) return;
        if (isProtectedWorld(worldName)) {
            event.setCancelled(true);
            sendCooldownMessage(event.getPlayer(), "Du darfst hier keine Blöcke platzieren!");
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        String worldName = event.getBlock().getWorld().getName();
        if (!WorldRepository.exists(worldName)) return;
        if (isProtectedWorld(worldName)) {
            event.setCancelled(true);
            sendCooldownMessage(event.getPlayer(), "Du darfst hier keine Flüssigkeiten platzieren!");
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        String worldName = event.getBlock().getWorld().getName();
        if (!WorldRepository.exists(worldName)) return;
        if (isProtectedWorld(worldName)) {
            event.setCancelled(true);
            sendCooldownMessage(event.getPlayer(), "Du darfst hier keine Flüssigkeiten entnehmen!");
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getBlock() == null || event.getBlock().getWorld() == null) return;
        String worldName = event.getBlock().getWorld().getName();
        if (!WorldRepository.exists(worldName)) return;
        if (isProtectedWorld(worldName)) {
            event.setCancelled(true);
            if (event.getPlayer() != null) {
                sendCooldownMessage(event.getPlayer(), "Feuer ist hier deaktiviert!");
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getLocation().getWorld() == null) return;
        String worldName = event.getLocation().getWorld().getName();
        if (!WorldRepository.exists(worldName)) return;
        if (isProtectedWorld(worldName)) {
            event.blockList().clear();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (event.getBlock() == null || event.getBlock().getWorld() == null) return;
        String worldName = event.getBlock().getWorld().getName();
        if (!WorldRepository.exists(worldName)) return;
        if (isProtectedWorld(worldName)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.getBlock() == null || event.getBlock().getWorld() == null) return;
        String worldName = event.getBlock().getWorld().getName();
        if (!WorldRepository.exists(worldName)) return;
        if (isProtectedWorld(worldName)) {
            event.setCancelled(true);
        }
    }
}
