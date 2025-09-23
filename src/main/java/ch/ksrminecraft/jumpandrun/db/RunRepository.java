package ch.ksrminecraft.jumpandrun.db;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalTime;

/**
 * Verwaltet Runs: Startzeit & Spieler.
 */
public class RunRepository {

    public static void inputStartTime(World world, Player player) {
        try {
            Time startTime = Time.valueOf(LocalTime.now());
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            String sql = "UPDATE JumpAndRuns SET StartTime='" + startTime + "', " +
                    "CurrentPlayer='" + player.getUniqueId() + "' WHERE JnrName='" + world.getName() + "'";
            stmt.executeUpdate(sql);
            stmt.close();
            log("Startzeit für Welt " + world.getName() + " gespeichert.");
        } catch (SQLException e) {
            log("Fehler beim Speichern der Startzeit.");
        }
    }

    public static Time getStartTime(World world) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT StartTime FROM JumpAndRuns WHERE JnrName='" + world.getName() + "'");
            Time start = rs.next() ? rs.getTime("StartTime") : null;
            stmt.close();
            return start;
        } catch (SQLException e) {
            log("Fehler beim Laden der Startzeit.");
            throw new RuntimeException(e);
        }
    }

    public static Player getStartPlayer(World world) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT CurrentPlayer FROM JumpAndRuns WHERE JnrName='" + world.getName() + "'");
            Player result = rs.next() ? Bukkit.getPlayer(rs.getString("CurrentPlayer")) : null;
            stmt.close();
            return result;
        } catch (SQLException e) {
            log("Fehler beim Laden des Startspielers.");
            throw new RuntimeException(e);
        }
    }

    private static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("[JNR-DB] " + msg);
    }
}
