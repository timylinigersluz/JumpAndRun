package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
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

        String worldName = world.getName();

        // prüfen ob es eine JnR-Welt ist
        if (!WorldRepository.exists(worldName)) {
            player.sendMessage(ChatColor.RED + "Diese Welt ist kein JumpAndRun.");
            return true;
        }

        // prüfen ob Spieler Ersteller ist
        String creator = WorldRepository.getCreator(worldName);
        if (creator == null || !creator.equals(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "Nur der Ersteller dieser Welt darf einen Namen vergeben.");
            return true;
        }

        // prüfen ob schon ein Alias existiert
        if (WorldRepository.getAlias(worldName) != null) {
            player.sendMessage(ChatColor.RED + "Für diese Welt wurde bereits ein Name vergeben.");
            return true;
        }

        // Alias zusammensetzen
        String alias = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)).trim();

        if (alias.length() < 3 || alias.length() > 20) {
            player.sendMessage(ChatColor.RED + "Der Name muss zwischen 3 und 20 Zeichen lang sein.");
            return true;
        }

        // Alias setzen
        WorldRepository.setAlias(worldName, alias);

        // Feedback für Spieler
        player.sendMessage(ChatColor.GREEN + "✔ Dein JumpAndRun heißt jetzt: §e" + alias);
        player.sendMessage(ChatColor.YELLOW + "Platziere ein Schild und schreibe:");
        player.sendMessage(ChatColor.AQUA + "  Zeile 1: [JNR]");
        player.sendMessage(ChatColor.AQUA + "  Zeile 2: " + alias);

        // Konsole informieren
        Bukkit.getConsoleSender().sendMessage("[JNR] Alias für Welt '" + worldName + "' gesetzt: " + alias);

        return true;
    }
}
