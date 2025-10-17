package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.listeners.WorldSwitchListener;
import ch.ksrminecraft.jumpandrun.utils.TestRunManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Subcommand: /jnr abort <keepworld|deleteworld>
 * Bricht einen Draft/Test ab und bringt den Spieler zurück.
 */
public class JnrAbortCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen.");
            return true;
        }

        Player player = (Player) sender;

        // --- Haupt-Permission prüfen ---
        if (!player.hasPermission("jumpandrun.abort")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, JumpAndRun-Abbrüche durchzuführen.");
            return true;
        }

        // --- Keine Option angegeben ---
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "❓ Möchtest du deine Welt behalten oder löschen?");
            player.sendMessage(ChatColor.GRAY + " → /jnr abort keepworld   " + ChatColor.DARK_GRAY + "(Welt als Draft behalten)");
            player.sendMessage(ChatColor.GRAY + " → /jnr abort deleteworld " + ChatColor.DARK_GRAY + "(Welt komplett löschen)");
            return true;
        }

        String action = args[1].toLowerCase();
        World currentWorld = player.getWorld();
        String worldName = currentWorld.getName();

        // --- Nur Draft-Welten dürfen abgebrochen werden ---
        if (WorldRepository.isPublished(worldName)) {
            player.sendMessage(ChatColor.RED + "Diese Welt ist bereits veröffentlicht und kann nicht mit /jnr abort abgebrochen werden.");
            return true;
        }

        // --- Spieler zurück zur Lobby/Ursprungswelt ---
        Location origin = WorldSwitchListener.getOrigin(player);
        if (origin != null) {
            player.teleport(origin);
            WorldSwitchListener.clearOrigin(player);
        } else {
            World defaultWorld = Bukkit.getWorld("world");
            if (defaultWorld != null) {
                player.teleport(defaultWorld.getSpawnLocation());
            }
        }

        // --- Falls der Spieler gerade testet ---
        if (TestRunManager.isTesting(player)) {
            TestRunManager.abortTest(player);
        }

        // === Option: Welt löschen ===
        if (action.equals("deleteworld")) {
            if (!player.hasPermission("jumpandrun.abort.deleteworld")) {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, Welten zu löschen.");
                return true;
            }

            // Welt deregistrieren (Multiverse) + entladen
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv remove " + worldName);
            boolean unloaded = Bukkit.unloadWorld(worldName, false);
            if (unloaded) {
                Bukkit.getConsoleSender().sendMessage("[JNR] Welt " + worldName + " entladen und aus Multiverse entfernt.");
            }

            // DB-Eintrag löschen
            if (WorldRepository.exists(worldName)) {
                WorldRepository.removeWorld(worldName);
                Bukkit.getConsoleSender().sendMessage("[JNR-DB] Welt " + worldName + " aus der DB entfernt.");
            }

            player.sendMessage(ChatColor.RED + "❌ Dein Testlauf wurde abgebrochen und die Welt §e" + worldName + ChatColor.RED + " gelöscht.");
            Bukkit.getConsoleSender().sendMessage("[JNR] Spieler " + player.getName() + " hat seine Welt '" + worldName + "' gelöscht.");
            return true;
        }

        // === Option: Welt behalten ===
        if (action.equals("keepworld")) {
            if (!player.hasPermission("jumpandrun.abort.keepworld")) {
                player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, Welten als Draft zu behalten.");
                return true;
            }

            player.sendMessage(ChatColor.GREEN + "✔ Dein Testlauf wurde abgebrochen. Die Welt §e" + worldName + ChatColor.GREEN + " bleibt als Draft bestehen.");
            Bukkit.getConsoleSender().sendMessage("[JNR] Spieler " + player.getName() + " hat seinen Test abgebrochen (Welt behalten: " + worldName + ").");
            return true;
        }

        // --- Ungültige Option ---
        player.sendMessage(ChatColor.RED + "Ungültige Option. Nutze /jnr abort <keepworld|deleteworld>");
        return true;
    }
}
