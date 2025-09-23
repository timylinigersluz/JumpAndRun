package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.WorldLockManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Subcommand: /jnr teleport <alias|welt>
 */
public class JnrTeleportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("jumpandrun.teleport")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für /jnr teleport.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /jnr teleport <alias|welt>");
            return true;
        }

        String aliasOrWorld = args[1];

        // Alias → Weltname auflösen
        String worldName = WorldRepository.getWorldByAlias(aliasOrWorld);
        if (worldName == null) {
            worldName = aliasOrWorld; // Fallback: direkter Weltname
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Die JumpAndRun-Welt " + aliasOrWorld + " existiert nicht.");
            return true;
        }

        if (!WorldRepository.isPublished(worldName) && !JumpAndRun.getConfigManager().isDebug()) {
            player.sendMessage(ChatColor.RED + "Dieses JumpAndRun ist noch im Draft-Modus.");
            return true;
        }

        if (!WorldLockManager.tryEnter(worldName, player)) {
            Player occ = WorldLockManager.getOccupant(worldName);
            String occName = (occ != null ? occ.getName() : "jemand anderes");
            player.sendMessage(ChatColor.RED + "Diese Welt wird gerade von " + occName + " gespielt. Bitte warte!");
            return true;
        }

        Location spawn = WorldRepository.getStartLocation(worldName);
        if (spawn == null) {
            player.sendMessage(ChatColor.RED + "Keine Startposition für diese Welt gefunden.");
            WorldLockManager.leave(worldName, player);
            return true;
        }

        // Alias für Anzeige
        String alias = WorldRepository.getAlias(worldName);
        String displayName = (alias != null && !alias.isEmpty()) ? alias : worldName;

        // Teleportposition
        Location tpLoc = spawn.clone().add(0, JumpAndRun.height + 1, 0);

        // Blickrichtung fix auf Osten setzen
        tpLoc.setYaw(-90f);

        player.teleport(tpLoc);
        player.sendMessage(ChatColor.GREEN + "Du wurdest zur JumpAndRun-Insel §e" + displayName + " §a teleportiert!");

        Voraussetzungen:

        return true;
    }
}
