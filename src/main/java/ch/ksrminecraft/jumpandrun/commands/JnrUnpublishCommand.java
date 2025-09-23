package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JnrUnpublishCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl ausführen.");
            return true;
        }

        Player player = (Player) sender;

        // Berechtigung prüfen
        if (!player.hasPermission("jumpandrun.unpublish") && !player.hasPermission("jumpandrun.manage")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, diesen Befehl auszuführen.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Verwendung: /jnr unpublish <welt|alias>");
            return true;
        }

        String input = args[1];
        String worldName = null;

        // 1. direkt prüfen ob Welt mit diesem Namen existiert
        if (WorldRepository.exists(input)) {
            worldName = input;
        } else {
            // 2. Alias auflösen → internen Weltnamen finden
            for (String candidate : WorldRepository.getPublishedWorlds()) {
                String alias = WorldRepository.getAlias(candidate);
                if (alias != null && alias.equalsIgnoreCase(input)) {
                    worldName = candidate;
                    break;
                }
            }
        }

        if (worldName == null) {
            player.sendMessage(ChatColor.RED + "Keine veröffentlichte Welt oder Alias '" + input + "' gefunden.");
            return true;
        }

        // prüfen ob Welt veröffentlicht ist
        if (!WorldRepository.isPublished(worldName)) {
            player.sendMessage(ChatColor.YELLOW + "Diese Welt ist bereits nicht veröffentlicht.");
            return true;
        }

        // zurücksetzen
        WorldRepository.setPublished(worldName, false);
        WorldRepository.setReady(worldName, false);

        String alias = WorldRepository.getAlias(worldName);
        String displayName = (alias != null && !alias.isEmpty()) ? alias : worldName;

        player.sendMessage(ChatColor.GREEN + "✔ Die Welt §e" + displayName + "§a ist jetzt wieder im Entwurfsmodus.");
        Bukkit.getConsoleSender().sendMessage("[JNR] Welt " + worldName + " (" + displayName + ") wurde von "
                + player.getName() + " zurück in den Draft-Modus versetzt.");

        return true;
    }
}
