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

        // Alten Zustand prüfen (war das Schild vorher geschützt?)
        String oldHeader = "";
        if (block.getState() instanceof Sign oldSign) {
            oldHeader = ChatColor.stripColor(oldSign.getLine(0));
        }

        // A) Bearbeiten eines BESTEHENDEN geschützten Schilds → nur Creative + Permission
        if (isProtectedSignType(oldHeader)) {
            if (!player.hasPermission(PERM_SIGN_INTERACT)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "❌ Du darfst dieses JumpAndRun-Schild nicht verändern.");
                debug("→ Blockiert: " + player.getName() + " versuchte, ein bestehendes " + oldHeader + "-Schild zu verändern.");
                return;
            }
            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "❌ JumpAndRun-Schilder dürfen nur im Creative-Modus bearbeitet werden.");
                debug("→ Blockiert: " + player.getName() + " ist im falschen Modus (" + player.getGameMode() + ") beim Bearbeiten von " + oldHeader);
                return;
            }
            // Ab hier: Bearbeiten ist erlaubt (Creative + Permission)
        }

        // B) NEU setzen (oder Konvertieren eines normalen Schilds zu [JNR]/[JNR-LEADER])
        if (newHeader.equalsIgnoreCase("[JNR]") || newHeader.equalsIgnoreCase("[JNR-LEADER]")) {
            debug(player.getName() + " erstellt/ändert ein Schild mit Kopfzeile '" + newHeader + "'.");

            // NEU setzen: nur Permission nötig (Gamemode egal)
            if (!player.hasPermission(PERM_SIGN_INTERACT)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "❌ Du darfst keine JumpAndRun-Schilder erstellen.");
                debug("→ Abgebrochen: " + player.getName() + " hat keine Permission (" + PERM_SIGN_INTERACT + ")");
                return;
            }

            // === [JNR] Start-Schild ===
            if (newHeader.equalsIgnoreCase("[JNR]")) {
                if (newAlias.isEmpty()) {
                    event.setLine(0, ChatColor.RED + "[JNR]");
                    event.setLine(1, ChatColor.RED + "Alias fehlt");
                    event.setLine(2, ChatColor.GRAY + "Zeile 2: Alias");
                    player.sendMessage(ChatColor.RED + "Bitte gib in Zeile 2 den Alias an.");
                    debug("→ Alias fehlt beim Erstellen von [JNR]-Schild durch " + player.getName());
                    return;
                }

                event.setLine(0, ChatColor.GREEN + "[JNR]");
                event.setLine(1, ChatColor.AQUA + "" + ChatColor.ITALIC + newAlias);
                event.setLine(2, ChatColor.GOLD + "» " + ChatColor.BOLD + "START" + ChatColor.GOLD + " «");
                event.setLine(3, ChatColor.GRAY + "Klicke zum Spielen");

                player.sendMessage(ChatColor.GREEN + "Start-Schild für '" + newAlias + "' erstellt.");
                debug("→ [JNR]-Schild erfolgreich erstellt für Alias '" + newAlias + "'");
                return;
            }

            // === [JNR-LEADER] Leaderboard-Schild ===
            if (newHeader.equalsIgnoreCase("[JNR-LEADER]")) {
                if (newAlias.isEmpty()) {
                    event.setLine(0, ChatColor.DARK_RED + "[JNR-LEADER]");
                    event.setLine(1, ChatColor.RED + "Alias fehlt");
                    event.setLine(2, ChatColor.GRAY + "Zeile 2: Alias");
                    player.sendMessage(ChatColor.RED + "Bitte gib in Zeile 2 den Alias an.");
                    debug("→ Alias fehlt beim Erstellen von [JNR-LEADER]-Schild durch " + player.getName());
                    return;
                }

                String worldName = WorldRepository.getWorldByAlias(newAlias);
                if (worldName == null) {
                    event.setLine(0, ChatColor.DARK_RED + "[JNR-LEADER]");
                    event.setLine(1, ChatColor.RED + "Unbekannter Alias");
                    event.setLine(2, ChatColor.GRAY + newAlias);
                    event.setLine(3, ChatColor.GRAY + "Alias prüfen");
                    player.sendMessage(ChatColor.RED + "Alias '" + newAlias + "' existiert nicht.");
                    debug("→ Fehler: Kein Weltname für Alias '" + newAlias + "'");
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
                            debug("→ Leader-Schild registriert & aktualisiert für '" + newAlias + "'");
                        }
                    }
                }.runTaskLater(JumpAndRun.getPlugin(), 1L);

                player.sendMessage(ChatColor.GREEN + "Leader-Schild für '" + newAlias + "' erstellt.");
                debug("→ [JNR-LEADER]-Schild erfolgreich erstellt für '" + newAlias + "'");
            }
        }
    }

    // ------------------------------------------------------------
    // 2) Klick: Teleport über [JNR]
    // ------------------------------------------------------------
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getState() instanceof Sign sign)) return;

        Player player = event.getPlayer();
        String line0 = ChatColor.stripColor(sign.getLine(0));
        String alias = ChatColor.stripColor(sign.getLine(1));
        if (line0 == null || alias == null) return;

        if (line0.equalsIgnoreCase("[JNR]")) {
            debug(player.getName() + " klickt auf [JNR]-Schild (Alias: " + alias + ")");

            String worldName = WorldRepository.getWorldByAlias(alias);
            if (worldName == null || !WorldRepository.isPublished(worldName)) {
                player.sendMessage("§cDieses JumpAndRun ist nicht verfügbar.");
                debug("→ Kein gültiges Ziel: Welt '" + worldName + "' nicht veröffentlicht oder unbekannt.");
                event.setCancelled(true);
                return;
            }

            Location start = WorldRepository.getStartLocation(worldName);
            if (start == null) {
                player.sendMessage("§cStartposition konnte nicht gefunden werden.");
                debug("→ Startposition für '" + worldName + "' nicht gefunden.");
                event.setCancelled(true);
                return;
            }

            Location tpLoc = start.clone().add(0, JumpAndRun.height + 1, 0);
            tpLoc.setYaw(-90f);
            tpLoc.setPitch(0f);

            player.teleport(tpLoc);
            player.sendMessage("§aTeleportiert zum Start von §e" + alias);
            debug("→ " + player.getName() + " wurde zu '" + worldName + "' teleportiert.");
            event.setCancelled(true);
        }
    }

    // ------------------------------------------------------------
    // 3) Abbauen geschützt
    // ------------------------------------------------------------
    @EventHandler(ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof Sign sign)) return;

        Player player = event.getPlayer();
        String header = ChatColor.stripColor(sign.getLine(0));
        String alias  = ChatColor.stripColor(sign.getLine(1));

        if (isProtectedSignType(header)) {
            debug(player.getName() + " versucht, ein " + header + "-Schild (Alias: " + alias + ") abzubauen.");

            if (!player.hasPermission(PERM_SIGN_INTERACT) || player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "❌ Du darfst dieses JumpAndRun-Schild nicht abbauen.");
                debug("→ Blockiert: " + player.getName() + " hat keine Rechte oder ist nicht im Creative-Modus (" + player.getGameMode() + ")");
                return;
            }

            // Entfernen erlaubt
            SignUpdater.unregisterLeaderSign(alias, block.getLocation());
            player.sendMessage(ChatColor.GRAY + "Schild für '" + alias + "' wurde entfernt.");
            debug("→ Schild für '" + alias + "' erfolgreich entfernt durch " + player.getName());
        }
    }

    // ------------------------------------------------------------
    // Hilfsmethoden
    // ------------------------------------------------------------
    private boolean isProtectedSignType(String text) {
        return text != null &&
                (text.equalsIgnoreCase("[JNR]") || text.equalsIgnoreCase("[JNR-LEADER]"));
    }

    private String safeLine(String line) {
        return (line == null) ? "" : ChatColor.stripColor(line.trim());
    }
}
