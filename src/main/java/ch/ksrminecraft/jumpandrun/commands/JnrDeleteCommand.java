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
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
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

        // Falls Welt gar nicht existiert → Fehlermeldung
        if (!WorldRepository.exists(worldName)) {
            player.sendMessage(ChatColor.RED + "Die JumpAndRun-Welt §e" + aliasOrWorld + ChatColor.RED + " ist nicht in der Datenbank registriert.");
            return true;
        }

        // Für Anzeige den Alias nehmen, falls vorhanden
        String alias = WorldRepository.getAlias(worldName);
        String displayName = (alias != null && !alias.isEmpty()) ? alias : worldName;

        // Spieler sicher in die Lobby teleportieren (vor dem Löschen)
        World fallback = Bukkit.getWorld("world");
        if (fallback != null) {
            player.teleport(fallback.getSpawnLocation());
            player.setFallDistance(0);
            player.setNoDamageTicks(40);
        }

        try {
            // === Multiverse-API ===
            MultiverseCoreApi mvApi = MultiverseCoreApi.get();
            WorldManager wm = mvApi.getWorldManager();

            var optMvWorld = wm.getWorld(worldName);
            if (optMvWorld.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Die Welt §e" + displayName + ChatColor.RED + " ist Multiverse nicht bekannt – nur DB-Eintrag wird gelöscht.");

                // 🔹 Trotzdem aus Datenbank entfernen
                WorldRepository.removeWorld(worldName);
                Bukkit.getConsoleSender().sendMessage("[JNR] Welt " + worldName + " (Alias=" + displayName + ") aus DB entfernt (nicht mehr in Multiverse).");
                return true;
            }

            MultiverseWorld mvWorld = optMvWorld.get();

            DeleteWorldOptions options = DeleteWorldOptions.world(mvWorld)
                    .keepFiles(Collections.emptyList());

            Attempt<String, DeleteFailureReason> attempt = wm.deleteWorld(options);

            if (attempt.isSuccess()) {
                // 🔹 Erfolgreich gelöscht → DB aktualisieren
                WorldRepository.removeWorld(worldName);

                player.sendMessage(ChatColor.GREEN + "JumpAndRun §e" + displayName + ChatColor.GREEN + " wurde vollständig gelöscht.");
                Bukkit.getConsoleSender().sendMessage("[JNR] Welt " + worldName + " (Alias=" + displayName + ") erfolgreich gelöscht (Multiverse + DB).");
            } else {
                DeleteFailureReason reason = attempt.getFailureReason();
                String msg = attempt.getFailureMessage().formatted();

                // 🔸 Trotzdem DB-Sync durchführen, wenn die Welt nicht mehr existiert
                if (Bukkit.getWorld(worldName) == null) {
                    WorldRepository.removeWorld(worldName);
                    Bukkit.getConsoleSender().sendMessage("[JNR] Multiverse-Löschung teilweise fehlgeschlagen (" + reason + "), aber Welt entladen → DB-Eintrag entfernt.");
                }

                player.sendMessage(ChatColor.RED + "Fehler beim Löschen (" + reason + "): " + msg);
            }

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Fehler beim Löschen der Welt über Multiverse-Core.");
            e.printStackTrace();

            // 🔸 Fallback: Entferne aus DB, falls Multiverse-Fehler
            if (WorldRepository.exists(worldName)) {
                WorldRepository.removeWorld(worldName);
                Bukkit.getConsoleSender().sendMessage("[JNR] Fallback: DB-Eintrag für " + worldName + " entfernt (MV-Fehler).");
            }
        }

        return true;
    }
}
