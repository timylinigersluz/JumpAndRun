package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.TestRunManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JnrReadyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern ausgeführt werden.");
            return true;
        }
        Player player = (Player) sender;
        World world = player.getWorld();

        // Prüfen ob Welt registriert ist
        if (!WorldRepository.exists(world.getName())) {
            player.sendMessage(ChatColor.RED + "Diese Welt ist kein JumpAndRun.");
            return true;
        }

        // Prüfen ob Spieler der Ersteller ist
        String creator = WorldRepository.getCreator(world.getName());
        if (creator == null || !creator.equals(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "Nur der Ersteller dieser Welt kann /jnr ready ausführen.");
            return true;
        }

        // Startposition aus DB laden
        Location start = WorldRepository.getStartLocation(world.getName());
        if (start == null) {
            player.sendMessage(ChatColor.RED + "Startposition konnte nicht geladen werden.");
            return true;
        }

        // Lobby-Location merken
        JumpAndRun.getPlugin().getConfig().set("players." + player.getUniqueId() + ".lobbyLocation", player.getLocation());
        JumpAndRun.getPlugin().saveConfig();

        // Teleport in Mitte der Startinsel
        Location tpLoc = start.clone().add(0, JumpAndRun.height + 1, 0);

        // Blickrichtung: grob zur Zielinsel
        Location goal = new Location(world, start.getX() + 20, start.getY(), start.getZ());
        tpLoc.setDirection(goal.toVector().subtract(tpLoc.toVector()));

        player.teleport(tpLoc);
        player.setGameMode(GameMode.ADVENTURE);

        // TestRun setzen, aber Zeit NICHT starten
        TestRunManager.startTest(player);

        // Feedback
        player.sendMessage(ChatColor.GREEN + "✔ Testmodus gestartet!");
        player.sendMessage(ChatColor.GRAY + "Die Zeit startet erst, wenn du die §eStartdruckplatte§7 betrittst.");
        player.sendMessage(ChatColor.AQUA + "Bitte gib deinem JumpAndRun jetzt einen Namen:");
        player.sendMessage(ChatColor.YELLOW + "→ Verwende dazu §f/jnr name <alias>");
        player.sendMessage(ChatColor.GRAY + "   (Dieser Name wird später auf Schildern und in Listen angezeigt.)");

        Bukkit.getConsoleSender().sendMessage("[JNR] Spieler " + player.getName() + " testet Welt " + world.getName());

        return true;
    }
}
