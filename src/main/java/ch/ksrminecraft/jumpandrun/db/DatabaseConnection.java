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

        String url = JumpAndRun.getPlugin().getConfig().getString("mysql.host");
        String user = JumpAndRun.getPlugin().getConfig().getString("mysql.user");
        String password = JumpAndRun.getPlugin().getConfig().getString("mysql.password");

        try {
            connection = DriverManager.getConnection(url, user, password);
            log("Verbindung zur JnR-Datenbank erfolgreich hergestellt.");
            return connection;
        } catch (SQLException e) {
            try {
                // Datenbank automatisch anlegen, falls nicht vorhanden
                String baseUrl = url.substring(0, url.lastIndexOf('/'));
                connection = DriverManager.getConnection(baseUrl, user, password);

                String dbName = url.substring(url.lastIndexOf('/') + 1).split("\\?")[0];
                Statement stmt = connection.createStatement();
                stmt.execute("CREATE DATABASE IF NOT EXISTS " + dbName);
                stmt.close();
                connection.close();

                connection = DriverManager.getConnection(url, user, password);
                log("Datenbank neu erstellt und verbunden.");
                return connection;
            } catch (SQLException ex) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DB] Verbindung fehlgeschlagen!");
                throw new RuntimeException(ex);
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
                    "JnrName VARCHAR(50), Published BOOLEAN, " +
                    "startLocationX INT, startLocationY INT, startLocationZ INT, " +
                    "YLimit INT, startTime TIME(3), CurrentPlayer VARCHAR(36))";
            stmt.execute(sql);
            stmt.close();
            log("Tabelle 'JumpAndRuns' geprüft/erstellt.");
        } catch (SQLException e) {
            log("Fehler beim Erstellen der Tabelle JumpAndRuns.");
            throw new RuntimeException(e);
        }
    }

    private static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("[JNR-DB] " + msg);
    }
}
