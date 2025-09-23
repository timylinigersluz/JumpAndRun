package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.SignUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listener für JumpAndRun-Schilder.
 * Unterstützt:
 * - [JNR] <alias> → Start-Schild (Teleport zum Startpunkt)
 * - [JNR-LEADER] <alias> → Leader-Schild (zeigt besten Spieler + Zeit)
 */
public class SignListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        final Player player = event.getPlayer();
        final String l0 = (event.getLine(0) == null ? "" : event.getLine(0)).trim();
        final String l1 = (event.getLine(1) == null ? "" : event.getLine(1)).trim();

        // --- Start-Schild: [JNR] <alias> ---
        if (l0.equalsIgnoreCase("[JNR]")) {
            if (l1.isEmpty()) {
                event.setLine(0, ChatColor.RED + "[JNR]");
                event.setLine(1, ChatColor.RED + "Alias fehlt");
                event.setLine(2, ChatColor.GRAY + "Zeile 2: Alias");
                event.setLine(3, "");
                player.sendMessage(ChatColor.RED + "Bitte gib in Zeile 2 den Alias an.");
                return;
            }

            // hübsch formatieren
            event.setLine(0, ChatColor.GREEN + "[JNR]");
            event.setLine(1, ChatColor.AQUA + "" + ChatColor.ITALIC + l1);
            event.setLine(2, ChatColor.GOLD + "» " + ChatColor.BOLD + "START" + ChatColor.GOLD + " «");
            event.setLine(3, ChatColor.GRAY + "Klicke zum Spielen");

            player.sendMessage(ChatColor.GREEN + "Start-Schild für '" + l1 + "' erstellt.");
            return;
        }

        // --- Leader-Schild: [JNR-LEADER] <alias> ---
        if (l0.equalsIgnoreCase("[JNR-LEADER]")) {
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
                // Alias unbekannt → Hinweis auf dem Schild
                event.setLine(0, ChatColor.DARK_RED + "[JNR-LEADER]");
                event.setLine(1, ChatColor.RED + "Unbekannter Alias");
                event.setLine(2, ChatColor.GRAY + l1);
                event.setLine(3, ChatColor.GRAY + "Alias prüfen");
                player.sendMessage(ChatColor.RED + "Alias '" + l1 + "' existiert nicht.");
                return;
            }

            // Temporäre Anzeige
            event.setLine(0, ChatColor.DARK_BLUE + "[JNR-LEADER]");
            event.setLine(1, ChatColor.AQUA + l1);
            event.setLine(2, ChatColor.GRAY + "Wird aktualisiert…");
            event.setLine(3, "");

            // Nach 1 Tick registrieren + updaten
            Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), () -> {
                try {
                    if (event.getBlock().getState() instanceof Sign sign) {
                        Location loc = sign.getLocation();
                        // Schild beim SignUpdater registrieren
                        SignUpdater.registerLeaderSign(worldName, loc);
                        // Schild-Inhalt aktualisieren
                        SignUpdater.updateLeaderSigns(worldName);
                    }
                } catch (Exception ex) {
                    player.sendMessage(ChatColor.RED + "Fehler beim Aktualisieren des Leader-Schilds.");
                    Bukkit.getConsoleSender().sendMessage("[JNR] Fehler bei Leader-Schild-Update: " + ex.getMessage());
                }
            }, 1L);

            player.sendMessage(ChatColor.GREEN + "Leader-Schild für '" + l1 + "' erstellt.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        String line0 = ChatColor.stripColor(sign.getLine(0));
        String alias = ChatColor.stripColor(sign.getLine(1));
        if (line0 == null || alias == null) return;

        Player player = event.getPlayer();

        // === [JNR] Start-Schild nutzen ===
        if (line0.equalsIgnoreCase("[JNR]")) {
            if (!player.hasPermission("jumpandrun.sign.use")) {
                player.sendMessage("§cDu darfst dieses Schild nicht benutzen.");
                event.setCancelled(true);
                return;
            }

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

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler " + player.getName() +
                        " hat ein JNR-Schild angeklickt (Alias=" + alias + ", Welt=" + worldName + ").");
            }

            // Teleport zum Startpunkt
            Location tpLoc = start.clone().add(0, JumpAndRun.height + 1, 0);

            // Blickrichtung fix auf Osten setzen
            tpLoc.setYaw(-90f);
            player.teleport(tpLoc);

            player.sendMessage("§aTeleportiert zum Start von " + alias);
            event.setCancelled(true);

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler " + player.getName() +
                        " wurde erfolgreich zum Start teleportiert (Welt=" + worldName + ").");
            }
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getBlock().getState();
        String line0 = ChatColor.stripColor(sign.getLine(0));
        String alias = ChatColor.stripColor(sign.getLine(1));
        if (line0 == null || alias == null) return;

        if (line0.equalsIgnoreCase("[JNR-LEADER]")) {
            String worldName = WorldRepository.getWorldByAlias(alias);
            if (worldName != null) {
                SignUpdater.unregisterLeaderSign(worldName, event.getBlock().getLocation());
            }
        }
    }
}
