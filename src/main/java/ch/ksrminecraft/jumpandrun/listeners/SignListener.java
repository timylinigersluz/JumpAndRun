package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.SignUpdater;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener für JumpAndRun-Schilder.
 * Unterstützt:
 *  - [JNR] <alias> → Start-Schild (Teleport zum Startpunkt)
 *  - [JNR-LEADER] <alias> → Leaderboard-Schild
 */
public class SignListener implements Listener {

    // ---------------------------------------------
    //  Erstellung von Schildern
    // ---------------------------------------------
    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String l0 = (event.getLine(0) == null ? "" : event.getLine(0)).trim();
        String l1 = (event.getLine(1) == null ? "" : event.getLine(1)).trim();

        // === [JNR] Start-Schild ===
        if (l0.equalsIgnoreCase("[JNR]")) {
            if (!player.hasPermission("jumpandrun.sign.create")) {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, JumpAndRun-Schilder zu erstellen.");
                event.setCancelled(true);
                return;
            }

            if (l1.isEmpty()) {
                event.setLine(0, ChatColor.RED + "[JNR]");
                event.setLine(1, ChatColor.RED + "Alias fehlt");
                event.setLine(2, ChatColor.GRAY + "Zeile 2: Alias");
                event.setLine(3, "");
                player.sendMessage(ChatColor.RED + "Bitte gib in Zeile 2 den Alias an.");
                return;
            }

            event.setLine(0, ChatColor.GREEN + "[JNR]");
            event.setLine(1, ChatColor.AQUA + "" + ChatColor.ITALIC + l1);
            event.setLine(2, ChatColor.GOLD + "» " + ChatColor.BOLD + "START" + ChatColor.GOLD + " «");
            event.setLine(3, ChatColor.GRAY + "Klicke zum Spielen");
            player.sendMessage(ChatColor.GREEN + "Start-Schild für '" + l1 + "' erstellt.");
            return;
        }

        // === [JNR-LEADER] Leaderboard-Schild ===
        if (l0.equalsIgnoreCase("[JNR-LEADER]")) {
            if (!player.hasPermission("jumpandrun.sign.leader")) {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, Leader-Schilder zu erstellen.");
                event.setCancelled(true);
                return;
            }

            if (l1.isEmpty()) {
                event.setLine(0, ChatColor.DARK_RED + "[JNR-LEADER]");
                event.setLine(1, ChatColor.RED + "Alias fehlt");
                event.setLine(2, ChatColor.GRAY + "Zeile 2: Alias");
                event.setLine(3, "");
                player.sendMessage(ChatColor.RED + "Bitte gib in Zeile 2 den Alias an.");
                return;
            }

            String worldName = WorldRepository.getWorldByAlias(l1);
            if (worldName == null) {
                event.setLine(0, ChatColor.DARK_RED + "[JNR-LEADER]");
                event.setLine(1, ChatColor.RED + "Unbekannter Alias");
                event.setLine(2, ChatColor.GRAY + l1);
                event.setLine(3, ChatColor.GRAY + "Alias prüfen");
                player.sendMessage(ChatColor.RED + "Alias '" + l1 + "' existiert nicht.");
                return;
            }

            event.setLine(0, ChatColor.DARK_BLUE + "[JNR-LEADER]");
            event.setLine(1, ChatColor.AQUA + l1);
            event.setLine(2, ChatColor.GRAY + "Wird aktualisiert…");
            event.setLine(3, "");

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (event.getBlock().getState() instanceof Sign sign) {
                        SignUpdater.registerLeaderSign(l1, sign.getLocation());
                        SignUpdater.updateLeaderSigns(l1);
                    }
                }
            }.runTaskLater(JumpAndRun.getPlugin(), 1L);

            player.sendMessage(ChatColor.GREEN + "Leader-Schild für '" + l1 + "' erstellt.");
        }
    }

    // ---------------------------------------------
    //  Klick auf ein JumpAndRun-Schild
    // ---------------------------------------------
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        // 🚫 Creative-Modus: Nichts tun (Bearbeiten erlaubt)
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        String line0 = ChatColor.stripColor(sign.getLine(0));
        String alias = ChatColor.stripColor(sign.getLine(1));
        if (line0 == null || alias == null) return;

        // === [JNR] Start-Schild ===
        if (line0.equalsIgnoreCase("[JNR]")) {
            String worldName = WorldRepository.getWorldByAlias(alias);
            if (worldName == null || !WorldRepository.isPublished(worldName)) {
                player.sendMessage("§cDieses JumpAndRun ist nicht verfügbar.");
                event.setCancelled(true);
                return;
            }

            Location start = WorldRepository.getStartLocation(worldName);
            if (start == null) {
                player.sendMessage("§cStartposition konnte nicht gefunden werden.");
                event.setCancelled(true);
                return;
            }

            Location tpLoc = start.clone().add(0, JumpAndRun.height + 1, 0);
            tpLoc.setYaw(-90f);
            tpLoc.setPitch(0f);

            player.teleport(tpLoc);
            player.sendMessage("§aTeleportiert zum Start von §e" + alias);
            event.setCancelled(true);
        }
    }

    // ---------------------------------------------
    //  Entfernen von Start- und Leader-Signs
    // ---------------------------------------------
    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getBlock().getState();
        String line0 = ChatColor.stripColor(sign.getLine(0));
        String alias = ChatColor.stripColor(sign.getLine(1));
        if (line0 == null || alias == null) return;

        Player player = event.getPlayer();

        // --- Schutz für [JNR] Start-Schilder ---
        if (line0.equalsIgnoreCase("[JNR]")) {
            if (!player.hasPermission("jumpandrun.sign.create")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Du darfst keine JumpAndRun-Startschilder entfernen!");
                return;
            }
            player.sendMessage(ChatColor.GRAY + "Start-Schild für '" + alias + "' wurde entfernt.");
        }

        // --- Schutz für [JNR-LEADER] Leader-Schilder ---
        if (line0.equalsIgnoreCase("[JNR-LEADER]")) {
            if (!player.hasPermission("jumpandrun.sign.leader")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Du darfst keine Leader-Schilder entfernen!");
                return;
            }
            SignUpdater.unregisterLeaderSign(alias, event.getBlock().getLocation());
            player.sendMessage(ChatColor.GRAY + "Leader-Schild für '" + alias + "' wurde entfernt.");
        }
    }
}
