package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Zentraler Config-Manager für das JumpAndRun-Plugin.
 * Lädt Werte aus der config.yml und stellt sie dem restlichen Plugin bereit.
 */
public class ConfigManager {

    private final JumpAndRun plugin;

    private boolean debug;

    private Material startPlate;
    private Material endPlate;
    private Material checkpointPlate;

    public ConfigManager(JumpAndRun plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Liest alle benötigten Werte aus der config.yml ein.
     */
    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        // Debugmodus
        this.debug = config.getBoolean("debug", false);

        // Druckplatten
        try {
            this.startPlate = Material.valueOf(config.getString("plates.start", "HEAVY_WEIGHTED_PRESSURE_PLATE"));
            this.endPlate = Material.valueOf(config.getString("plates.end", "LIGHT_WEIGHTED_PRESSURE_PLATE"));
            this.checkpointPlate = Material.valueOf(config.getString("plates.checkpoint", "STONE_PRESSURE_PLATE"));
        } catch (IllegalArgumentException e) {
            Bukkit.getConsoleSender().sendMessage("[JNR] Fehler in der config.yml: Ungültiges Material für Druckplatten.");
            // Fallback auf Defaults
            this.startPlate = Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
            this.endPlate = Material.LIGHT_WEIGHTED_PRESSURE_PLATE;
            this.checkpointPlate = Material.STONE_PRESSURE_PLATE;
        }

        if (debug) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Config geladen (Debug-Modus aktiv). " +
                    "Platten: Start=" + startPlate + ", Ziel=" + endPlate + ", Checkpoint=" + checkpointPlate);
        } else {
            Bukkit.getConsoleSender().sendMessage("[JNR] Config geladen (Debug-Modus deaktiviert).");
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public Material getStartPlate() {
        return startPlate;
    }

    public Material getEndPlate() {
        return endPlate;
    }

    public Material getCheckpointPlate() {
        return checkpointPlate;
    }
}
