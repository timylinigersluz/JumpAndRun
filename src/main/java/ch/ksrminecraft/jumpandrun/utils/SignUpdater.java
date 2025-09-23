package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.TimeRepository;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwalter für alle Leader-Schilder ([JNR-LEADER]).
 * Hält ihre Positionen im Speicher und aktualisiert sie bei neuen Rekorden.
 */
public class SignUpdater {

    /** Map<WorldName, Liste von Schild-Positionen> */
    private static final Map<String, List<Location>> leaderSigns = new ConcurrentHashMap<>();

    /**
     * Registriert ein neues Leader-Schild im Speicher.
     */
    public static void registerLeaderSign(String worldName, Location loc) {
        leaderSigns.computeIfAbsent(worldName, k -> new ArrayList<>()).add(loc);

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Leader-Schild für " + worldName +
                    " bei " + formatLoc(loc) + " registriert.");
        }
    }

    /**
     * Aktualisiert alle Leader-Schilder einer Welt.
     */
    public static void updateLeaderSigns(String worldName) {
        List<Location> signs = leaderSigns.get(worldName);
        if (signs == null || signs.isEmpty()) return;

        Long bestTime = TimeRepository.getBestTime(worldName);
        String leader = TimeRepository.getLeader(worldName);

        String leaderName = "—";
        if (leader != null) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(UUID.fromString(leader));
            if (offline != null && offline.getName() != null) {
                leaderName = offline.getName();
            }
        }
        String timeStr = (bestTime != null) ? TimeUtils.formatMs(bestTime) : "—";

        // Alias nutzen, falls vorhanden
        String alias = WorldRepository.getAlias(worldName);
        String displayName = (alias != null && !alias.isEmpty()) ? alias : worldName;

        for (Location loc : signs) {
            if (loc.getWorld() == null) continue; // Welt nicht geladen
            Block block = loc.getBlock();
            if (!(block.getState() instanceof Sign)) continue;

            Sign sign = (Sign) block.getState();
            sign.setLine(0, ChatColor.DARK_BLUE + "[JNR-LEADER]");
            sign.setLine(1, ChatColor.AQUA + displayName);
            sign.setLine(2, ChatColor.YELLOW + leaderName);
            sign.setLine(3, ChatColor.GRAY + timeStr);
            sign.update();
        }

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Alle Leader-Schilder für " +
                    worldName + " aktualisiert (Alias=" + displayName + ", Leader=" + leaderName + ", Zeit=" + timeStr + ")");
        }
    }

    private static String formatLoc(Location loc) {
        return String.format("(%s|%.1f,%.1f,%.1f)",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ());
    }
}
