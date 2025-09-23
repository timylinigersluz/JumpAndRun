package ch.ksrminecraft.jumpandrun.db;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.*;

/**
 * Verwaltet aktive Runs: Startzeit & Spieler.
 * Nutzt die Tabelle ActiveRuns.
 */
public class RunRepository {

    /**
     * Speichert die Startzeit für einen Spieler in einer Welt.
     */
    public static void inputStartTime(World world, Player player) {
        int jnrId = WorldRepository.getId(world.getName());
        if (jnrId == -1) {
            log("Konnte keine jnrId für Welt " + world.getName() + " finden.");
            return;
        }

        String sql = "INSERT INTO ActiveRuns (jnrId, playerUUID, startTime) VALUES (?,?,?) " +
                "ON DUPLICATE KEY UPDATE startTime=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            long now = System.currentTimeMillis();
            ps.setInt(1, jnrId);
            ps.setString(2, player.getUniqueId().toString());
            ps.setLong(3, now);
            ps.setLong(4, now);
            ps.executeUpdate();
            log("Startzeit für " + player.getName() + " in Welt " + world.getName() + " gespeichert.");
        } catch (SQLException e) {
            log("Fehler beim Speichern der Startzeit.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Holt die Startzeit eines Spielers in einer Welt.
     */
    public static Long getStartTime(World world, Player player) {
        int jnrId = WorldRepository.getId(world.getName());
        if (jnrId == -1) return null;

        String sql = "SELECT startTime FROM ActiveRuns WHERE jnrId=? AND playerUUID=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jnrId);
            ps.setString(2, player.getUniqueId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("startTime");
                }
            }
        } catch (SQLException e) {
            log("Fehler beim Laden der Startzeit.");
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Entfernt den aktiven Run eines Spielers (z. B. nach Ziel erreicht).
     */
    public static void clearRun(World world, Player player) {
        int jnrId = WorldRepository.getId(world.getName());
        if (jnrId == -1) return;

        String sql = "DELETE FROM ActiveRuns WHERE jnrId=? AND playerUUID=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jnrId);
            ps.setString(2, player.getUniqueId().toString());
            ps.executeUpdate();
            log("Run von " + player.getName() + " in Welt " + world.getName() + " entfernt.");
        } catch (SQLException e) {
            log("Fehler beim Löschen eines aktiven Runs.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Entfernt alle aktiven Runs einer Welt (z. B. wenn die Welt gelöscht wird).
     */
    public static void clearRunsForWorld(String worldName) {
        int jnrId = WorldRepository.getId(worldName);
        if (jnrId == -1) return;

        String sql = "DELETE FROM ActiveRuns WHERE jnrId=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jnrId);
            int count = ps.executeUpdate();
            log(count + " aktive Runs in Welt " + worldName + " entfernt.");
        } catch (SQLException e) {
            log("Fehler beim Löschen aller aktiven Runs einer Welt.");
            throw new RuntimeException(e);
        }
    }

    private static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("[JNR-DB] " + msg);
    }
}
