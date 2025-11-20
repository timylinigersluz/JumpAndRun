package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.SignUpdater;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener für JumpAndRun-Schilder.
 *
 * Regeln:
 *  - jumpandrun.sign.interact:
 *      • NEU setzen von [JNR]/[JNR-LEADER]: erlaubt in jedem Gamemode
 *      • Bearbeiten bestehender [JNR]/[JNR-LEADER]: nur im Creative
 *      • Abbauen von [JNR]/[JNR-LEADER]: nur im Creative
 *  - Spieler ohne Rechte:
 *      • dürfen [JNR] klicken (Teleport), aber nichts editieren/abbauen
 */
public class SignListener implements Listener {

    private static final String PERM_SIGN_INTERACT = "jumpandrun.sign.interact";

    private void debug(String msg) {
        if (JumpAndRun.getConfigManager() != null && JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getLogger().info("[JNR-DEBUG] " + msg);
        }
    }

    // ------------------------------------------------------------
    // 1) Erstellung & Bearbeitung
    // ------------------------------------------------------------
    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        String newHeader = safeLine(event.getLine(0));
        String newAlias  = safeLine(event.getLine(1));

        // Alten Zustand erfassen
        String oldHeader = "";
        String oldAlias  = "";
        if (block.getState() instanceof Sign oldSign) {
            oldHeader = ChatColor.stripColor(oldSign.getLine(0));
            oldAlias  = ChatColor.stripColor(oldSign.getLine(1));
        }

