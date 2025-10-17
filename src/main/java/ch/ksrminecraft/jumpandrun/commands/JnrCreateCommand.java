package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.IslandGenerator;
import ch.ksrminecraft.jumpandrun.utils.ItemKitManager;
import ch.ksrminecraft.jumpandrun.utils.WorldUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Subcommand: /jnr create <länge>
 * Erstellt eine neue JumpAndRun-Welt für den Spieler.
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

        // Welt erstellen & konfigurieren
        World world = WorldUtils.loadOrRegisterWorld(worldId, World.Environment.NORMAL, WorldType.FLAT);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Fehler beim Erstellen der Welt!");
            return true;
        }
        WorldUtils.configureWorld(world);

        // Inseln generieren
        Location start = new Location(world, -(length / 2) - 10, 100, 0);
        Location goal = new Location(world, (length / 2) + 10, 100, 0);

        IslandGenerator.createFloatingIslandStart(start, 10, JumpAndRun.height);
        IslandGenerator.createFloatingIslandGoal(goal, 10, JumpAndRun.height);

        // Welt in DB registrieren
        WorldRepository.registerWorld(start, JumpAndRun.height, player.getUniqueId().toString());

        // Teleport nach 1 Tick (damit Welt und DB-Write vollständig abgeschlossen sind)
        Bukkit.getScheduler().runTask(plugin, () -> {
            Location tpLoc = start.clone().add(0, JumpAndRun.height + 1, 0);
            tpLoc.setDirection(goal.toVector().subtract(tpLoc.toVector()));

            player.teleport(tpLoc);
            player.setGameMode(GameMode.CREATIVE);

            // Standard-Bauset
            ItemKitManager.giveBuildKit(player);

            // Sound + Partikel
            player.playSound(tpLoc, Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
            player.spawnParticle(Particle.CLOUD, tpLoc, 30, 0.3, 0.3, 0.3, 0.01);

            // Info
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "🎯 Du bist jetzt in deiner neuen Jump & Run Welt!");
            player.sendMessage(ChatColor.GRAY + "Baue dein Jump & Run mit den erhaltenen Blöcken.");
            player.sendMessage(ChatColor.GRAY + "Platziere die " + ChatColor.GOLD + "Checkpoint-Platte" + ChatColor.GRAY + " an wichtigen Stellen.");
            player.sendMessage(ChatColor.DARK_AQUA + "Wenn du fertig bist, nutze " + ChatColor.GREEN + "/jnr ready" + ChatColor.DARK_AQUA + " um dein Jump & Run zu veröffentlichen.");
            player.sendMessage("");

            Bukkit.getConsoleSender().sendMessage("[JNR] Welt " + worldId + " wurde erstellt.");
        });

        return true;
    }
}
