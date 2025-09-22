package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.IslandGenerator;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Subcommand: /jnr create <länge>
 */
public class JnrCreateCommand implements CommandExecutor {

    private final JumpAndRun plugin;

    public JnrCreateCommand(JumpAndRun plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden.");
            return true;
        }
        Player player = (Player) sender;

        // Permission-Check
        if (!player.hasPermission("jumpandrun.create")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für /jnr create.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Verwendung: /jnr create <länge>");
            return true;
        }

        // Länge parsen
        int length = 15;
        try {
            length = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.YELLOW + "Ungültige Zahl. Standardwert 15 wird verwendet.");
        }

        // Dynamischer Weltname
        String worldId = "JumpAndRun_" + player.getName() + "_" + UUID.randomUUID().toString().substring(0, 8);

        // Welt laden/erstellen
        WorldCreator creator = new WorldCreator(worldId)
                .environment(World.Environment.NORMAL)
                .type(WorldType.FLAT)
                .generateStructures(false);

        World world = Bukkit.createWorld(creator);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Fehler beim Erstellen der Welt!");
            return true;
        }

        // Inseln erstellen
        Location start = new Location(world, -(length / 2) - 10, 100, 0);
        IslandGenerator.createFloatingIslandStart(start, 10, JumpAndRun.height);

        Location goal = new Location(world, (length / 2) + 10, 100, 0);
        IslandGenerator.createFloatingIslandGoal(goal, 10, JumpAndRun.height);

        // Datenbank registrieren
        WorldRepository.registerWorld(start, JumpAndRun.height, player.getUniqueId().toString());


        // Teleport Spieler
        player.teleport(start.clone().add(0, JumpAndRun.height + 1, 0));
        player.setGameMode(GameMode.CREATIVE);

        player.sendMessage(ChatColor.GREEN + "JumpAndRun Welt " + worldId + " erstellt und Inseln generiert!");
        Bukkit.getConsoleSender().sendMessage("[JNR] Welt " + worldId + " wurde erstellt.");

        return true;
    }
}
