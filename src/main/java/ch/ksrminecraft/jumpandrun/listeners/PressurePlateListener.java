package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.CheckpointRepository;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.TestRunManager;
import ch.ksrminecraft.jumpandrun.utils.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener, der Spielerinteraktionen mit Druckplatten überwacht und basierend auf
 * dem Material den Start, Checkpoints und das Ende eines Runs registriert.
 */
public class PressurePlateListener implements Listener {

    /** Letzter Checkpoint pro Spieler (UUID). */
    private static final Map<UUID, Location> lastCheckpoints = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) return;

        String worldName = clickedBlock.getWorld().getName();
        Material blockType = clickedBlock.getType();

        // Materialien aus Config laden
        Material startPlate = JumpAndRun.getConfigManager().getStartPlate();
        Material endPlate = JumpAndRun.getConfigManager().getEndPlate();
        Material checkpointPlate = JumpAndRun.getConfigManager().getCheckpointPlate();

        // Startdruckplatte → Zeitmessung starten
        if (blockType == startPlate) {
            TimeManager.inputStartTime(clickedBlock.getWorld(), player);
            debug(player.getName() + " hat die Startdruckplatte in Welt " + worldName + " betreten.");
        }

        // Checkpointdruckplatte → Spieler-Checkpoint setzen
        else if (blockType == checkpointPlate) {
            Location cp = clickedBlock.getLocation();
            lastCheckpoints.put(player.getUniqueId(), cp);

            // Wenn Welt im Draft: Checkpoint auch in DB speichern
            if (!WorldRepository.isPublished(worldName)) {
                int idx = CheckpointRepository.getNextIndex(worldName);
                CheckpointRepository.addCheckpoint(worldName, idx, cp);
            }

            player.sendMessage("§eCheckpoint erreicht!");
            debug(player.getName() + " hat einen Checkpoint in Welt " + worldName + " erreicht.");
        }

        // Zieldruckplatte → Zeitmessung beenden
        else if (blockType == endPlate) {
            if (TestRunManager.isTesting(player)) {
                // Ersteller beendet seinen Test → Welt wird veröffentlicht
                TestRunManager.completeTest(player);
                debug("Ersteller " + player.getName() + " hat seinen Test-Run in Welt " + worldName + " abgeschlossen → Welt veröffentlicht.");
            } else {
                if (WorldRepository.isPublished(worldName)) {
                    TimeManager.calcTime(clickedBlock.getWorld(), player);
                    debug(player.getName() + " hat die Zieldruckplatte in Welt " + worldName + " betreten (Run-Zeit gespeichert).");
                } else {
                    player.sendMessage("§cDieses JumpAndRun ist noch im Draft-Modus und nicht spielbar.");
                }
            }
        }
    }

    /**
     * Liefert den zuletzt gespeicherten Checkpoint eines Spielers zurück.
     */
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
