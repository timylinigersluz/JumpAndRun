package ch.ksrminecraft.jumpandrun;

import ch.ksrminecraft.jumpandrun.commands.*;
import ch.ksrminecraft.jumpandrun.db.DatabaseConnection;
import ch.ksrminecraft.jumpandrun.listeners.*;
import ch.ksrminecraft.jumpandrun.utils.ConfigManager;
import ch.ksrminecraft.jumpandrun.utils.PointsService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Haupteinstiegspunkt des JumpAndRun-Plugins.
 * Initialisiert Listener, Befehle, Datenbank-Verbindungen sowie die Konfiguration.
 */
public final class JumpAndRun extends JavaPlugin {

    /** Globale Plugin-Instanz. */
    private static JumpAndRun plugin;

    /** Zentraler ConfigManager für Einstellungen (debug etc.). */
    private static ConfigManager configManager;

    /** Listener, die Events im Spiel überwachen. */
    private static PressurePlateListener pressurePlatesListener;
    private static FallDownListener fallDownListener;

    /** Statisch referenzierte Startposition (derzeit ungenutzt, für zukünftige Features). */
    private static Location startPosition;

    /** Standardhöhe, auf der Inseln erzeugt werden. */
    public static final int DEFAULT_HEIGHT = 20;
    public static int height = DEFAULT_HEIGHT;

    @Override
    public void onEnable() {
        plugin = this;

        // Config laden
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        if (configManager.isDebug()) {
            getLogger().info("[JNR-DEBUG] Config geladen, Debugmodus aktiv.");
        }

        // JumpAndRun-Datenbank initialisieren
        try {
            DatabaseConnection.initializeWorldTable();
        } catch (RuntimeException e) {
            getLogger().severe("Keine DB gefunden, Plugin fährt runter!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // RankPointsAPI initialisieren (separate Punkte-DB)
        try {
            PointsService.init();
        } catch (Exception e) {
            getLogger().severe("PointsService konnte nicht initialisiert werden! Punktevergabe deaktiviert.");
            e.printStackTrace();
        }

        // Listener initialisieren und registrieren
        pressurePlatesListener = new PressurePlateListener();
        fallDownListener = new FallDownListener();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(pressurePlatesListener, this);
        pm.registerEvents(fallDownListener, this);
        pm.registerEvents(new TestRunAbortListener(), this);
        pm.registerEvents(new CheckpointRespawnListener(), this);
        pm.registerEvents(new WorldLockListener(), this);

        // Command-Dispatcher für /jnr
        if (getCommand("jnr") != null) {
            getCommand("jnr").setExecutor((sender, cmd, label, args) -> {
                if (args.length == 0) {
                    sender.sendMessage("§cUsage: /jnr <create|delete|teleport|list|publish|ready>");
                    return true;
                }
                switch (args[0].toLowerCase()) {
                    case "create":
                        return new JnrCreateCommand(this).onCommand(sender, cmd, label, args);
                    case "delete":
                        return new JnrDeleteCommand().onCommand(sender, cmd, label, args);
                    case "teleport":
                        return new JnrTeleportCommand().onCommand(sender, cmd, label, args);
                    case "list":
                        return new JnrListCommand().onCommand(sender, cmd, label, args);
                    case "ready":
                        return new JnrReadyCommand().onCommand(sender, cmd, label, args);
                    default:
                        sender.sendMessage("§cUsage: /jnr <create|delete|teleport|list|publish|ready>");
                        return true;
                }
            });
        } else {
            getLogger().severe("Befehl /jnr konnte nicht registriert werden (plugin.yml prüfen!)");
        }

        getLogger().info("JumpAndRun Plugin erfolgreich aktiviert.");
    }

    @Override
    public void onDisable() {
        getLogger().info("JumpAndRun Plugin heruntergefahren.");
    }

    // =====================
    // Getter für globale Objekte
    // =====================

    public static JumpAndRun getPlugin() {
        return plugin;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public PressurePlateListener getPressurePlatesListener() {
        return pressurePlatesListener;
    }

    public FallDownListener getFallDownListener() {
        return fallDownListener;
    }

    public static Location getStartPosition() {
        return startPosition;
    }

    public static void setStartPosition(Location location) {
        startPosition = location;
    }
}
