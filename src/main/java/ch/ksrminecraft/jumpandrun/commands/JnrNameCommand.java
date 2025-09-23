package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.AliasPromptManager;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JnrNameCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen.");
            return true;
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Verwendung: /jnr name <alias>");
            return true;
        }

        String alias = String.join(" ", args).substring(args[0].length()).trim();

        // Falls Prompt offen → bevorzugt nutzen
        if (AliasPromptManager.isAwaiting(player)) {
            String worldName = AliasPromptManager.consumeAwaiting(player);
            AliasPromptManager.handleAlias(player, worldName, alias);
            return true;
        }

        // Normale Variante
        String worldName = world.getName();

        if (!WorldRepository.exists(worldName)) {
            player.sendMessage(ChatColor.RED + "Diese Welt ist kein JumpAndRun.");
            return true;
        }

        if (WorldRepository.getAlias(worldName) != null) {
            player.sendMessage(ChatColor.RED + "Für diese Welt wurde bereits ein Name vergeben.");
            return true;
        }

        AliasPromptManager.handleAlias(player, worldName, alias);
        return true;
    }
}
