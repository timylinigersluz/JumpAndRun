package ch.ksrminecraft.jumpandrun.db;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Datenbank-Repository für JumpAndRun-Checkpoints.
 * Checkpoints sind pro Welt durchnummeriert (Idx).
 */
public class CheckpointRepository {

    /**
     * Erstellt die Checkpoints-Tabelle, falls sie noch nicht existiert.
     */
    public static void initializeTable() {
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS Checkpoints (" +
                    "WorldName VARCHAR(50)," +
                    "Idx INT," +
                    "X INT," +
                    "Y INT," +
                    "Z INT," +
                    "PRIMARY KEY(WorldName, Idx))";
            stmt.execute(sql);
            log("Checkpoints-Tabelle geprüft/erstellt.");
        } catch (SQLException e) {
            log("Fehler beim Erstellen der Checkpoints-Tabelle.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Fügt einen neuen Checkpoint für eine Welt hinzu.
     */
    public static void addCheckpoint(String worldName, int idx, Location loc) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "INSERT INTO Checkpoints (WorldName, Idx, X, Y, Z) VALUES (?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE X=?, Y=?, Z=?")) {
            ps.setString(1, worldName);
            ps.setInt(2, idx);
            ps.setInt(3, loc.getBlockX());
            ps.setInt(4, loc.getBlockY());
            ps.setInt(5, loc.getBlockZ());
            ps.setInt(6, loc.getBlockX());
            ps.setInt(7, loc.getBlockY());
            ps.setInt(8, loc.getBlockZ());
            ps.executeUpdate();
            log("Checkpoint #" + idx + " in Welt " + worldName +
                    " gespeichert bei (" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")");
        } catch (SQLException e) {
            log("Fehler beim Speichern von Checkpoint #" + idx + " in Welt " + worldName);
            throw new RuntimeException(e);
        }
    }

    /**
     * Holt alle Checkpoints einer Welt.
     */
    public static List<Location> getCheckpoints(String worldName) {
        List<Location> checkpoints = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT X, Y, Z FROM Checkpoints WHERE WorldName=? ORDER BY Idx ASC")) {
            ps.setString(1, worldName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Location loc = new Location(
                        Bukkit.getWorld(worldName),
                        rs.getInt("X"),
                        rs.getInt("Y"),
                        rs.getInt("Z")
                );
                checkpoints.add(loc);
            }
            rs.close();
        } catch (SQLException e) {
            log("Fehler beim Laden der Checkpoints für Welt " + worldName);
            throw new RuntimeException(e);
        }
        return checkpoints;
    }

    /**
     * Holt den nächsten freien Index für einen neuen Checkpoint in einer Welt.
     */
    public static int getNextIndex(String worldName) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT MAX(Idx) as MaxIdx FROM Checkpoints WHERE WorldName=?")) {
            ps.setString(1, worldName);
            ResultSet rs = ps.executeQuery();
            int idx = 1;
            if (rs.next()) {
                idx = rs.getInt("MaxIdx") + 1;
            }
            rs.close();
            return idx;
        } catch (SQLException e) {
            log("Fehler beim Ermitteln des nächsten Checkpoint-Index für Welt " + worldName);
            throw new RuntimeException(e);
        }
    }

    private static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("[JNR-DB] " + msg);
    }
}
