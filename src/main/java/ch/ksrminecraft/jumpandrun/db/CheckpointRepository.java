package ch.ksrminecraft.jumpandrun.db;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Datenbank-Repository für JumpAndRun-Checkpoints.
 * Checkpoints werden pro JumpAndRun (jnrId) durchnummeriert (Idx).
 */
public class CheckpointRepository {

    /**
     * Erstellt die Checkpoints-Tabelle, falls sie noch nicht existiert.
     */
    public static void initializeTable() {
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS Checkpoints (" +
                    "jnrId INT," +
                    "idx INT," +
                    "x INT," +
                    "y INT," +
                    "z INT," +
                    "PRIMARY KEY(jnrId, idx)," +
                    "FOREIGN KEY (jnrId) REFERENCES JumpAndRuns(id) ON DELETE CASCADE)";
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
        int jnrId = WorldRepository.getId(worldName);
        if (jnrId == -1) {
            log("Konnte keine jnrId für Welt " + worldName + " finden.");
            return;
        }

        String sql;
        if (DatabaseConnection.isMySQL()) {
            sql = "INSERT INTO Checkpoints (jnrId, idx, x, y, z) VALUES (?,?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE x=?, y=?, z=?";
        } else {
            sql = "INSERT OR REPLACE INTO Checkpoints (jnrId, idx, x, y, z) VALUES (?,?,?,?,?)";
        }

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jnrId);
            ps.setInt(2, idx);
            ps.setInt(3, loc.getBlockX());
            ps.setInt(4, loc.getBlockY());
            ps.setInt(5, loc.getBlockZ());

            if (DatabaseConnection.isMySQL()) {
                ps.setInt(6, loc.getBlockX());
                ps.setInt(7, loc.getBlockY());
                ps.setInt(8, loc.getBlockZ());
            }

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
        int jnrId = WorldRepository.getId(worldName);
        if (jnrId == -1) return checkpoints;

        String sql = "SELECT x, y, z FROM Checkpoints WHERE jnrId=? ORDER BY idx ASC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jnrId);
            try (ResultSet rs = ps.executeQuery()) {
                World bukkitWorld = Bukkit.getWorld(worldName);
                while (rs.next()) {
                    Location loc = new Location(
                            bukkitWorld,
                            rs.getInt("x"),
                            rs.getInt("y"),
                            rs.getInt("z")
                    );
                    checkpoints.add(loc);
                }
            }
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
        int jnrId = WorldRepository.getId(worldName);
        if (jnrId == -1) return 1;

        String sql = "SELECT MAX(idx) as MaxIdx FROM Checkpoints WHERE jnrId=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jnrId);
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
