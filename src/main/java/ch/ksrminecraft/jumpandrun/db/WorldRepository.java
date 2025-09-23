package ch.ksrminecraft.jumpandrun.db;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Datenbankoperationen für Jump-And-Run Welten (Metadaten).
 * Verwendet zentrale Tabelle "JumpAndRuns".
 */
public class WorldRepository {

    /**
     * Registriert eine neue Welt in der JumpAndRuns-Tabelle.
     */
    public static void registerWorld(Location spawnpoint, int height, String creatorId) {
        String sql = "INSERT INTO JumpAndRuns " +
                "(worldName, alias, creator, published, ready, startLocationX, startLocationY, startLocationZ, yLimit, currentPlayer) " +
                "VALUES (?, NULL, ?, false, false, ?, ?, ?, ?, NULL)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, spawnpoint.getWorld().getName());
            ps.setString(2, creatorId);
            ps.setInt(3, spawnpoint.getBlockX());
            ps.setInt(4, spawnpoint.getBlockY());
            ps.setInt(5, spawnpoint.getBlockZ());
            ps.setInt(6, spawnpoint.getBlockY() - height - 2);
            ps.executeUpdate();
            log("Welt " + spawnpoint.getWorld().getName() + " von " + creatorId + " registriert.");
        } catch (SQLException e) {
            log("Fehler beim Registrieren der Welt.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Holt die interne ID einer Welt anhand des technischen World-Namens.
     */
    public static int getId(String worldName) {
        String sql = "SELECT id FROM JumpAndRuns WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, worldName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            log("Fehler beim Ermitteln der ID für Welt " + worldName);
            throw new RuntimeException(e);
        }
        return -1;
    }

    public static void removeWorld(String worldName) {
        String sql = "DELETE FROM JumpAndRuns WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, worldName);
            ps.executeUpdate();
            log("Welt " + worldName + " aus DB entfernt.");
        } catch (SQLException e) {
            log("Fehler beim Entfernen der Welt " + worldName);
            throw new RuntimeException(e);
        }
    }

