package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.listeners.WorldSwitchListener;
import ch.ksrminecraft.jumpandrun.utils.TestRunManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Subcommand: /jnr abort <keepworld|deleteworld>
 * --------------------------------------------------
 * Bricht einen Draft/Testlauf ab und bringt den Spieler
 * sicher in die Lobby zurück.
 *
 * Regeln:
 *  - darf NUR in einer Draft-Welt (unveröffentlicht) ausgeführt werden
 *  - /jnr abort keepworld   → Welt bleibt als Draft erhalten
 *  - /jnr abort deleteworld → Welt wird gelöscht
 */
public class JnrAbortCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Nur Spieler können diesen Befehl nutzen.");
            return true;
        }

        // --- Permission prüfen ---
        if (!player.hasPermission("jumpandrun.abort")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, JumpAndRun-Abbrüche durchzuführen.");
            return true;
        }

        // --- Syntaxhilfe ---
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "❓ Möchtest du deine Welt behalten oder löschen?");
            player.sendMessage(ChatColor.GRAY + " → /jnr abort keepworld   " + ChatColor.DARK_GRAY + "(Welt als Draft behalten)");
            player.sendMessage(ChatColor.GRAY + " → /jnr abort deleteworld " + ChatColor.DARK_GRAY + "(Welt komplett löschen)");
            return true;
        }

        String action = args[1].toLowerCase();
        World currentWorld = player.getWorld();
        String worldName = currentWorld.getName();

        // ------------------------------------------------------------
        // Nur Draft-Welten zulassen (existiert + nicht veröffentlicht)
        // ------------------------------------------------------------
        boolean exists = WorldRepository.exists(worldName);
        boolean published = WorldRepository.isPublished(worldName);
        boolean isDraft = exists && !published;

        if (!isDraft) {
            player.sendMessage(ChatColor.RED + "❌ Du kannst diesen Befehl nur in einer Entwurfs-Welt (Draft) ausführen!");
            if (JumpAndRun.getConfigManager().isDebug()) {
                JumpAndRun.getPlugin().getLogger()
                        .info("[JNR-DEBUG] " + player.getName() + " versuchte /jnr abort " + action + " in keiner Draft-Welt: " + worldName);
            }
            return true;
        }

        // ------------------------------------------------------------
        // Draft-Welt → Abbruchlogik
        // ------------------------------------------------------------
        if (TestRunManager.isTesting(player)) {
            TestRunManager.abortTest(player);
        }

        player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
        player.setFallDistance(0);
        player.setNoDamageTicks(40);
        player.getInventory().clear();
        player.updateInventory();

        switch (action) {
            // --------------------------------------------------------
            case "deleteworld" -> {
                if (!player.hasPermission("jumpandrun.abort.deleteworld")) {
                    player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, Welten zu löschen.");
                    return true;
                }

                // Multiverse entfernen + entladen
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
                teleportToLobby(player);
            }

            // --------------------------------------------------------
            case "keepworld" -> {
                if (!player.hasPermission("jumpandrun.abort.keepworld")) {
                    player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung, Welten als Draft zu behalten.");
                    return true;
                }

                player.sendMessage(ChatColor.GREEN + "✔ Dein Testlauf wurde abgebrochen. Die Welt §e" + worldName + ChatColor.GREEN + " bleibt als Draft bestehen.");
                Bukkit.getConsoleSender().sendMessage("[JNR] Spieler " + player.getName() + " hat seinen Test abgebrochen (Welt behalten: " + worldName + ").");
                teleportToLobby(player);
            }

            // --------------------------------------------------------
            default -> player.sendMessage(ChatColor.RED + "Ungültige Option. Nutze /jnr abort <keepworld|deleteworld>");
        }

        return true;
    }

    /**
     * Teleportiert den Spieler sicher zurück in die Lobby / Fallback-Welt.
     */
    private void teleportToLobby(Player player) {
        Bukkit.getScheduler().runTask(JumpAndRun.getPlugin(), () -> {
            Location origin = WorldSwitchListener.getOrigin(player);
            if (origin != null) {
                WorldSwitchListener.markSkipClear(player);
                player.teleport(origin);
                WorldSwitchListener.clearOrigin(player);
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(ChatColor.AQUA + "Du wurdest zurück in die Lobby teleportiert.");
                return;
            }

            // Fallback aus Config laden
            String fallbackWorldName = JumpAndRun.getConfigManager().getFallbackWorld();
            World fallback = Bukkit.getWorld(fallbackWorldName);
            if (fallback != null) {
                WorldSwitchListener.markSkipClear(player);
                player.teleport(fallback.getSpawnLocation());
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(ChatColor.AQUA + "Du wurdest zurück zur Lobby §e" + fallbackWorldName + " §bteleportiert.");
            } else {
                player.sendMessage(ChatColor.RED + "Fehler: Keine Lobby- oder Fallback-Welt gefunden!");
            }
        });
    }
}
