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
            // Subcommands vorschlagen
            for (String sub : SUBCOMMANDS) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    suggestions.add(sub);
                }
            }
            return suggestions;
        }

        if (args.length == 2 && sender instanceof Player) {
            Player player = (Player) sender;

            switch (args[0].toLowerCase()) {
                case "create":
                    suggestions.addAll(Arrays.asList("25", "50", "75", "100"));
                    break;

                case "teleport":
                case "delete":
                    try {
                        for (String world : WorldRepository.getPublishedWorlds()) {
                            String aliasName = WorldRepository.getAlias(world);
                            if (aliasName != null && !aliasName.isEmpty()) {
                                suggestions.add(aliasName); // Alias bevorzugen
                            } else {
                                suggestions.add(world); // Fallback: interner Name
                            }
                        }
                    } catch (Exception e) {
                        suggestions.add("<keine veröffentlichten Welten>");
                    }
                    break;

                case "continue":
                    try {
                        suggestions.addAll(WorldRepository.getDraftWorldsOf(player.getUniqueId()));
                    } catch (Exception e) {
                        suggestions.add("<keine Draft-Welten>");
                    }
                    break;

                case "abort":
                    suggestions.addAll(Arrays.asList("keepworld", "deleteworld"));
                    break;
            }
        }

        return suggestions;
    }
}
