package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.utils.ConfigManager;
import ch.ksrminecraft.jumpandrun.utils.SignUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class JnrReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("jumpandrun.reload")) {
            sender.sendMessage(ChatColor.RED + "Dazu hast du keine Berechtigung.");
            return true;
        }

        long start = System.currentTimeMillis();

        try {
            // --- Config neu laden ---
            JumpAndRun.getPlugin().reloadConfig();
            JumpAndRun.setConfigManager(new ConfigManager(JumpAndRun.getPlugin()));

            sender.sendMessage(ChatColor.GREEN + "✔ JumpAndRun-Konfiguration neu geladen.");

            // 🔹 Schritt 1: asynchron DB laden
            Bukkit.getScheduler().runTaskAsynchronously(JumpAndRun.getPlugin(), () -> {
                try {
                    SignUpdater.loadAllFromDatabase(); // Datenbankzugriff → erlaubt asynchron

                    // 🔹 Schritt 2: synchron auf Bukkit-Thread die Weltoperationen ausführen
                    Bukkit.getScheduler().runTask(JumpAndRun.getPlugin(), () -> {
                        for (World world : Bukkit.getWorlds()) {
                            if (world.getName().equalsIgnoreCase("world")
                                    || ch.ksrminecraft.jumpandrun.db.WorldRepository.exists(world.getName())) {
                                SignUpdater.syncWorldSigns(world);
                            }
                        }

                        long took = System.currentTimeMillis() - start;
                        sender.sendMessage(ChatColor.YELLOW + "🔁 Leader-Schilder neu synchronisiert (" + took + " ms).");
                    });

                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "⚠ Fehler beim Aktualisieren der Leader-Schilder: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "⚠ Fehler beim Neuladen der Konfiguration: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}
