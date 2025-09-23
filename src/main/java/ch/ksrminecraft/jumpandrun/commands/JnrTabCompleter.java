package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TabCompleter für den /jnr Befehl.
 */
public class JnrTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "create", "delete", "teleport", "list", "ready", "continue", "abort", "name"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Vorschläge für Subcommands
            for (String sub : SUBCOMMANDS) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    suggestions.add(sub);
                }
            }
            return suggestions;
        }

        if (args.length == 2) {
            if (!(sender instanceof Player)) return suggestions;

            switch (args[0].toLowerCase()) {
                case "create":
                    suggestions.addAll(Arrays.asList("25", "50", "75", "100"));
                    break;

                case "teleport":
                case "delete":
                    try {
                        // Alias statt Weltname vorschlagen
                        for (String world : WorldRepository.getPublishedWorlds()) {
                            String aliasName = WorldRepository.getAlias(world);
                            suggestions.add((aliasName != null && !aliasName.isEmpty()) ? aliasName : world);
                        }
                    } catch (Exception e) {
                        suggestions.add("<keine veröffentlichten Welten>");
                    }
                    break;

                case "continue":
                    try {
                        suggestions.addAll(WorldRepository.getDraftWorldsOf(((Player) sender).getUniqueId()));
                    } catch (Exception e) {
                        suggestions.add("<keine Draft-Welten>");
                    }
                    break;

                case "abort":
                    // Direkt die Optionen anbieten
                    suggestions.addAll(Arrays.asList("keepworld", "deleteworld"));
                    break;
            }
            return suggestions;
        }

        return suggestions;
    }
}
