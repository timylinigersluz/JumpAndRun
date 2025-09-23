package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldSyncManager {

    public static void syncWorlds() {
        // Alle DB-Welten
        List<String> dbWorlds = WorldRepository.getAllWorlds();
        Set<String> dbWorldSet = new HashSet<>(dbWorlds);

        // Alle Ordner im Server-Verzeichnis
        File worldContainer = Bukkit.getWorldContainer();
        File[] dirs = worldContainer.listFiles(File::isDirectory);

        if (dirs == null) return;

        for (File dir : dirs) {
            String folderName = dir.getName();

            // Nur JnR-Welten beachten
            if (!folderName.startsWith("JumpAndRun_")) continue;

            // 1. Falls noch in DB → nichts machen
            if (dbWorldSet.contains(folderName)) continue;

            // 2. Falls gerade geladen → nichts machen
            World bukkitWorld = Bukkit.getWorld(folderName);
            if (bukkitWorld != null) continue;

            // 3. Ansonsten löschen
            try {
                deleteDirectory(dir);
                Bukkit.getConsoleSender().sendMessage("[JNR] Verwaiste Welt " + folderName + " wurde vom Server entfernt.");
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage("[JNR-ERROR] Konnte Weltordner " + folderName + " nicht löschen: " + e.getMessage());
            }
        }

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Welten-Abgleich abgeschlossen (inkl. Aufräumen verwaister Ordner).");
        }
    }

    private static void deleteDirectory(File dir) throws IOException {
        if (!dir.exists()) return;
        Files.walk(dir.toPath())
                .map(java.nio.file.Path::toFile)
                .sorted((a, b) -> -a.compareTo(b)) // erst Dateien, dann Ordner
                .forEach(File::delete);
    }
}
