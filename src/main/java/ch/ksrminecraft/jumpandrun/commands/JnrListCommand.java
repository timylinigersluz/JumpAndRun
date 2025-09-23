package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.TimeRepository;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

/**
 * Subcommand: /jnr list
 * Listet JumpAndRun-Welten mit Leader und Bestzeit.
 * Normale Spieler sehen nur veröffentlichte Welten,
 * Spieler mit Permission "jumpandrun.drafts" oder im Debugmodus auch Drafts.
 */
public class JnrListCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> worlds = WorldRepository.getAllWorlds();

        if (worlds.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Es sind keine JumpAndRun-Welten registriert.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "===== JumpAndRun-Welten =====");

        boolean showDrafts = JumpAndRun.getConfigManager().isDebug()
                || sender.hasPermission("jumpandrun.drafts");

        for (String worldName : worlds) {
            boolean published = WorldRepository.isPublished(worldName);

            // Drafts ausblenden
            if (!published && !showDrafts) {
                continue;
            }

            // Bestzeit und Leader aus DB holen
            Long bestTime = TimeRepository.getBestTime(worldName);
            String leaderUUID = TimeRepository.getLeader(worldName);

            // Leadername bestimmen
            String leaderName = "—";
            if (leaderUUID != null) {
                try {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(leaderUUID));
                    if (offlinePlayer != null && offlinePlayer.getName() != null) {
                        leaderName = offlinePlayer.getName();
                    } else {
                        leaderName = leaderUUID.substring(0, 8); // Fallback: UUID-Kürzel
                    }
                } catch (IllegalArgumentException e) {
                    leaderName = "Unbekannt";
                }
            }

            // Zeit formatieren
            String timeStr = (bestTime != null && bestTime > 0)
                    ? TimeRepository.formatTime(bestTime)
                    : "—";

            // Alias oder Weltname anzeigen
            String alias = WorldRepository.getAlias(worldName);
            String displayName = (alias != null && !alias.isEmpty()) ? alias : worldName;

            // Status farbig
            String status = published ? ChatColor.GREEN + " [LIVE]" : ChatColor.YELLOW + " [DRAFT]";

            // Konsolendebug (optional)
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] /jnr list → "
                        + "World=" + worldName
                        + " | Published=" + published
                        + " | Leader=" + leaderName
                        + " | Best=" + timeStr);
            }

            sender.sendMessage(ChatColor.AQUA + displayName + status +
                    ChatColor.GRAY + " | Leader: " + ChatColor.WHITE + leaderName +
                    ChatColor.GRAY + " | Zeit: " + ChatColor.WHITE + timeStr);
        }

        return true;
    }
}
