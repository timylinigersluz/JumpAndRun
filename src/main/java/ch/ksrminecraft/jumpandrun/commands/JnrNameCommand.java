package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.AliasPromptManager;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Befehl: /jnr name <alias>
 * Vergibt einen Anzeigenamen (Alias) für eine JumpAndRun-Welt.
 * Leerzeichen oder ungültige Zeichen werden abgefangen.
 */
public class JnrNameCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen.");
            return true;
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Verwendung: /jnr name <alias>");
            return true;
        }

        // === Alias aus Argumenten lesen ===
        String alias = String.join(" ", args).trim();

        // === Eingabeprüfung ===
        if (!isAliasValid(alias, player)) {
            return true;
        }

        // === Falls Prompt offen → bevorzugt nutzen ===
        if (AliasPromptManager.isAwaiting(player)) {
            String worldName = AliasPromptManager.consumeAwaiting(player);
            AliasPromptManager.handleAlias(player, worldName, alias);
            return true;
        }

        // === Normale Variante ===
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

    /**
     * Prüft, ob ein Alias gültig ist.
     */
    private boolean isAliasValid(String alias, Player player) {
        if (alias.length() < 3 || alias.length() > 20) {
            player.sendMessage(ChatColor.RED + "Der Name muss zwischen 3 und 20 Zeichen lang sein.");
            return false;
        }

        if (alias.contains(" ")) {
            player.sendMessage(ChatColor.RED + "Ungültiger Name! Bitte verwende keine Leerzeichen.");
            return false;
        }

        if (!alias.matches("^[A-Za-z0-9_-]+$")) {
            player.sendMessage(ChatColor.RED + "Ungültiger Name! Erlaubt sind nur Buchstaben, Zahlen, Unterstrich und Bindestrich.");
            return false;
        }

        return true;
    }
}
