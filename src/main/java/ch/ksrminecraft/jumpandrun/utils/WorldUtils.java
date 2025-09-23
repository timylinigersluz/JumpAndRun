package ch.ksrminecraft.jumpandrun.utils;

import org.bukkit.*;
import org.bukkit.World.Environment;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;
import org.mvplugins.multiverse.core.world.options.ImportWorldOptions;
import org.mvplugins.multiverse.core.world.options.LoadWorldOptions;
import org.mvplugins.multiverse.external.vavr.control.Option; // <- Wichtig!

import java.nio.file.Files;
import java.nio.file.Path;

public final class WorldUtils {

    private WorldUtils() {}

    public static World loadOrRegisterWorld(String worldId, Environment env, WorldType type) {
        try {
            MultiverseCoreApi mvApi = MultiverseCoreApi.get();
            WorldManager wm = mvApi.getWorldManager();

            Option<MultiverseWorld> mvWorld = wm.getWorld(worldId);
            if (mvWorld.isDefined()) {
                if (!wm.isLoadedWorld(worldId)) {
                    wm.loadWorld(LoadWorldOptions.world(mvWorld.get()));
                }
            } else {
                Path worldFolder = Bukkit.getWorldContainer().toPath().resolve(worldId);
                boolean hasFolder = Files.exists(worldFolder);

                if (hasFolder) {
                    wm.importWorld(
                            ImportWorldOptions.worldName(worldId)
                                    .environment(env)
                                    .doFolderCheck(true)
                                    .useSpawnAdjust(true)
                    );
                } else {
                    wm.createWorld(
                            CreateWorldOptions.worldName(worldId)
                                    .environment(env)
                                    .worldType(type)
                                    .generateStructures(false)
                                    .useSpawnAdjust(true)
                    );
                }
            }

            Option<LoadedMultiverseWorld> loaded = wm.getLoadedWorld(worldId);
            if (loaded.isDefined()) {
                return loaded.get().getBukkitWorld().getOrNull();
            }

            // Fallback
            World bukkitWorld = Bukkit.getWorld(worldId);
            if (bukkitWorld != null) {
                return bukkitWorld;
            }

            return Bukkit.createWorld(new WorldCreator(worldId)
                    .environment(env)
                    .type(type)
                    .generateStructures(false));

        } catch (Throwable t) {
            Bukkit.getLogger().warning("[WorldUtils] Fehler oder kein Multiverse-Core verfügbar: " + t.getMessage());
            return Bukkit.createWorld(new WorldCreator(worldId)
                    .environment(env)
                    .type(type)
                    .generateStructures(false));
        }
    }

    public static void configureWorld(World world) {
        if (world == null) return;

        // Zeit & Wetter fixieren
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(6000);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setStorm(false);
        world.setThundering(false);

        // Keine Monster & PvE deaktiviert
        world.setSpawnFlags(false, false);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);

        // Spielerfreundlich
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);

        // Performance & Cleanups
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);

        // Debug-Ausgabe
        Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Welt " + world.getName()
                + " wurde konfiguriert: fixer Tag, kein Wetter, keine Mobs, PvE aus, Spielerfreundliche Regeln aktiv.");
    }
}
