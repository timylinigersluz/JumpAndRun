package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.LeaderSignRepository;
import ch.ksrminecraft.jumpandrun.db.TimeRepository;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwalter für alle Leader-Schilder ([JNR-LEADER]).
 * Speichert jetzt den JumpAndRun-Alias statt der physikalischen Welt.
 */
public class SignUpdater {

    /** Cache: Map<Alias, Liste von Schild-Positionen> */
    private static final Map<String, List<Location>> leaderSigns = new ConcurrentHashMap<>();

    /** Lädt alle gespeicherten Leader-Schilder aus der Datenbank. */
    public static void loadAllFromDatabase() {
        leaderSigns.clear();

        // Wir laden für alle Aliase aus der DB (nicht nach Welt iterieren)
        Set<String> aliases = LeaderSignRepository.getAllAliases();
        for (String alias : aliases) {
            List<Location> locs = LeaderSignRepository.getLeaderSigns(alias);
            if (!locs.isEmpty()) {
                leaderSigns.put(alias, locs);
                if (JumpAndRun.getConfigManager().isDebug()) {
                    Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] "
                            + locs.size() + " Leader-Schilder aus DB geladen für Alias '" + alias + "'");
                }
            }
        }
    }

    /** Registriert ein neues Leader-Schild anhand des Aliases. */
    public static void registerLeaderSign(String alias, Location loc) {
        leaderSigns.computeIfAbsent(alias, k -> new ArrayList<>());
        if (leaderSigns.get(alias).stream().anyMatch(l -> l.equals(loc))) {
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Leader-Schild für Alias '" + alias
                        + "' bei " + formatLoc(loc) + " war bereits registriert.");
            }
            return;
        }

        leaderSigns.get(alias).add(loc);
        LeaderSignRepository.saveLeaderSign(alias, loc);

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Leader-Schild für Alias '" + alias
                    + "' bei " + formatLoc(loc) + " registriert und in DB gespeichert.");
        }

        updateLeaderSigns(alias);
    }

    /** Aktualisiert alle Leader-Schilder eines Aliases. */
    public static void updateLeaderSigns(String alias) {
        String worldName = WorldRepository.getWorldByAlias(alias);
        if (worldName == null) return;

        List<Location> signs = leaderSigns.computeIfAbsent(alias, a -> LeaderSignRepository.getLeaderSigns(alias));
        if (signs.isEmpty()) {
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Keine Leader-Schilder für Alias '" + alias + "' gefunden.");
            }
            return;
        }

        Long bestTime = TimeRepository.getBestTime(worldName);
        String leader = TimeRepository.getLeader(worldName);
        String leaderName = "—";
        if (leader != null) {
            try {
                OfflinePlayer off = Bukkit.getOfflinePlayer(UUID.fromString(leader));
                if (off != null && off.getName() != null) leaderName = off.getName();
            } catch (IllegalArgumentException ignored) {}
        }

        String timeStr = (bestTime != null) ? TimeUtils.formatMs(bestTime) : "—";

        for (Location loc : signs) {
            if (loc.getWorld() == null) continue;
            BlockState state = loc.getBlock().getState();
            if (!(state instanceof Sign)) continue;
            Sign sign = (Sign) state;

            sign.setLine(0, ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "[JNR-LEADER]");
            sign.setLine(1, ChatColor.AQUA + alias);
            sign.setLine(2, ChatColor.GOLD + leaderName);
            sign.setLine(3, ChatColor.GRAY + timeStr);
            sign.update();
        }

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Leader-Schilder für Alias '" + alias
                    + "' aktualisiert (Leader=" + leaderName + ", Zeit=" + timeStr + ")");
        }
    }

    /** Entfernt ein Leader-Schild (Alias + Location). */
    public static void unregisterLeaderSign(String alias, Location loc) {
        List<Location> signs = leaderSigns.get(alias);
        if (signs != null) {
            signs.removeIf(l -> l.equals(loc));
            if (signs.isEmpty()) leaderSigns.remove(alias);
        }
        LeaderSignRepository.deleteLeaderSign(alias, loc);

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Leader-Schild für Alias '" + alias
                    + "' bei " + formatLoc(loc) + " entfernt (DB & Cache).");
        }
    }

    /** Synchronisiert Welt <-> DB für Schilder, basierend auf Alias aus Zeile 2. */
    public static void syncWorldSigns(World world) {
        if (world == null) return;
        List<Location> worldSigns = new ArrayList<>();
        Map<String, List<Location>> dbByAlias = new HashMap<>();

        // DB-Inhalt zwischenspeichern
        for (String alias : LeaderSignRepository.getAllAliases()) {
            dbByAlias.put(alias, new ArrayList<>(LeaderSignRepository.getLeaderSigns(alias)));
        }

        // Welt nach [JNR-LEADER]-Schildern durchsuchen
        for (Chunk chunk : world.getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                if (!(state instanceof Sign)) continue;
                Sign sign = (Sign) state;
                String line0 = ChatColor.stripColor(sign.getLine(0));
                String alias = ChatColor.stripColor(sign.getLine(1));
                if (line0 != null && line0.equalsIgnoreCase("[JNR-LEADER]") && alias != null && !alias.isEmpty()) {
                    worldSigns.add(sign.getLocation());
                    List<Location> dbLocs = dbByAlias.computeIfAbsent(alias, k -> new ArrayList<>());
                    boolean exists = dbLocs.stream().anyMatch(l -> l.equals(sign.getLocation()));
                    if (!exists) {
                        LeaderSignRepository.saveLeaderSign(alias, sign.getLocation());
                        dbLocs.add(sign.getLocation());
                    }
                }
            }
        }

        // DB-Einträge löschen, deren Blöcke nicht mehr existieren
        for (String alias : dbByAlias.keySet()) {
            List<Location> dbLocs = dbByAlias.get(alias);
            for (Location loc : new ArrayList<>(dbLocs)) {
                if (!loc.getBlock().getType().name().contains("SIGN")) {
                    LeaderSignRepository.deleteLeaderSign(alias, loc);
                    dbLocs.remove(loc);
                }
            }
            leaderSigns.put(alias, dbLocs);
            updateLeaderSigns(alias);
        }

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Sign-Sync in Welt "
                    + world.getName() + " abgeschlossen.");
        }
    }

    private static String formatLoc(Location loc) {
        return String.format("(%s|%.1f,%.1f,%.1f)",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ());
    }
}