        // A) Geschütztes Schild bearbeiten → nur Creative & Permission
        if (isProtectedSignType(oldHeader, oldAlias)) {
            if (!player.hasPermission(PERM_SIGN_INTERACT)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "❌ Du darfst dieses JumpAndRun-Schild nicht verändern.");
                debug("→ Blockiert: " + player.getName() + " wollte ein " + oldHeader + "-Schild bearbeiten.");
                return;
            }
            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "❌ JumpAndRun-Schilder dürfen nur im Creative-Modus bearbeitet werden.");
                debug("→ Blockiert: " + player.getName() + " ist im falschen Modus (" + player.getGameMode() + ")");
                return;
            }
        }

        // B) Neues Schild: [JNR] oder [JNR-LEADER]
        if (newHeader.equalsIgnoreCase("[JNR]") || newHeader.equalsIgnoreCase("[JNR-LEADER]")) {
            debug(player.getName() + " erstellt/ändert ein Schild mit '" + newHeader + "'.");

            if (!player.hasPermission(PERM_SIGN_INTERACT)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "❌ Du darfst keine JumpAndRun-Schilder erstellen.");
                return;
            }

            // ===== [JNR] Start-Schild =====
            if (newHeader.equalsIgnoreCase("[JNR]")) {
                if (newAlias.isEmpty()) {
                    event.setLine(0, ChatColor.RED + "[JNR]");
                    event.setLine(1, ChatColor.RED + "Alias fehlt");
                    event.setLine(2, ChatColor.GRAY + "Zeile 2: Alias");
                    player.sendMessage(ChatColor.RED + "Bitte gib in Zeile 2 den Alias an.");
                    debug("→ Alias fehlt bei [JNR]-Erstellung.");
                    return;
                }

                event.setLine(0, ChatColor.GREEN + "[JNR]");
                event.setLine(1, ChatColor.AQUA + "" + ChatColor.ITALIC + newAlias);
                event.setLine(2, ChatColor.GOLD + "» " + ChatColor.BOLD + "START" + ChatColor.GOLD + " «");
                event.setLine(3, ChatColor.GRAY + "Klicke zum Spielen");

                player.sendMessage(ChatColor.GREEN + "Start-Schild für '" + newAlias + "' erstellt.");
                debug("→ [JNR]-Schild erstellt für Alias '" + newAlias + "'");
                return;
            }

            // ===== [JNR-LEADER] Leaderboard-Schild =====
            if (newHeader.equalsIgnoreCase("[JNR-LEADER]")) {
                if (newAlias.isEmpty()) {
                    event.setLine(0, ChatColor.DARK_RED + "[JNR-LEADER]");
                    event.setLine(1, ChatColor.RED + "Alias fehlt");
                    event.setLine(2, ChatColor.GRAY + "Zeile 2: Alias");
                    player.sendMessage(ChatColor.RED + "Bitte gib in Zeile 2 den Alias an.");
                    return;
                }

                String worldName = WorldRepository.getWorldByAlias(newAlias);
                if (worldName == null) {
                    event.setLine(0, ChatColor.DARK_RED + "[JNR-LEADER]");
                    event.setLine(1, ChatColor.RED + "Unbekannter Alias");
                    event.setLine(2, ChatColor.GRAY + newAlias);
                    event.setLine(3, ChatColor.GRAY + "Alias prüfen");
                    player.sendMessage(ChatColor.RED + "Alias '" + newAlias + "' existiert nicht.");
                    return;
                }

                event.setLine(0, ChatColor.DARK_BLUE + "[JNR-LEADER]");
                event.setLine(1, ChatColor.AQUA + newAlias);
                event.setLine(2, ChatColor.GRAY + "Wird aktualisiert…");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (event.getBlock().getState() instanceof Sign sign) {
                            SignUpdater.registerLeaderSign(newAlias, sign.getLocation());
                            SignUpdater.updateLeaderSigns(newAlias);
                        }
                    }
                }.runTaskLater(JumpAndRun.getPlugin(), 1L);

                player.sendMessage(ChatColor.GREEN + "Leader-Schild für '" + newAlias + "' erstellt.");
            }
        }
    }

    // ------------------------------------------------------------
    // 2) Klick: Teleport bei [JNR]
    // ------------------------------------------------------------
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getState() instanceof Sign sign)) return;

        Player player = event.getPlayer();

        // --------------------------------------------------------
        // Creative + Permission → NICHT interagieren, NICHT teleportieren
        // → Schild darf abgebaut werden
        // --------------------------------------------------------
        if (player.getGameMode() == GameMode.CREATIVE &&
                player.hasPermission(PERM_SIGN_INTERACT)) {
            return;
        }

        // Ab hier nur SURVIVAL-Interaktionen beachten
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        String header = ChatColor.stripColor(sign.getLine(0));
        String alias  = ChatColor.stripColor(sign.getLine(1));

        if (!"[JNR]".equalsIgnoreCase(header)) return;

        // ---- Teleport-Logik ----
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


    // ------------------------------------------------------------
    // 3) Abbauen (geschützt)
    // ------------------------------------------------------------
    @EventHandler(ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof Sign sign)) return;

        Player player = event.getPlayer();
        String header = ChatColor.stripColor(sign.getLine(0));
        String alias  = ChatColor.stripColor(sign.getLine(1));

        // Sonderfall: [JNR] ohne Alias → nie geschützt
        if (header.equalsIgnoreCase("[JNR]") && (alias == null || alias.isEmpty())) {
            return;
        }

        if (isProtectedSignType(header, alias)) {
            if (!player.hasPermission(PERM_SIGN_INTERACT) ||
                    player.getGameMode() != GameMode.CREATIVE) {

                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "❌ Du darfst dieses JumpAndRun-Schild nicht abbauen.");
                return;
            }

            // Nur Leader-Schilder deregistrieren
            if (header.equalsIgnoreCase("[JNR-LEADER]") && alias != null && !alias.isEmpty()) {
                SignUpdater.unregisterLeaderSign(alias, block.getLocation());
            }

            player.sendMessage("§7Schild entfernt.");
        }
    }

    // ------------------------------------------------------------
    // Hilfsmethoden
    // ------------------------------------------------------------
    private boolean isProtectedSignType(String header, String alias) {
        if (header == null) return false;

        boolean match = header.equalsIgnoreCase("[JNR]") ||
                header.equalsIgnoreCase("[JNR-LEADER]");

        if (!match) return false;

        // Nur echte Schilder schützen
        return alias != null && !alias.isEmpty();
    }

    private String safeLine(String line) {
        return (line == null) ? "" : ChatColor.stripColor(line.trim());
    }
}
