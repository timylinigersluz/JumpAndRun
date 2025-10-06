package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.TimeRepository;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.*;
import org.bukkit.block.BlockState;
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
     * Verhindert doppelte Einträge und führt sofort ein Update durch.
     */
    public static void registerLeaderSign(String worldName, Location loc) {
        leaderSigns.computeIfAbsent(worldName, k -> new ArrayList<>());

        // doppelte Registrierung verhindern
        if (leaderSigns.get(worldName).stream().anyMatch(l -> l.equals(loc))) {
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Leader-Schild für " + worldName +
                        " bei " + formatLoc(loc) + " war bereits registriert.");
            }
            return;
        }

        leaderSigns.get(worldName).add(loc);

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Leader-Schild für " + worldName +
                    " bei " + formatLoc(loc) + " registriert.");
        }

        // sofortiges Update für dieses Schild
        updateLeaderSigns(worldName);
    }

    /**
     * Aktualisiert alle Leader-Schilder einer Welt.
     * Bevor das passiert, werden alle [JNR-LEADER]-Schilder in der Welt gesucht und ggf. registriert.
     */
    public static void updateLeaderSigns(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        // 1️⃣ Scan der Welt, um unregistrierte Leader-Schilder zu finden
        int newlyRegistered = scanWorldForLeaderSigns(world);

        // 2️⃣ Vorhandene registrierte Schilder abrufen
        List<Location> signs = leaderSigns.get(worldName);
        if (signs == null || signs.isEmpty()) {
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Keine Leader-Schilder für " + worldName + " gefunden.");
            }
            return;
        }

        // 3️⃣ Bestzeiten abrufen
        Long bestTime = TimeRepository.getBestTime(worldName);
        String leader = TimeRepository.getLeader(worldName);

        String leaderName = "—";
        if (leader != null) {
            try {
                OfflinePlayer offline = Bukkit.getOfflinePlayer(UUID.fromString(leader));
                if (offline != null && offline.getName() != null) {
                    leaderName = offline.getName();
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        String timeStr = (bestTime != null) ? TimeUtils.formatMs(bestTime) : "—";

        String alias = WorldRepository.getAlias(worldName);
        String displayName = (alias != null && !alias.isEmpty()) ? alias : worldName;

        // 4️⃣ Alle Schilder in der Liste aktualisieren
        for (Location loc : signs) {
            if (loc.getWorld() == null) continue;
            BlockState state = loc.getBlock().getState();
            if (!(state instanceof Sign)) continue;

            Sign sign = (Sign) state;

            sign.setLine(0, ChatColor.DARK_BLUE.toString() + ChatColor.BOLD + "[JNR-LEADER]");
            sign.setLine(1, ChatColor.AQUA + displayName);
            sign.setLine(2, ChatColor.GOLD + leaderName);
            sign.setLine(3, ChatColor.GRAY + timeStr);
            sign.update();
        }

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Leader-Schilder für " + worldName +
                    " aktualisiert (" + displayName + ", Leader=" + leaderName + ", Zeit=" + timeStr + ", +" + newlyRegistered + " neue)");
        }
    }

    /**
     * Durchsucht eine Welt nach allen [JNR-LEADER]-Schildern
     * und registriert sie, falls sie noch nicht bekannt sind.
     *
     * @return Anzahl neu registrierter Schilder
     */
    public static int scanWorldForLeaderSigns(World world) {
        if (world == null) return 0;

        int count = 0;
        String worldName = world.getName();
        leaderSigns.computeIfAbsent(worldName, k -> new ArrayList<>());

        for (Chunk chunk : world.getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                if (!(state instanceof Sign)) continue;
                Sign sign = (Sign) state;

                String first = ChatColor.stripColor(sign.getLine(0)).trim();
                if (!first.equalsIgnoreCase("[JNR-LEADER]")) continue;

                Location loc = sign.getLocation();

                // Nur registrieren, wenn nicht bereits enthalten
                if (leaderSigns.get(worldName).stream().noneMatch(l -> l.equals(loc))) {
                    leaderSigns.get(worldName).add(loc);
                    count++;
                }
            }
        }

        if (count > 0 && JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] " + count + " neue Leader-Schilder in Welt '" + worldName + "' gefunden.");
        }

        return count;
    }

    /**
     * Entfernt ein Leader-Schild aus dem Speicher.
     */
    public static void unregisterLeaderSign(String worldName, Location loc) {
        List<Location> signs = leaderSigns.get(worldName);
        if (signs != null) {
            signs.removeIf(l -> l.equals(loc));
            if (signs.isEmpty()) {
                leaderSigns.remove(worldName);
            }
        }

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Leader-Schild für " + worldName +
                    " bei " + formatLoc(loc) + " entfernt.");
        }
    }

    private static String formatLoc(Location loc) {
        return String.format("(%s|%.1f,%.1f,%.1f)",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ());
    }
}
