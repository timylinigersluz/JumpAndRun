package ch.ksrminecraft.jumpandrun.db;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Verwaltet die Verbindung zur MySQL-Datenbank und initialisiert Basis-Tabellen.
 */
public class DatabaseConnection {

    private static Connection connection;

    /**
     * Liefert eine offene DB-Verbindung zurück.
     */
    public static Connection getConnection() {
        if (connection != null) return connection;

        String host = JumpAndRun.getPlugin().getConfig().getString("mysql.host");
        int port = JumpAndRun.getPlugin().getConfig().getInt("mysql.port");
        String database = JumpAndRun.getPlugin().getConfig().getString("mysql.database");
        String user = JumpAndRun.getPlugin().getConfig().getString("mysql.user");
        String password = JumpAndRun.getPlugin().getConfig().getString("mysql.password");

        // JDBC-URL zusammensetzen
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";

        log("[DEBUG] Versuche Verbindung aufzubauen...");
        log("[DEBUG] Host=" + host + " Port=" + port + " DB=" + database + " User=" + user);

        try {
            connection = DriverManager.getConnection(url, user, password);
            log("Verbindung zur JnR-Datenbank erfolgreich hergestellt.");
            return connection;
        } catch (SQLException e) {
            log("[ERROR] Verbindung fehlgeschlagen!");
            log("[ERROR] SQLState=" + e.getSQLState() + " ErrorCode=" + e.getErrorCode() + " Message=" + e.getMessage());

            try {
                // Datenbank automatisch anlegen, falls nicht vorhanden
                String baseUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&autoReconnect=true";
                connection = DriverManager.getConnection(baseUrl, user, password);

                log("[DEBUG] Mit Server verbunden, versuche Datenbank '" + database + "' anzulegen...");
                String sql = "CREATE DATABASE IF NOT EXISTS " + database;
                Statement stmt = connection.createStatement();
                stmt.execute(sql);
                stmt.close();
                connection.close();

                connection = DriverManager.getConnection(url, user, password);
                log("Datenbank '" + database + "' neu erstellt und verbunden.");
                return connection;
            } catch (SQLException ex) {
                log("[FATAL] Verbindung endgültig fehlgeschlagen!");
                log("[FATAL] SQLState=" + ex.getSQLState() + " ErrorCode=" + ex.getErrorCode() + " Message=" + ex.getMessage());
                throw new RuntimeException("Konnte keine Verbindung zur Datenbank herstellen", ex);
            }
        }
    }

    /**
     * Erstellt die zentrale JumpAndRuns-Tabelle.
     */
    public static void initializeWorldTable() {
        try {
            Statement stmt = getConnection().createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS JumpAndRuns (" +
                    "JnrName VARCHAR(50), " +
                    "Published BOOLEAN, " +
                    "startLocationX INT, startLocationY INT, startLocationZ INT, " +
                    "YLimit INT, " +
                    "startTime TIME(3), " +
                    "CurrentPlayer VARCHAR(36))";
            stmt.execute(sql);
            stmt.close();
            log("Tabelle 'JumpAndRuns' geprüft/erstellt.");
        } catch (SQLException e) {
            log("[ERROR] Fehler beim Erstellen der Tabelle 'JumpAndRuns'.");
            log("[ERROR] SQLState=" + e.getSQLState() + " ErrorCode=" + e.getErrorCode() + " Message=" + e.getMessage());
            throw new RuntimeException("Fehler beim Initialisieren der Tabelle JumpAndRuns", e);
        }
    }

    /**
     * Hilfsmethode fürs Logging.
     */
    private static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("[JNR-DB] " + msg);
    }
}
