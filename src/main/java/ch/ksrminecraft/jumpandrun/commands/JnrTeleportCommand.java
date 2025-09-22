package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Subcommand: /jnr teleport
 */
public class JnrTeleportCommand implements CommandExecutor {

    private final String WORLD_ID = "JumpAndRun";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden.");
            return true;
        }
        Player player = (Player) sender;

        // Permission-Check
        if (!player.hasPermission("jumpandrun.teleport")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für /jnr teleport.");
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler "
                        + player.getName() + " hat versucht /jnr teleport ohne Berechtigung auszuführen.");
            }
            return true;
        }

        World world = Bukkit.getWorld(WORLD_ID);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Die JumpAndRun Welt existiert nicht.");
            return true;
        }

// Published-Check
        if (!WorldRepository.isPublished(world.getName()) && !JumpAndRun.getConfigManager().isDebug()) {
            player.sendMessage(ChatColor.RED + "Dieses JumpAndRun ist noch im Draft-Modus.");
            return true;
        }

        Location spawn = world.getSpawnLocation().add(0, JumpAndRun.height + 1, 0);
        player.teleport(spawn);
        player.sendMessage(ChatColor.GREEN + "Du wurdest zur JumpAndRun-Insel teleportiert!");

        return true;
    }
}
