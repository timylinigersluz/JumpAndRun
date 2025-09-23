package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.CheckpointRepository;
import ch.ksrminecraft.jumpandrun.db.TimeRepository;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.PlayerUtils;
import ch.ksrminecraft.jumpandrun.utils.TestRunManager;
import ch.ksrminecraft.jumpandrun.utils.TimeManager;
import ch.ksrminecraft.jumpandrun.utils.SignUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener für Druckplatten: Start, Checkpoints, Ziel.
 */
public class PressurePlateListener implements Listener {

    private static final Map<UUID, Location> lastCheckpoints = new HashMap<>();

    // Cooldown-Speicher: (Spieler+Block) → Zeitstempel
    private static final Map<String, Long> plateCooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 3000; // 3 Sekunden Cooldown

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        String worldName = clickedBlock.getWorld().getName();
        Material blockType = clickedBlock.getType();

        // ==== Cooldown prüfen ====
        String key = player.getUniqueId() + ":" + clickedBlock.getLocation().toString();
        long now = System.currentTimeMillis();
        if (plateCooldowns.containsKey(key)) {
            long last = plateCooldowns.get(key);
            if (now - last < COOLDOWN_MS) {
                return; // Noch im Cooldown → keine Aktion
            }
        }
        plateCooldowns.put(key, now);

        // Plattentypen aus Config
        Material startPlate = JumpAndRun.getConfigManager().getStartPlate();
        Material endPlate = JumpAndRun.getConfigManager().getEndPlate();
        Material checkpointPlate = JumpAndRun.getConfigManager().getCheckpointPlate();

        // Startdruckplatte
        if (blockType == startPlate) {
            if (TestRunManager.isPrepared(player)) {
                PlayerUtils.resetState(player);
                TestRunManager.startTest(player);
                player.sendMessage(ChatColor.YELLOW + "Dein Testlauf hat begonnen!");
                debug(player.getName() + " hat die Startdruckplatte in Welt " + worldName + " betreten → Testlauf gestartet.");
            } else if (WorldRepository.isPublished(worldName)) {
                PlayerUtils.resetState(player);
                TimeManager.inputStartTime(clickedBlock.getWorld(), player);
                player.sendMessage(ChatColor.YELLOW + "Dein Lauf hat begonnen!");
                debug(player.getName() + " hat die Startdruckplatte in Welt " + worldName + " betreten (Run gestartet, Werte reset).");
            } else {
                player.sendMessage(ChatColor.RED + "Dieses JumpAndRun ist noch nicht bereit.");
            }
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
            if (TestRunManager.isTesting(player) && !WorldRepository.isPublished(worldName)) {
                // === READY-TESTRUN (nur wenn Welt noch nicht veröffentlicht ist) ===
                long duration = TimeManager.calcTime(clickedBlock.getWorld(), player);
                TestRunManager.completeTest(player);

                player.sendMessage(ChatColor.GREEN + "✔ Funktionstest erfolgreich abgeschlossen!");
                player.sendMessage(ChatColor.AQUA + "Bitte vergebe deiner Welt noch einen Alias.");
                ch.ksrminecraft.jumpandrun.utils.AliasPromptManager.awaitAliasInput(player, worldName);

                debug("Funktionstest abgeschlossen (Spieler=" + player.getName() + ", Dauer=" + duration + "ms).");

            } else if (WorldRepository.isPublished(worldName)) {
                // === NORMALER MODUS ===
                long duration = TimeManager.calcTime(clickedBlock.getWorld(), player);

                teleportBack(player, worldName);

                if (duration > 0) {
                    player.sendMessage(ChatColor.GREEN + "✔ Lauf beendet in " + TimeRepository.formatTime(duration));

                    Long bestTime = TimeRepository.getBestTime(worldName);

                    if (bestTime == null) {
                        // Erster Lauf = automatisch Rekord
                        player.sendMessage(ChatColor.GOLD + "🏆 Erster Rekord!");

                        Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), () -> {
                            SignUpdater.updateLeaderSigns(worldName);
                        }, 1L);

                        triggerCelebration(player);
                        debug("Erster Rekord in Welt " + worldName + ": " + duration + "ms (Spieler=" + player.getName() + ")");
                    } else if (duration <= bestTime) {
                        // Neuer Rekord
                        player.sendMessage(ChatColor.GOLD + "🏆 Neuer Rekord!");

                        Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), () -> {
                            SignUpdater.updateLeaderSigns(worldName);
                        }, 1L);

                        triggerCelebration(player);
                        debug("Neuer Rekord in Welt " + worldName + ": " + duration + "ms (Spieler=" + player.getName() + ")");
                    } else {
                        debug("Kein Rekord: Spieler=" + player.getName()
                                + ", Zeit=" + duration + "ms, Beste=" + bestTime + "ms");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Fehler: Keine Startzeit gefunden.");
                }

                debug(player.getName() + " hat die Zieldruckplatte in Welt " + worldName
                        + " betreten (Dauer=" + duration + "ms).");

            } else {
                player.sendMessage(ChatColor.RED + "Dieses JumpAndRun ist noch nicht veröffentlicht.");
            }
        }
    }

    private void teleportBack(Player player, String worldName) {
        Location origin = WorldSwitchListener.getOrigin(player);
        if (origin != null) {
            player.teleport(origin);
            player.sendMessage(ChatColor.AQUA + "Du wurdest zurück zu deinem Startpunkt teleportiert.");
            return;
        }

        String fallbackWorldName = JumpAndRun.getConfigManager().getFallbackWorld();
        World fallbackWorld = Bukkit.getWorld(fallbackWorldName);
        if (fallbackWorld != null) {
            player.teleport(fallbackWorld.getSpawnLocation());
            player.sendMessage(ChatColor.AQUA + "Du wurdest zur Lobby zurückgebracht.");
            return;
        }

        Location start = WorldRepository.getStartLocation(worldName);
        if (start != null) {
            player.teleport(start.clone().add(0, JumpAndRun.height + 1, 0));
            player.sendMessage(ChatColor.AQUA + "Du wurdest zurück zum Start teleportiert.");
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

    /**
     * Feuerwerk-Serie bei Rekord
     */
    private void triggerCelebration(Player player) {
        for (int i = 0; i < 3; i++) {
            int delay = 20 * i; // alle 20 Ticks (=1 Sekunde)
            Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), () -> {
                spawnCelebrationFirework(player.getLocation());
            }, delay);
        }
    }

    /**
     * Großes Feuerwerk für Rekorde
     */
    private void spawnCelebrationFirework(Location loc) {
        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.AQUA, Color.LIME, Color.YELLOW)
                .withFade(Color.WHITE)
                .trail(true)
                .flicker(true)
                .build());
        meta.setPower(1);

        firework.setFireworkMeta(meta);

        // Sofort explodieren lassen
        Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), firework::detonate, 1L);
    }
}
