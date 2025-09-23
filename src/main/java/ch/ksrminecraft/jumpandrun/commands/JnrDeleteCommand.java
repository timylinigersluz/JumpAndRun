package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.world.reasons.DeleteFailureReason;

import java.util.Collections;

public class JnrDeleteCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("jumpandrun.delete")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für /jnr delete.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Verwendung: /jnr delete <alias>");
            return true;
        }

        String aliasOrWorld = args[1];

        // Alias → Weltname auflösen
        String worldName = WorldRepository.getWorldByAlias(aliasOrWorld);
        if (worldName == null) {
            worldName = aliasOrWorld;
        }

        // Für Anzeige den Alias nehmen, falls vorhanden
        String alias = WorldRepository.getAlias(worldName);
        String displayName = (alias != null && !alias.isEmpty()) ? alias : worldName;

        // Spieler zurück in Main-World teleportieren
        World mainWorld = Bukkit.getWorld("world");
        if (mainWorld != null) {
            player.teleport(mainWorld.getSpawnLocation());
        }

        try {
            MultiverseCoreApi mvApi = MultiverseCoreApi.get();
            WorldManager wm = mvApi.getWorldManager();

            var optMvWorld = wm.getWorld(worldName);
            if (optMvWorld.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Die Welt §e" + displayName + ChatColor.RED + " ist Multiverse nicht bekannt.");
                return true;
            }
            MultiverseWorld mvWorld = optMvWorld.get();

            DeleteWorldOptions options = DeleteWorldOptions.world(mvWorld)
                    .keepFiles(Collections.emptyList());

            Attempt<String, DeleteFailureReason> attempt = wm.deleteWorld(options);

            if (attempt.isSuccess()) {
                WorldRepository.removeWorld(worldName);
                player.sendMessage(ChatColor.GREEN + "JumpAndRun §e" + displayName + ChatColor.GREEN + " wurde vollständig gelöscht.");
                Bukkit.getConsoleSender().sendMessage("[JNR] Welt " + worldName + " (Alias=" + displayName + ") erfolgreich entfernt (inkl. Dateien).");
            } else {
                DeleteFailureReason reason = attempt.getFailureReason();
                String msg = attempt.getFailureMessage().formatted();
                player.sendMessage(ChatColor.RED + "Fehler beim Löschen (" + reason + "): " + msg);
            }

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Fehler beim Löschen der Welt mit Multiverse-Core.");
            e.printStackTrace();
        }

        return true;
    }
}
