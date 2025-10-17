package ch.ksrminecraft.jumpandrun.db;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verwaltet die Speicherung und Abfrage aller Leader-Schilder in der Datenbank.
 * Speichert den JumpAndRun-Alias (nicht den physischen Weltnamen).
 */
public class LeaderSignRepository {

    /** Speichert ein Leader-Schild (Alias + Blockkoordinaten). */
    public static void saveLeaderSign(String alias, Location loc) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "INSERT IGNORE INTO JumpAndRunLeaderSigns (worldName, x, y, z) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, alias); // Alias statt Weltname
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[JNR-DB] Fehler beim Speichern eines Leader-Schilds: " + e.getMessage());
        }
    }

    /** Löscht ein bestimmtes Leader-Schild (Alias + Blockkoordinaten). */
    public static void deleteLeaderSign(String alias, Location loc) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "DELETE FROM JumpAndRunLeaderSigns WHERE worldName=? AND x=? AND y=? AND z=?")) {
            ps.setString(1, alias);
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[JNR-DB] Fehler beim Löschen eines Leader-Schilds: " + e.getMessage());
        }
    }

    /**
     * Gibt alle gespeicherten Schilder eines Aliases zurück.
     * @param alias Alias des JumpAndRuns
     */
    public static List<Location> getLeaderSigns(String alias) {
        List<Location> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT x, y, z FROM JumpAndRunLeaderSigns WHERE worldName=?")) {
            ps.setString(1, alias);
            try (ResultSet rs = ps.executeQuery()) {
                // Wir wissen nicht, in welcher Welt das Schild steht – daher: alle Welten prüfen
                for (World world : Bukkit.getWorlds()) {
                    while (rs.next()) {
                        result.add(new Location(world,
                                rs.getInt("x"),
                                rs.getInt("y"),
                                rs.getInt("z")));
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[JNR-DB] Fehler beim Laden der Leader-Schilder für Alias '" + alias + "': " + e.getMessage());
        }
        return result;
    }

    /** Gibt alle Aliase zurück, für die Leader-Schilder existieren. */
    public static Set<String> getAllAliases() {
        Set<String> aliases = new HashSet<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT DISTINCT worldName FROM JumpAndRunLeaderSigns")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    aliases.add(rs.getString("worldName"));
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[JNR-DB] Fehler beim Laden aller Aliase aus JumpAndRunLeaderSigns: " + e.getMessage());
        }
        return aliases;
    }

    public static void clearCache() {
        // reserviert für künftige Nutzung
    }
}
