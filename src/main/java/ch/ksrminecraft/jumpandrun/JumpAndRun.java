package ch.ksrminecraft.jumpandrun;

import ch.ksrminecraft.jumpandrun.commands.*;
import ch.ksrminecraft.jumpandrun.db.DatabaseConnection;
import ch.ksrminecraft.jumpandrun.listeners.*;
import ch.ksrminecraft.jumpandrun.utils.ConfigManager;
import ch.ksrminecraft.jumpandrun.utils.PointsService;
import ch.ksrminecraft.jumpandrun.utils.WorldSyncManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Haupteinstiegspunkt des JumpAndRun-Plugins.
 * Initialisiert Listener, Befehle, Datenbank-Verbindungen sowie die Konfiguration.
 */
public final class JumpAndRun extends JavaPlugin {

    private static JumpAndRun plugin;
    private static ConfigManager configManager;

    private static PressurePlateListener pressurePlatesListener;
    private static FallDownListener fallDownListener;

    private static Location startPosition;

    public static final int DEFAULT_HEIGHT = 20;
    public static int height = DEFAULT_HEIGHT;

    @Override
    public void onEnable() {
        plugin = this;

        // --- Config laden ---
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        if (configManager.isDebug()) {
            getLogger().info("[JNR-DEBUG] Config geladen, Debugmodus aktiv.");
        }

        // --- JumpAndRun-Datenbank initialisieren ---
        try {
            getLogger().info("[JNR-DEBUG] Starte Initialisierung der JumpAndRun-DB ...");
            DatabaseConnection.initializeWorldTables();
            getLogger().info("[JNR-DEBUG] DB-Initialisierung abgeschlossen.");

            // 🔹 Leader-Schilder aus DB laden
            ch.ksrminecraft.jumpandrun.utils.SignUpdater.loadAllFromDatabase();
            getLogger().info("[JNR-DEBUG] Leader-Schilder aus DB geladen.");
        } catch (RuntimeException e) {
            getLogger().severe("[JNR-ERROR] Konnte keine Verbindung zur JumpAndRun-DB aufbauen!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // --- RankPointsAPI initialisieren ---
        try {
            getLogger().info("[JNR-DEBUG] Starte Initialisierung der Punkte-DB (RankPointsAPI)...");
            PointsService.init();
            getLogger().info("[JNR-DEBUG] PointsService erfolgreich initialisiert.");
        } catch (Exception e) {
            getLogger().severe("[JNR-ERROR] PointsService konnte nicht initialisiert werden!");
            e.printStackTrace();
        }

        // --- Welten-Abgleich nach Server-Start (verzögert, Multiverse-kompatibel) ---
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                getLogger().info("[JNR-DEBUG] Starte Welten-Abgleich zwischen DB und Server...");
                WorldSyncManager.syncWorlds();
            } catch (Exception e) {
                getLogger().severe("[JNR-ERROR] Fehler beim Welten-Abgleich: " + e.getMessage());
                e.printStackTrace();
            }
        }, 20L * 5);

        // --- Listener registrieren ---
        PluginManager pm = getServer().getPluginManager();
        pressurePlatesListener = new PressurePlateListener();
        fallDownListener = new FallDownListener();

        pm.registerEvents(pressurePlatesListener, this);
        pm.registerEvents(fallDownListener, this);
        pm.registerEvents(new TestRunAbortListener(), this);
        pm.registerEvents(new CheckpointRespawnListener(), this);
        pm.registerEvents(new WorldLockListener(), this);
        pm.registerEvents(new CheckpointCleanupListener(), this);
        pm.registerEvents(new SignListener(), this);
        pm.registerEvents(new WorldSwitchListener(), this);
        pm.registerEvents(new DeathListener(), this);
        pm.registerEvents(new LeaveItemListener(), this);
        pm.registerEvents(new BlockListener(), this);

        // --- AliasPrompt aktivieren ---
        ch.ksrminecraft.jumpandrun.utils.AliasPromptManager.init();

        // --- Command-Dispatcher ---
        if (getCommand("jnr") != null) {
            getCommand("jnr").setExecutor((sender, cmd, label, args) -> {
                if (args.length == 0) {
                    sender.sendMessage("§cUsage: /jnr <create|delete|teleport|list|ready|continue|abort|name|unpublish|reload>");
                    return true;
                }
                switch (args[0].toLowerCase()) {
                    case "create":    return new JnrCreateCommand(this).onCommand(sender, cmd, label, args);
                    case "delete":    return new JnrDeleteCommand().onCommand(sender, cmd, label, args);
                    case "teleport":  return new JnrTeleportCommand().onCommand(sender, cmd, label, args);
                    case "list":      return new JnrListCommand().onCommand(sender, cmd, label, args);
                    case "ready":     return new JnrReadyCommand().onCommand(sender, cmd, label, args);
                    case "continue":  return new JnrContinueCommand().onCommand(sender, cmd, label, args);
                    case "abort":     return new JnrAbortCommand().onCommand(sender, cmd, label, args);
                    case "name":      return new JnrNameCommand().onCommand(sender, cmd, label, args);
                    case "unpublish": return new JnrUnpublishCommand().onCommand(sender, cmd, label, args);
                    case "reload":    return new JnrReloadCommand().onCommand(sender, cmd, label, args);
                    default:
                        sender.sendMessage("§cUsage: /jnr <create|delete|teleport|list|ready|continue|abort|name|unpublish>");
                        return true;
                }
            });
            getCommand("jnr").setTabCompleter(new JnrTabCompleter());
        } else {
            getLogger().severe("Befehl /jnr konnte nicht registriert werden (plugin.yml prüfen!)");
        }

        // --- Leader-Schilder beim Start synchronisieren & aktualisieren ---
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                getLogger().info("[JNR-DEBUG] Starte Leader-Sign-Sync...");

                for (World world : Bukkit.getWorlds()) {
                    // Nur Lobby + veröffentlichte JumpAndRuns
                    if (world.getName().equalsIgnoreCase("world") ||
                            ch.ksrminecraft.jumpandrun.db.WorldRepository.exists(world.getName())) {
                        ch.ksrminecraft.jumpandrun.utils.SignUpdater.syncWorldSigns(world);
                    }
                }

                getLogger().info("[JNR] Leader-Schilder synchronisiert und aktualisiert.");
            } catch (Exception e) {
                getLogger().warning("[JNR] Fehler beim Leader-Sign-Sync: " + e.getMessage());
                e.printStackTrace();
            }
        }, 20L * 10); // 10 Sekunden nach Serverstart (Multiverse-kompatibel)

        getLogger().info("JumpAndRun Plugin erfolgreich aktiviert.");
    }

    @Override
    public void onDisable() {
        getLogger().info("JumpAndRun Plugin heruntergefahren.");
    }

    // --- Getter und Setter---
    public static JumpAndRun getPlugin() { return plugin; }
    public static ConfigManager getConfigManager() { return configManager; }
    public PressurePlateListener getPressurePlatesListener() { return pressurePlatesListener; }
    public FallDownListener getFallDownListener() { return fallDownListener; }
    public static Location getStartPosition() { return startPosition; }
    public static void setStartPosition(Location location) { startPosition = location; }
    public static void setConfigManager(ConfigManager manager) {configManager = manager; }
}
