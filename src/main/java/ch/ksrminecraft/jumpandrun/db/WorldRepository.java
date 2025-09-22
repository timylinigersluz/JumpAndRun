package ch.ksrminecraft.jumpandrun.db;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Datenbankoperationen für Jump-And-Run Welten (Anlegen/Löschen, Statusabfragen).
 */
public class WorldRepository {

    /**
     * Registriert eine neue JumpAndRun-Welt in der Datenbank.
     *
     * @param spawnpoint Startposition der Welt
     * @param height     Höhe der Plattform
     * @param creatorId  UUID des Erstellers (als String)
     */
    public static void registerWorld(Location spawnpoint, int height, String creatorId) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            String sql = "INSERT INTO jnr.JumpAndRuns (" +
                    "JnrName, Published, Ready, StartLocationX, StartLocationY, StartLocationZ, YLimit, CurrentPlayer, Creator" +
                    ") VALUES (" +
                    "'" + spawnpoint.getWorld().getName() + "', false, false, " +
                    spawnpoint.getBlockX() + ", " + spawnpoint.getBlockY() + ", " +
                    spawnpoint.getBlockZ() + ", " + (spawnpoint.getBlockY() - height - 2) + ", " +
                    "NULL, '" + creatorId + "')";
            stmt.execute(sql);
            stmt.close();
            log("Welt " + spawnpoint.getWorld().getName() + " von " + creatorId + " registriert.");
        } catch (SQLException e) {
            log("Fehler beim Registrieren der Welt.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Entfernt eine Welt aus der DB.
     */
    public static void removeWorld(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            stmt.execute("DELETE FROM jnr.JumpAndRuns WHERE JnrName = '" + worldName + "'");
            stmt.close();
            log("Welt " + worldName + " aus DB entfernt.");
        } catch (SQLException e) {
            log("Fehler beim Entfernen der Welt " + worldName);
            throw new RuntimeException(e);
        }
    }

    /**
     * Holt alle registrierten Weltnamen.
     */
    public static List<String> getAllWorlds() {
        List<String> worlds = new ArrayList<>();
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT JnrName FROM jnr.JumpAndRuns");
            while (rs.next()) {
                worlds.add(rs.getString("JnrName"));
            }
            stmt.close();
        } catch (SQLException e) {
            log("Fehler beim Laden aller Welten.");
            throw new RuntimeException(e);
        }
        return worlds;
    }

    /**
     * Prüft, ob eine Welt in der DB registriert ist.
     */
    public static boolean exists(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1 FROM jnr.JumpAndRuns WHERE JnrName='" + worldName + "'");
            boolean exists = rs.next();
            stmt.close();
            return exists;
        } catch (SQLException e) {
            log("Fehler bei exists(" + worldName + ")");
            throw new RuntimeException(e);
        }
    }

    /**
     * Holt den Creator einer Welt.
     */
    public static String getCreator(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT CreatorUUID FROM jnr.JumpAndRuns WHERE JnrName='" + worldName + "'");
            String creator = null;
            if (rs.next()) {
                creator = rs.getString("CreatorUUID");
            }
            stmt.close();
            return creator;
        } catch (SQLException e) {
            log("Fehler beim Laden des Erstellers.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Prüft, ob eine Welt freigegeben (Published) ist.
     */
    public static boolean isPublished(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Published FROM jnr.JumpAndRuns WHERE JnrName='" + worldName + "'");
            boolean published = false;
            if (rs.next()) {
                published = rs.getBoolean("Published");
            }
            stmt.close();
            return published;
        } catch (SQLException e) {
            log("Fehler beim Prüfen von Published.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Setzt den Publish-Status.
     */
    public static void setPublished(String worldName, boolean published) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            stmt.executeUpdate("UPDATE jnr.JumpAndRuns SET Published=" + (published ? "true" : "false") +
                    " WHERE JnrName='" + worldName + "'");
            stmt.close();
            log("Welt " + worldName + " Published=" + published);
        } catch (SQLException e) {
            log("Fehler beim Setzen von Published.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Holt den Ready-Status.
     */
    public static boolean isReady(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Ready FROM jnr.JumpAndRuns WHERE JnrName='" + worldName + "'");
            boolean ready = false;
            if (rs.next()) {
                ready = rs.getBoolean("Ready");
            }
            stmt.close();
            return ready;
        } catch (SQLException e) {
            log("Fehler beim Prüfen von Ready.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Setzt den Ready-Status.
     */
    public static void setReady(String worldName, boolean ready) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            stmt.executeUpdate("UPDATE jnr.JumpAndRuns SET Ready=" + (ready ? "true" : "false") +
                    " WHERE JnrName='" + worldName + "'");
            stmt.close();
            log("Welt " + worldName + " Ready=" + ready);
        } catch (SQLException e) {
            log("Fehler beim Setzen von Ready.");
            throw new RuntimeException(e);
        }
    }

    private static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("[JNR-DB] " + msg);
    }

    /**
     * Holt die gespeicherte Startposition einer Welt aus der Datenbank.
     *
     * @param worldName Name der Welt
     * @return Bukkit Location des Startpunkts oder null, falls nicht gefunden
     */
    public static org.bukkit.Location getStartLocation(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            String sql = "SELECT StartLocationX, StartLocationY, StartLocationZ " +
                    "FROM jnr.JumpAndRuns WHERE JnrName='" + worldName + "'";
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                double x = rs.getDouble("StartLocationX");
                double y = rs.getDouble("StartLocationY");
                double z = rs.getDouble("StartLocationZ");

                org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
                if (bukkitWorld != null) {
                    org.bukkit.Location loc = new org.bukkit.Location(bukkitWorld, x, y, z);
                    rs.close();
                    stmt.close();
                    return loc;
                }
            }

            rs.close();
            stmt.close();
            return null;
        } catch (SQLException e) {
            log("Fehler beim Laden der StartLocation für Welt " + worldName);
            throw new RuntimeException(e);
        }
    }

    /**
     * Holt die gespeicherte Y-Grenze (unterhalb dieser Höhe wird der Spieler zurück teleportiert).
     *
     * @param worldName Name der Welt
     * @return gespeichertes YLimit oder 0, falls nicht vorhanden
     */
    public static int getYLimit(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            String sql = "SELECT YLimit FROM jnr.JumpAndRuns WHERE JnrName='" + worldName + "'";
            ResultSet rs = stmt.executeQuery(sql);

            int limit = 0;
            if (rs.next()) {
                limit = rs.getInt("YLimit");
            }

            rs.close();
            stmt.close();
            return limit;
        } catch (SQLException e) {
            log("Fehler beim Laden des YLimit für Welt " + worldName);
            throw new RuntimeException(e);
        }
    }
}
