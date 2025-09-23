package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.TimeRepository;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.SignUpdater;
import ch.ksrminecraft.jumpandrun.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

/**
 * Listener für JumpAndRun-Schilder.
 * Unterstützt:
 * - [JNR] <alias> → Start-Schild (Teleport zum Startpunkt)
 * - [JNR-LEADER] <alias> → Leader-Schild (zeigt besten Spieler + Zeit)
 */
public class SignListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String firstLine = event.getLine(0);

        if (firstLine == null) return;

        // Start-Schild
        if (firstLine.equalsIgnoreCase("[JNR]")) {
            if (!player.hasPermission("jumpandrun.sign.create")) {
                player.sendMessage(ChatColor.RED + "Du darfst keine JnR-Schilder erstellen.");
                return;
            }

            String alias = event.getLine(1);
            if (alias == null || alias.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Bitte gib in Zeile 2 den Namen (Alias) an.");
                return;
            }

            String worldName = WorldRepository.getWorldByAlias(alias);
            if (worldName == null || !WorldRepository.isPublished(worldName)) {
                player.sendMessage(ChatColor.RED + "Dieses JumpAndRun existiert nicht oder ist nicht veröffentlicht.");
                return;
            }

            event.setLine(0, ChatColor.DARK_GREEN + "[JNR]");
            event.setLine(1, ChatColor.AQUA + alias);
            event.setLine(2, ChatColor.YELLOW + ">> Start <<");
            event.setLine(3, ChatColor.GRAY + "Klicke zum Spielen");

            player.sendMessage(ChatColor.GREEN + "Start-Schild für " + alias + " erstellt.");
        }

        // Leader-Schild
        else if (firstLine.equalsIgnoreCase("[JNR-LEADER]")) {
            if (!player.hasPermission("jumpandrun.sign.leader")) {
                player.sendMessage(ChatColor.RED + "Du darfst keine Leader-Schilder erstellen");
                return;
            }

            String alias = event.getLine(1);
            if (alias == null || alias.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Bitte gib in Zeile 2 den Namen (Alias) an.");
                return;
            }

            String worldName = WorldRepository.getWorldByAlias(alias);
            if (worldName == null || !WorldRepository.isPublished(worldName)) {
                player.sendMessage(ChatColor.RED + "Dieses JumpAndRun existiert nicht oder ist nicht veröffentlicht.");
                return;
            }

            // Leader-Infos laden
            Long bestTime = TimeRepository.getBestTime(worldName);
            String leader = TimeRepository.getLeader(worldName);
            String leaderName = (leader != null) ? Bukkit.getOfflinePlayer(UUID.fromString(leader)).getName() : "—";
            String timeStr = (bestTime != null) ? TimeUtils.formatMs(bestTime) : "—";

            event.setLine(0, ChatColor.DARK_BLUE + "[JNR-LEADER]");
            event.setLine(1, ChatColor.AQUA + alias);
            event.setLine(2, ChatColor.YELLOW + (leaderName != null ? leaderName : "—"));
            event.setLine(3, ChatColor.GRAY + timeStr);

            // Schild registrieren für spätere Updates
            SignUpdater.registerLeaderSign(worldName, event.getBlock().getLocation());

            player.sendMessage(ChatColor.GREEN + "Leader-Schild für " + alias + " erstellt.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        String line0 = ChatColor.stripColor(sign.getLine(0));
        String alias = ChatColor.stripColor(sign.getLine(1));

        if (line0 == null || alias == null) return;

        Player player = event.getPlayer();

        // [JNR] Start-Schild
        if (line0.equalsIgnoreCase("[JNR]")) {
            if (!player.hasPermission("jumpandrun.sign.use")) {
                player.sendMessage("§cDu darfst dieses Schild nicht benutzen.");
                return;
            }

            String worldName = WorldRepository.getWorldByAlias(alias);
            if (worldName == null || !WorldRepository.isPublished(worldName)) {
                player.sendMessage("§cDieses JumpAndRun ist nicht verfügbar.");
                return;
            }

            Location start = WorldRepository.getStartLocation(worldName);
            if (start == null) {
                player.sendMessage("§cStartposition konnte nicht gefunden werden.");
                return;
            }

            player.teleport(start.clone().add(0, JumpAndRun.height + 1, 0));
            player.sendMessage("§aTeleportiert zum Start von " + alias);

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler " + player.getName() +
                        " hat Start-Schild von Welt " + worldName + " benutzt (Alias=" + alias + ").");
            }
        }
    }
}
