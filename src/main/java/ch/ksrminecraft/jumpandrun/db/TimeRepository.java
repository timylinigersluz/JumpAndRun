package ch.ksrminecraft.jumpandrun.db;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

/**
 * Speichert Laufzeiten pro Welt in eigenen Tabellen.
 */
public class TimeRepository {

    /**
     * Initialisiert die Zeitentabelle für eine Welt.
     */
    public static void initializeTimeTable(World world) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS jnr." + world.getName() + " (" +
                    "Player CHAR(36), " +
                    "time BIGINT)";
            stmt.execute(sql);
            stmt.close();
            log("Tabelle für Welt " + world.getName() + " geprüft/erstellt.");
        } catch (SQLException e) {
            log("Fehler beim Erstellen der Tabelle für Welt " + world.getName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Speichert eine Zeit in der Datenbank.
     */
    public static void inputTime(World world, Player player, long time) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            String sql = "INSERT INTO jnr." + world.getName() +
                    " (Player, time) VALUES ('" + player.getUniqueId() + "', " + time + ")";
            stmt.execute(sql);
            stmt.close();
            log("Zeit für " + player.getName() + " gespeichert (" + time + "ms).");
        } catch (SQLException e) {
            log("Fehler beim Speichern der Zeit in Welt " + world.getName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Holt die beste (niedrigste) Zeit in einer Welt.
     * @return Zeit in Millisekunden oder null, wenn keine Daten vorhanden.
     */
    public static Long getBestTime(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MIN(time) as BestTime FROM jnr." + worldName);
            Long bestTime = null;
            if (rs.next()) {
                bestTime = rs.getLong("BestTime");
                if (rs.wasNull()) bestTime = null;
            }
            stmt.close();
            return bestTime;
        } catch (SQLException e) {
            log("Fehler beim Abfragen der Bestzeit für Welt " + worldName);
            throw new RuntimeException(e);
        }
    }

    /**
     * Holt den Spieler (Leader) mit der Bestzeit einer Welt.
     * @return UUID als String oder null, wenn keine Daten vorhanden.
     */
    public static String getLeader(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            String sql = "SELECT Player FROM jnr." + worldName +
                    " WHERE time = (SELECT MIN(time) FROM jnr." + worldName + ") LIMIT 1";
            ResultSet rs = stmt.executeQuery(sql);
            String leader = null;
            if (rs.next()) {
                leader = rs.getString("Player"); // UUID
            }
            stmt.close();
            return leader;
        } catch (SQLException e) {
            log("Fehler beim Abfragen des Leaders für Welt " + worldName);
            throw new RuntimeException(e);
        }
    }

    /**
     * Formatiert eine Zeit in Millisekunden zu MM:SS.mmm
     * Beispiel: 83456ms -> "01:23.456"
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
