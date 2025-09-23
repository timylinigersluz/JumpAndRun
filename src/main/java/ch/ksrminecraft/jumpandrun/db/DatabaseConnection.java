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
    public static void initializeWorldTables() {
        try (Statement stmt = getConnection().createStatement()) {
            // JumpAndRuns
            stmt.execute("CREATE TABLE IF NOT EXISTS JumpAndRuns (" +
                    "id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                    "worldName VARCHAR(100) NOT NULL," +
                    "alias VARCHAR(100) DEFAULT ''," +
                    "creator CHAR(36) NOT NULL," +
                    "published BOOLEAN DEFAULT false," +
                    "ready BOOLEAN DEFAULT false," +
                    "startLocationX INT, startLocationY INT, startLocationZ INT," +
                    "yLimit INT," +
                    "currentPlayer CHAR(36)," +
                    "UNIQUE KEY unique_world (worldName)" +
                    ") ENGINE=InnoDB");

            // Laufzeiten
            stmt.execute("CREATE TABLE IF NOT EXISTS JumpAndRunTimes (" +
                    "id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                    "jnrId INT UNSIGNED NOT NULL," +
                    "playerUUID CHAR(36) NOT NULL," +
                    "time BIGINT NOT NULL," +
                    "created TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (jnrId) REFERENCES JumpAndRuns(id) ON DELETE CASCADE," +
                    "INDEX idx_jnr_times (jnrId, time)" +
                    ") ENGINE=InnoDB");

            // Checkpoints
            stmt.execute("CREATE TABLE IF NOT EXISTS Checkpoints (" +
                    "jnrId INT UNSIGNED NOT NULL," +
                    "idx INT NOT NULL," +
                    "x INT, y INT, z INT," +
                    "PRIMARY KEY (jnrId, idx)," +
                    "FOREIGN KEY (jnrId) REFERENCES JumpAndRuns(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB");

            // Aktive Runs (neu!)
            stmt.execute("CREATE TABLE IF NOT EXISTS ActiveRuns (" +
                    "jnrId INT UNSIGNED NOT NULL," +
                    "playerUUID CHAR(36) NOT NULL," +
                    "startTime BIGINT NOT NULL," +
                    "PRIMARY KEY (jnrId, playerUUID)," +
                    "FOREIGN KEY (jnrId) REFERENCES JumpAndRuns(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB");

            log("Tabellen JumpAndRuns, JumpAndRunTimes, Checkpoints und ActiveRuns geprüft/erstellt.");
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Initialisieren der Tabellen", e);
        }
    }

    /**
     * Hilfsmethode fürs Logging.
     */
    private static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("[JNR-DB] " + msg);
    }
}
