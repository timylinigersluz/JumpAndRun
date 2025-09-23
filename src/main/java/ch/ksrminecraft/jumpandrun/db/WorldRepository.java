package ch.ksrminecraft.jumpandrun.db;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Datenbankoperationen für Jump-And-Run Welten.
 */
public class WorldRepository {

    public static void registerWorld(Location spawnpoint, int height, String creatorId) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            String sql = "INSERT INTO JumpAndRuns (" +
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

    public static void removeWorld(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            stmt.execute("DELETE FROM JumpAndRuns WHERE JnrName = '" + worldName + "'");
            stmt.close();
            log("Welt " + worldName + " aus DB entfernt.");
        } catch (SQLException e) {
            log("Fehler beim Entfernen der Welt " + worldName);
            throw new RuntimeException(e);
        }
    }

    public static List<String> getAllWorlds() {
        List<String> worlds = new ArrayList<>();
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT JnrName FROM JumpAndRuns");
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

    public static boolean exists(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1 FROM JumpAndRuns WHERE JnrName='" + worldName + "'");
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
            ResultSet rs = stmt.executeQuery("SELECT Creator FROM JumpAndRuns WHERE JnrName='" + worldName + "'");
            String creator = null;
            if (rs.next()) {
                creator = rs.getString("Creator");
            }
            stmt.close();
            return creator;
        } catch (SQLException e) {
            log("Fehler beim Laden des Erstellers.");
            throw new RuntimeException(e);
        }
    }

    public static boolean isPublished(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Published FROM JumpAndRuns WHERE JnrName='" + worldName + "'");
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

    public static void setPublished(String worldName, boolean published) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            stmt.executeUpdate("UPDATE JumpAndRuns SET Published=" + (published ? "true" : "false") +
                    " WHERE JnrName='" + worldName + "'");
            stmt.close();
            log("Welt " + worldName + " Published=" + published);
        } catch (SQLException e) {
            log("Fehler beim Setzen von Published.");
            throw new RuntimeException(e);
        }
    }

    public static boolean isReady(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Ready FROM JumpAndRuns WHERE JnrName='" + worldName + "'");
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

    public static void setReady(String worldName, boolean ready) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            stmt.executeUpdate("UPDATE JumpAndRuns SET Ready=" + (ready ? "true" : "false") +
                    " WHERE JnrName='" + worldName + "'");
            stmt.close();
            log("Welt " + worldName + " Ready=" + ready);
        } catch (SQLException e) {
            log("Fehler beim Setzen von Ready.");
            throw new RuntimeException(e);
        }
    }

    public static org.bukkit.Location getStartLocation(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            String sql = "SELECT StartLocationX, StartLocationY, StartLocationZ " +
                    "FROM JumpAndRuns WHERE JnrName='" + worldName + "'";
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

    public static int getYLimit(String worldName) {
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            String sql = "SELECT YLimit FROM JumpAndRuns WHERE JnrName='" + worldName + "'";
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

    private static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("[JNR-DB] " + msg);
    }

    public static List<String> getPublishedWorlds() {
        List<String> worlds = new ArrayList<>();
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT JnrName FROM JumpAndRuns WHERE Published=true");
            while (rs.next()) {
                worlds.add(rs.getString("JnrName"));
            }
            stmt.close();
        } catch (SQLException e) {
            log("Fehler beim Laden der veröffentlichten Welten.");
            throw new RuntimeException(e);
        }
        return worlds;
    }

    public static List<String> getDraftWorldsOf(java.util.UUID playerId) {
        List<String> worlds = new ArrayList<>();
        try {
            Statement stmt = DatabaseConnection.getConnection().createStatement();
            String sql = "SELECT JnrName FROM JumpAndRuns WHERE Published=false AND Creator='" + playerId.toString() + "'";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                worlds.add(rs.getString("JnrName"));
            }
            stmt.close();
        } catch (SQLException e) {
            log("Fehler beim Laden der Draft-Welten von " + playerId);
            throw new RuntimeException(e);
        }
        return worlds;
    }
}
