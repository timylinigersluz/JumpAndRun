package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.listeners.WorldSwitchListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Subcommand: /jnr continue <welt>
 * Lässt den Ersteller eine seiner Draft-Welten wieder betreten.
 */
public class JnrContinueCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Verwendung: /jnr continue <welt>");
            return true;
        }

        String worldName = args[1];

        // prüfen ob Welt existiert
        if (!WorldRepository.exists(worldName)) {
            player.sendMessage(ChatColor.RED + "Diese JumpAndRun-Welt existiert nicht.");
            return true;
        }

        // prüfen ob Spieler Ersteller ist
        String creator = WorldRepository.getCreator(worldName);
        if (creator == null || !creator.equals(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "Du bist nicht der Ersteller dieser Welt.");
            return true;
        }

        // prüfen ob Welt veröffentlicht ist
        if (WorldRepository.isPublished(worldName)) {
            player.sendMessage(ChatColor.RED + "Diese Welt ist bereits veröffentlicht und kann nicht fortgesetzt werden.");
            return true;
        }

        // Welt laden
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            world = Bukkit.createWorld(new org.bukkit.WorldCreator(worldName));
        }

        if (world == null) {
            player.sendMessage(ChatColor.RED + "Fehler beim Laden der Welt.");
            return true;
        }

        // Herkunft speichern
        WorldSwitchListener.setOrigin(player, player.getLocation());

        // Spieler teleportieren
        Location tp = WorldRepository.getStartLocation(worldName);
        if (tp == null) tp = world.getSpawnLocation();
        player.teleport(tp.add(0, 1, 0));

        player.sendMessage(ChatColor.GREEN + "Du bist wieder in deiner JumpAndRun-Welt " + worldName + ".");
        return true;
    }
}
