package ch.ksrminecraft.jumpandrun.db;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.utils.ConfigManager;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;

/**
 * Verwaltet die Verbindung zur Datenbank (MySQL oder SQLite) und initialisiert Basis-Tabellen.
 */
public class DatabaseConnection {

    private static Connection connection;
    private static boolean mysql = true;

    public static boolean isMySQL() {
        return mysql;
    }

    /**
     * Liefert eine offene DB-Verbindung zurück.
     */
    public static Connection getConnection() {
        if (connection != null) return connection;

        ConfigManager cfg = JumpAndRun.getConfigManager();

        try {
            if (cfg.isJnrDbEnabled()) {
                // === MySQL ===
                mysql = true;
                String host = cfg.getJnrHost();
                int port = cfg.getJnrPort();
                String database = cfg.getJnrDatabase();
                String user = cfg.getJnrUser();
                String password = cfg.getJnrPassword();

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";

                log("[DEBUG] MySQL-Verbindung aufbauen: " + url + " User=" + user);
                connection = DriverManager.getConnection(url, user, password);
                log("MySQL-Verbindung erfolgreich hergestellt.");
            } else {
                // === SQLite ===
                mysql = false;
                File dbFile = new File(JumpAndRun.getPlugin().getDataFolder(), "jumpandrun.db");
                dbFile.getParentFile().mkdirs();

                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                log("[DEBUG] SQLite-Verbindung aufbauen: " + url);

                connection = DriverManager.getConnection(url);
                log("SQLite-Verbindung erfolgreich hergestellt.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Konnte keine Verbindung zur Datenbank herstellen", e);
        }

        return connection;
    }

    /**
     * Erstellt die zentralen Tabellen für JumpAndRun.
     */
    public static void initializeWorldTables() {
        try (Statement stmt = getConnection().createStatement()) {

            // JumpAndRuns
            stmt.execute("CREATE TABLE IF NOT EXISTS JumpAndRuns (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," + // AUTOINCREMENT = MySQL ignoriert Zusatz
                    "worldName VARCHAR(100) NOT NULL UNIQUE," +
                    "alias VARCHAR(100) DEFAULT ''," +
                    "creator CHAR(36)," +
                    "published BOOLEAN DEFAULT false," +
                    "ready BOOLEAN DEFAULT false," +
                    "startLocationX INT, startLocationY INT, startLocationZ INT," +
                    "yLimit INT," +
                    "currentPlayer CHAR(36))");

            // Laufzeiten
            stmt.execute("CREATE TABLE IF NOT EXISTS JumpAndRunTimes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "jnrId INT NOT NULL," +
                    "playerUUID CHAR(36) NOT NULL," +
                    "time BIGINT NOT NULL," +
                    "created TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (jnrId) REFERENCES JumpAndRuns(id) ON DELETE CASCADE)");

            // Checkpoints
            stmt.execute("CREATE TABLE IF NOT EXISTS Checkpoints (" +
                    "jnrId INT NOT NULL," +
                    "idx INT NOT NULL," +
                    "x INT, y INT, z INT," +
                    "PRIMARY KEY (jnrId, idx)," +
                    "FOREIGN KEY (jnrId) REFERENCES JumpAndRuns(id) ON DELETE CASCADE)");

            // Aktive Runs
            stmt.execute("CREATE TABLE IF NOT EXISTS ActiveRuns (" +
                    "jnrId INT NOT NULL," +
                    "playerUUID CHAR(36) NOT NULL," +
                    "startTime BIGINT NOT NULL," +
                    "PRIMARY KEY (jnrId, playerUUID)," +
                    "FOREIGN KEY (jnrId) REFERENCES JumpAndRuns(id) ON DELETE CASCADE)");

            log("Tabellen JumpAndRuns, JumpAndRunTimes, Checkpoints und ActiveRuns geprüft/erstellt.");
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Initialisieren der Tabellen", e);
        }
    }

    private static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("[JNR-DB] " + msg);
    }
}
