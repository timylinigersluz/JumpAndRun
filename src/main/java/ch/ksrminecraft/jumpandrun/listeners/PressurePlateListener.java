package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.CheckpointRepository;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.TestRunManager;
import ch.ksrminecraft.jumpandrun.utils.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener für Druckplatten: Start, Checkpoints, Ziel.
 */
public class PressurePlateListener implements Listener {

    private static final Map<UUID, Location> lastCheckpoints = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        String worldName = clickedBlock.getWorld().getName();
        Material blockType = clickedBlock.getType();

        Material startPlate = JumpAndRun.getConfigManager().getStartPlate();
        Material endPlate = JumpAndRun.getConfigManager().getEndPlate();
        Material checkpointPlate = JumpAndRun.getConfigManager().getCheckpointPlate();

        // Startdruckplatte → Zeit starten
        if (blockType == startPlate) {
            TimeManager.inputStartTime(clickedBlock.getWorld(), player);
            player.sendMessage(ChatColor.YELLOW + "Dein Lauf hat begonnen!");
            debug(player.getName() + " hat die Startdruckplatte in Welt " + worldName + " betreten.");
        }

        // Checkpoint
        else if (blockType == checkpointPlate) {
            Location cp = clickedBlock.getLocation();
            lastCheckpoints.put(player.getUniqueId(), cp);

            if (!WorldRepository.isPublished(worldName)) {
                int idx = CheckpointRepository.getNextIndex(worldName);
                CheckpointRepository.addCheckpoint(worldName, idx, cp);
            }

            player.sendMessage(ChatColor.GOLD + "Checkpoint erreicht!");
            debug(player.getName() + " hat einen Checkpoint in Welt " + worldName + " erreicht.");
        }

        // Zieldruckplatte
        else if (blockType == endPlate) {
            if (TestRunManager.isTesting(player)) {
                // Herkunfts-Location laden
                Location origin = WorldSwitchListener.getOrigin(player);
                if (origin != null) {
                    player.teleport(origin);
                    WorldSwitchListener.clearOrigin(player);
                } else {
                    // Fallback Lobby-Spawn
                    player.teleport(Bukkit.getWorld("world").getSpawnLocation());
                }

                // Meldungen
                player.sendMessage(ChatColor.GREEN + "✔ Test erfolgreich abgeschlossen!");
                player.sendMessage(ChatColor.AQUA + "Das JumpAndRun ist jetzt startklar.");
                player.sendMessage(ChatColor.YELLOW + "Platziere ein Schild, um es öffentlich zugänglich zu machen.");

                // Schild ins Inventar
                player.getInventory().addItem(new ItemStack(Material.OAK_SIGN, 1));

                TestRunManager.completeTest(player);
                debug("Ersteller " + player.getName() + " hat seinen Test-Run abgeschlossen.");
            } else {
                if (WorldRepository.isPublished(worldName)) {
                    TimeManager.calcTime(clickedBlock.getWorld(), player);
                    debug(player.getName() + " hat die Zieldruckplatte in Welt " + worldName + " betreten (Zeit gespeichert).");
                } else {
                    player.sendMessage(ChatColor.RED + "Dieses JumpAndRun ist noch nicht veröffentlicht.");
                }
            }
        }
    }

    public static Location getLastCheckpoint(Player player) {
        return lastCheckpoints.get(player.getUniqueId());
    }

    public static void clearCheckpoint(UUID uuid) {
        lastCheckpoints.remove(uuid);
    }

    private void debug(String msg) {
        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] " + msg);
        }
    }
}
