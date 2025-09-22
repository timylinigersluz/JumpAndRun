package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.TestRunManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Subcommand: /jnr ready
 * Setzt den Ersteller in den Testmodus: Survival + Timer
 */
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

        // Startposition holen
        Location start = WorldRepository.getStartLocation(world.getName());
        if (start == null) {
            player.sendMessage(ChatColor.RED + "Startposition konnte nicht geladen werden.");
            return true;
        }

        // Spieler in Survival setzen und zum Start teleportieren
        player.teleport(start.clone().add(0, JumpAndRun.height + 1, 0));
        player.setGameMode(GameMode.SURVIVAL);

        // TestRun starten
        TestRunManager.startTest(player);

        player.sendMessage(ChatColor.GREEN + "Testmodus gestartet! Spiele dein eigenes JumpAndRun in Survival durch.");
        Bukkit.getConsoleSender().sendMessage("[JNR] Spieler " + player.getName() + " testet Welt " + world.getName());

        return true;
    }
}
