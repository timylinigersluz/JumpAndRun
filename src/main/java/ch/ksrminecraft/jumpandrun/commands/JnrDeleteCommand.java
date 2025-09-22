package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Subcommand: /jnr delete <weltname>
 */
public class JnrDeleteCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden.");
            return true;
        }
        Player player = (Player) sender;

        // Permission-Check
        if (!player.hasPermission("jumpandrun.delete")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für /jnr delete.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Verwendung: /jnr delete <weltname>");
            return true;
        }

        String worldName = args[1];

        // Spieler zurück in Main-World
        World mainWorld = Bukkit.getWorld("world");
        if (mainWorld != null) {
            player.teleport(mainWorld.getSpawnLocation());
        }

        // Welt löschen
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
            WorldRepository.removeWorld(world.getName());

            // Ordner löschen
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            try {
                deleteDirectory(worldFolder);
                player.sendMessage(ChatColor.GREEN + "JumpAndRun Welt " + worldName + " vollständig gelöscht.");
                Bukkit.getConsoleSender().sendMessage("[JNR] Welt " + worldName + " und Dateien gelöscht.");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Welt entladen, aber Dateien konnten nicht gelöscht werden.");
                e.printStackTrace();
            }
        } else {
            player.sendMessage(ChatColor.RED + "Die Welt " + worldName + " existiert nicht.");
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
