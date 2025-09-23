package ch.ksrminecraft.jumpandrun.db;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.concurrent.TimeUnit;

/**
 * Speichert Laufzeiten zentral in der Tabelle "JumpAndRunTimes".
 */
public class TimeRepository {

    /**
     * Speichert eine Zeit für einen Spieler in einer bestimmten Welt.
     */
    public static void inputTime(World world, Player player, long time) {
        int jnrId = WorldRepository.ensureExists(world.getName());
        if (jnrId == -1) {
            log("Konnte keine ID für Welt " + world.getName() + " erzeugen.");
            return;
        }

        String sql = "INSERT INTO JumpAndRunTimes (jnrId, playerUUID, time) VALUES (?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jnrId);
            ps.setString(2, player.getUniqueId().toString());
            ps.setLong(3, time);
            ps.executeUpdate();
            log("Zeit für " + player.getName() + " gespeichert (" + time + " ms).");
        } catch (SQLException e) {
            log("Fehler beim Speichern der Zeit.");
            e.printStackTrace();
        }
    }

    /**
     * Gibt die Bestzeit für eine Welt zurück.
     */
    public static Long getBestTime(String worldName) {
        int jnrId = WorldRepository.getId(worldName);
        if (jnrId == -1) return null;

        String sql = "SELECT MIN(time) AS BestTime FROM JumpAndRunTimes WHERE jnrId = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jnrId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long best = rs.getLong("BestTime");
                    return rs.wasNull() ? null : best;
                }
            }
        } catch (SQLException e) {
            log("Fehler beim Abfragen der Bestzeit für " + worldName);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gibt den Leader (UUID) für eine Welt zurück.
     */
    public static String getLeader(String worldName) {
        int jnrId = WorldRepository.getId(worldName);
        if (jnrId == -1) return null;

        String sql = "SELECT playerUUID FROM JumpAndRunTimes " +
                "WHERE jnrId = ? ORDER BY time ASC LIMIT 1";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jnrId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("playerUUID");
                }
            }
        } catch (SQLException e) {
            log("Fehler beim Abfragen des Leaders für " + worldName);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Formatiert die Zeit als mm:ss.SSS
     */
    public static String formatTime(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long ms = millis % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, ms);
    }

    private static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("[JNR-DB] " + msg);
    }
}