    public static List<String> getAllWorlds() {
        List<String> worlds = new ArrayList<>();
        String sql = "SELECT worldName FROM JumpAndRuns";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                worlds.add(rs.getString("worldName"));
            }
        } catch (SQLException e) {
            log("Fehler beim Laden aller Welten.");
            throw new RuntimeException(e);
        }
        return worlds;
    }

    public static boolean exists(String worldName) {
        String sql = "SELECT 1 FROM JumpAndRuns WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, worldName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log("Fehler bei exists(" + worldName + ")");
            throw new RuntimeException(e);
        }
    }

    // === Alias-Handling ===

    public static void setAlias(String worldName, String alias) {
        String sql = "UPDATE JumpAndRuns SET alias = ? WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, alias);
            ps.setString(2, worldName);
            ps.executeUpdate();
            log("Alias für Welt " + worldName + " gesetzt auf '" + alias + "'");
        } catch (SQLException e) {
            log("Fehler beim Setzen des Alias.");
            throw new RuntimeException(e);
        }
    }

    public static String getAlias(String worldName) {
        String sql = "SELECT alias FROM JumpAndRuns WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, worldName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("alias");
                }
            }
        } catch (SQLException e) {
            log("Fehler beim Laden des Alias.");
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String getWorldByAlias(String alias) {
        String sql = "SELECT worldName FROM JumpAndRuns WHERE alias = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, alias);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("worldName");
                }
            }
        } catch (SQLException e) {
            log("Fehler beim Suchen nach Welt per Alias.");
            throw new RuntimeException(e);
        }
        return null;
    }

    // === Creator / Status ===

    public static String getCreator(String worldName) {
        String sql = "SELECT creator FROM JumpAndRuns WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, worldName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("creator");
                }
            }
            return null;
        } catch (SQLException e) {
            log("Fehler beim Laden des Erstellers.");
            throw new RuntimeException(e);
        }
    }

    public static boolean isPublished(String worldName) {
        String sql = "SELECT published FROM JumpAndRuns WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, worldName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean("published");
            }
        } catch (SQLException e) {
            log("Fehler beim Prüfen von Published.");
            throw new RuntimeException(e);
        }
    }

    public static void setPublished(String worldName, boolean published) {
        String sql = "UPDATE JumpAndRuns SET published = ? WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, published);
            ps.setString(2, worldName);
            ps.executeUpdate();
            log("Welt " + worldName + " Published=" + published);
        } catch (SQLException e) {
            log("Fehler beim Setzen von Published.");
            throw new RuntimeException(e);
        }
    }

    public static boolean isReady(String worldName) {
        String sql = "SELECT ready FROM JumpAndRuns WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, worldName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean("ready");
            }
        } catch (SQLException e) {
            log("Fehler beim Prüfen von Ready.");
            throw new RuntimeException(e);
        }
    }

    public static void setReady(String worldName, boolean ready) {
        String sql = "UPDATE JumpAndRuns SET ready = ? WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, ready);
            ps.setString(2, worldName);
            ps.executeUpdate();
            log("Welt " + worldName + " Ready=" + ready);
        } catch (SQLException e) {
            log("Fehler beim Setzen von Ready.");
            throw new RuntimeException(e);
        }
    }

    // === Locations ===

    public static Location getStartLocation(String worldName) {
        String sql = "SELECT startLocationX, startLocationY, startLocationZ FROM JumpAndRuns WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, worldName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double x = rs.getDouble("startLocationX");
                    double y = rs.getDouble("startLocationY");
                    double z = rs.getDouble("startLocationZ");

                    World bukkitWorld = Bukkit.getWorld(worldName);
                    if (bukkitWorld != null) {
                        return new Location(bukkitWorld, x, y, z);
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            log("Fehler beim Laden der StartLocation für Welt " + worldName);
            throw new RuntimeException(e);
        }
    }

    public static int getYLimit(String worldName) {
        String sql = "SELECT yLimit FROM JumpAndRuns WHERE worldName = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, worldName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("yLimit");
                }
            }
            return 0;
        } catch (SQLException e) {
            log("Fehler beim Laden des YLimit für Welt " + worldName);
            throw new RuntimeException(e);
        }
    }

    public static List<String> getPublishedWorlds() {
        List<String> worlds = new ArrayList<>();
        String sql = "SELECT worldName FROM JumpAndRuns WHERE published = true";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                worlds.add(rs.getString("worldName"));
            }
        } catch (SQLException e) {
            log("Fehler beim Laden der veröffentlichten Welten.");
            throw new RuntimeException(e);
        }
        return worlds;
    }

    public static List<String> getDraftWorldsOf(UUID playerId) {
        List<String> worlds = new ArrayList<>();
        String sql = "SELECT worldName FROM JumpAndRuns WHERE published = false AND creator = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    worlds.add(rs.getString("worldName"));
                }
            }
        } catch (SQLException e) {
            log("Fehler beim Laden der Draft-Welten von " + playerId);
            throw new RuntimeException(e);
        }
        return worlds;
    }

    private static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage("[JNR-DB] " + msg);
    }

    public static int ensureExists(String worldName) {
        int id = getId(worldName);
        if (id != -1) {
            return id;
        }

        String sql = "INSERT INTO JumpAndRuns (worldName, alias, creator, published, ready, " +
                "startLocationX, startLocationY, startLocationZ, yLimit, currentPlayer) " +
                "VALUES (?, NULL, NULL, false, false, 0, 0, 0, 0, NULL)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, worldName);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    log("Welt " + worldName + " automatisch registriert (ID=" + newId + ").");
                    return newId;
                }
            }
        } catch (SQLException e) {
            log("Fehler beim automatischen Registrieren der Welt " + worldName);
            e.printStackTrace();
        }
        return -1;
    }
}
