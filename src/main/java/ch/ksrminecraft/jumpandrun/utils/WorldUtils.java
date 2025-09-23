package ch.ksrminecraft.jumpandrun.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
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
}
