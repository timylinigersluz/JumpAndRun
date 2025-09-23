package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

/**
 * Zentraler Config-Manager für das JumpAndRun-Plugin.
 * Lädt Werte aus der config.yml und stellt sie dem restlichen Plugin bereit.
 * Bereinigt ungültige LobbyLocations beim Start.
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
     * Entfernt ungültige LobbyLocations (Welt nicht mehr vorhanden).
     */
    public void loadConfig() {
        plugin.reloadConfig();
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

        // Ungültige LobbyLocations aufräumen
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection != null) {
            Set<String> playerKeys = playersSection.getKeys(false);
            for (String uuid : playerKeys) {
                String worldName = config.getString("players." + uuid + ".lobbyLocation.world");
                if (worldName != null) {
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        // Welt existiert nicht -> Eintrag löschen
                        config.set("players." + uuid + ".lobbyLocation", null);
                        Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Ungültige LobbyLocation für Spieler " + uuid + " entfernt (Welt " + worldName + " nicht gefunden).");
                    }
                }
            }
            plugin.saveConfig();
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
