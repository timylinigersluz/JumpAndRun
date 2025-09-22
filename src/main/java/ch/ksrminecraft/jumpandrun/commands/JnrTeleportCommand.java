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
 * Subcommand: /jnr teleport <welt>
 */
public class JnrTeleportCommand implements CommandExecutor {

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

        // Argument prüfen
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /jnr teleport <welt>");
            return true;
        }
        String worldName = args[1];

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Die JumpAndRun-Welt " + worldName + " existiert nicht.");
            return true;
        }

        // Published-Check
        if (!WorldRepository.isPublished(worldName) && !JumpAndRun.getConfigManager().isDebug()) {
            player.sendMessage(ChatColor.RED + "Dieses JumpAndRun ist noch im Draft-Modus.");
            return true;
        }

        // Lock-Check
        if (!WorldLockManager.tryEnter(worldName, player)) {
            Player occ = WorldLockManager.getOccupant(worldName);
            String occName = (occ != null ? occ.getName() : "jemand anderes");
            player.sendMessage(ChatColor.RED + "Diese Welt wird gerade von " + occName + " gespielt. Bitte warte!");
            return true;
        }

        // Start-Location aus DB
        Location spawn = WorldRepository.getStartLocation(worldName);
        if (spawn == null) {
            player.sendMessage(ChatColor.RED + "Keine Startposition für diese Welt gefunden.");
            // Lock sofort wieder freigeben
            WorldLockManager.leave(worldName, player);
            return true;
        }

        // Teleport
        player.teleport(spawn.clone().add(0, JumpAndRun.height + 1, 0));
        player.sendMessage(ChatColor.GREEN + "Du wurdest zur JumpAndRun-Insel " + worldName + " teleportiert!");

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler "
                    + player.getName() + " wurde in Welt " + worldName + " teleportiert.");
        }

        return true;
    }
}
