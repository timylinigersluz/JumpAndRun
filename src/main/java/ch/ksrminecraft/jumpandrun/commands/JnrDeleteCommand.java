package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.WorldManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Subcommand: /jnr delete <alias>
 */
public class JnrDeleteCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("jumpandrun.delete")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für /jnr delete.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Verwendung: /jnr delete <alias>");
            return true;
        }

        String aliasOrWorld = args[1];

        // Alias → Weltname auflösen
        String worldName = WorldRepository.getWorldByAlias(aliasOrWorld);
        if (worldName == null) {
            worldName = aliasOrWorld; // Fallback: direkter Weltname
        }

        // Für Anzeige den Alias nehmen, falls vorhanden
        String alias = WorldRepository.getAlias(worldName);
        String displayName = (alias != null && !alias.isEmpty()) ? alias : worldName;

        // Spieler zurück in Main-World teleportieren
        World mainWorld = Bukkit.getWorld("world");
        if (mainWorld != null) {
            player.teleport(mainWorld.getSpawnLocation());
        }

        // Welt entladen & löschen
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
            WorldRepository.removeWorld(worldName);

            // auch in Multiverse-Core deregistrieren
            try {
                MultiverseCoreApi mvApi = MultiverseCoreApi.get();
                WorldManager wm = mvApi.getWorldManager();
                wm.removeWorld(worldName);
            } catch (Exception ignored) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DB] Konnte Welt " + worldName + " nicht aus MV-Core deregistrieren.");
            }

            // Ordner löschen
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            try {
                deleteDirectory(worldFolder);
                player.sendMessage(ChatColor.GREEN + "JumpAndRun §e" + displayName + ChatColor.GREEN + " wurde vollständig gelöscht.");
                Bukkit.getConsoleSender().sendMessage("[JNR] Welt " + worldName + " (Alias=" + displayName + ") und Dateien gelöscht.");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Welt entladen, aber Dateien konnten nicht gelöscht werden.");
                e.printStackTrace();
            }
        } else {
            player.sendMessage(ChatColor.RED + "Die Welt §e" + displayName + ChatColor.RED + " existiert nicht.");
        }

        return true;
    }

    private void deleteDirectory(File dir) throws IOException {
        if (!dir.exists()) return;
        Files.walk(dir.toPath())
                .map(java.nio.file.Path::toFile)
                .sorted((a, b) -> -a.compareTo(b)) // erst Dateien, dann Ordner
                .forEach(File::delete);
    }
}
