package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Zentraler Config-Manager für das JumpAndRun-Plugin.
 * Lädt Werte aus der config.yml und stellt sie dem restlichen Plugin bereit.
 * Bereinigt ungültige Location-Einträge beim Start, ohne unbekannte Welten zu laden.
 */
public class ConfigManager {

    private final JumpAndRun plugin;

    private boolean debug;
    private Material startPlate;
    private Material endPlate;
    private Material checkpointPlate;
    private String fallbackWorld;

    // --- DB Settings ---
    private boolean jnrDbEnabled;
    private String jnrHost;
    private int jnrPort;
    private String jnrDatabase;
    private String jnrUser;
    private String jnrPassword;

    private boolean pointsDbEnabled;
    private boolean pointsExcludeStaff;
    private String pointsHost;
    private int pointsPort;
    private String pointsDatabase;
    private String pointsUser;
    private String pointsPassword;

    public ConfigManager(JumpAndRun plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Lädt alle Konfigurationen und entfernt ungültige Welteinträge,
     * ohne die Location-Deserialisierung zu triggern.
     */
    public void loadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Debugmodus
        this.debug = config.getBoolean("debug", false);

        // --- JumpAndRun-DB ---
        this.jnrDbEnabled = config.getBoolean("jumpandrun.enabled", true);
        this.jnrHost = config.getString("jumpandrun.host", "localhost");
        this.jnrPort = config.getInt("jumpandrun.port", 3306);
        this.jnrDatabase = config.getString("jumpandrun.database", "jnr");
        this.jnrUser = config.getString("jumpandrun.user", "root");
        this.jnrPassword = config.getString("jumpandrun.password", "");

        // --- Punkte-DB ---
        this.pointsDbEnabled = config.getBoolean("pointsdb.enabled", true);
        this.pointsExcludeStaff = config.getBoolean("pointsdb.excludeStaff", true);
        this.pointsHost = config.getString("pointsdb.host", "localhost");
        this.pointsPort = config.getInt("pointsdb.port", 3306);
        this.pointsDatabase = config.getString("pointsdb.database", "rankpoints");
        this.pointsUser = config.getString("pointsdb.user", "root");
        this.pointsPassword = config.getString("pointsdb.password", "");

        // --- Druckplatten ---
        try {
            this.startPlate = Material.valueOf(config.getString("plates.start", "HEAVY_WEIGHTED_PRESSURE_PLATE"));
            this.endPlate = Material.valueOf(config.getString("plates.end", "LIGHT_WEIGHTED_PRESSURE_PLATE"));
            this.checkpointPlate = Material.valueOf(config.getString("plates.checkpoint", "STONE_PRESSURE_PLATE"));
        } catch (IllegalArgumentException e) {
            Bukkit.getConsoleSender().sendMessage("[JNR] Fehler in der config.yml: Ungültiges Material für Druckplatten.");
            this.startPlate = Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
            this.endPlate = Material.LIGHT_WEIGHTED_PRESSURE_PLATE;
            this.checkpointPlate = Material.STONE_PRESSURE_PLATE;
        }

        // --- Fallback-Welt ---
        this.fallbackWorld = config.getString("fallback-world", "world");

        // --- Ungültige Welten bei gespeicherten Locations entfernen ---
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection != null) {
            for (String uuid : playersSection.getKeys(false)) {
                ConfigurationSection playerSec = playersSection.getConfigurationSection(uuid);
                if (playerSec == null) continue;

                ConfigurationSection lobbyLoc = playerSec.getConfigurationSection("lobbyLocation");
                if (lobbyLoc != null) {
                    String worldName = lobbyLoc.getString("world");
                    if (worldName != null && Bukkit.getWorld(worldName) == null) {
                        playerSec.set("lobbyLocation", null);
                        if (debug)
                            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Ungültige LobbyLocation für Spieler "
                                    + uuid + " entfernt (Welt '" + worldName + "' nicht geladen).");
                    }
                }
            }
            plugin.saveConfig();
        }

        // --- Logausgabe ---
        if (debug) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Config geladen (Debug-Modus aktiv). " +
                    "Platten: Start=" + startPlate + ", Ziel=" + endPlate + ", Checkpoint=" + checkpointPlate +
                    ", FallbackWorld=" + fallbackWorld +
                    ", JNR-DB=" + (jnrDbEnabled ? "MySQL" : "SQLite") +
                    ", Points-DB=" + (pointsDbEnabled ? "MySQL" : "SQLite"));
        } else {
            Bukkit.getConsoleSender().sendMessage("[JNR] Config geladen (Debug-Modus deaktiviert). FallbackWorld=" + fallbackWorld);
        }
    }

    // --- Getter ---
    public boolean isDebug() { return debug; }
    public String getFallbackWorld() { return fallbackWorld; }

    public Material getStartPlate() { return startPlate; }
    public Material getEndPlate() { return endPlate; }
    public Material getCheckpointPlate() { return checkpointPlate; }

    public boolean isJnrDbEnabled() { return jnrDbEnabled; }
    public String getJnrHost() { return jnrHost; }
    public int getJnrPort() { return jnrPort; }
    public String getJnrDatabase() { return jnrDatabase; }
    public String getJnrUser() { return jnrUser; }
    public String getJnrPassword() { return jnrPassword; }

    public boolean isPointsDbEnabled() { return pointsDbEnabled; }
    public boolean isPointsExcludeStaff() { return pointsExcludeStaff; }
    public String getPointsHost() { return pointsHost; }
    public int getPointsPort() { return pointsPort; }
    public String getPointsDatabase() { return pointsDatabase; }
    public String getPointsUser() { return pointsUser; }
    public String getPointsPassword() { return pointsPassword; }
}
