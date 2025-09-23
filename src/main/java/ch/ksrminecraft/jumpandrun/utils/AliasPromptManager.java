package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.listeners.WorldSwitchListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Verwaltet Chat-Prompts für Alias-Vergabe nach /jnr ready.
 */
public class AliasPromptManager implements Listener {

    private static final Map<UUID, String> awaitingAlias = new HashMap<>();

    public static void init() {
        Bukkit.getPluginManager().registerEvents(new AliasPromptManager(), JumpAndRun.getPlugin());
    }

    /**
     * Aktiviert den Prompt für einen Spieler.
     */
    public static void awaitAliasInput(Player player, String worldName) {
        awaitingAlias.put(player.getUniqueId(), worldName);
        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "👉 Bitte gib jetzt einen Namen für dein JumpAndRun ein (3–20 Zeichen).");
        player.sendMessage(ChatColor.GRAY + "(Schreibe den Namen einfach in den Chat oder nutze §e/jnr name <alias>§7)");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!awaitingAlias.containsKey(uuid)) return;

        String alias = event.getMessage().trim();
        String worldName = awaitingAlias.remove(uuid);

        event.setCancelled(true); // Verhindert, dass die Nachricht im Chat sichtbar ist

        handleAlias(player, worldName, alias);
    }

    /**
     * Gemeinsame Logik für Chat-Prompt und /jnr name.
     */
    public static void handleAlias(Player player, String worldName, String alias) {
        // Alias prüfen
        if (alias.length() < 3 || alias.length() > 20) {
            player.sendMessage(ChatColor.RED + "Der Name muss zwischen 3 und 20 Zeichen lang sein.");
            awaitAliasInput(player, worldName); // Prompt erneut aktivieren
            return;
        }

        // In DB setzen
        WorldRepository.setAlias(worldName, alias);

        // Spieler informieren
        player.sendMessage(ChatColor.GREEN + "✔ Dein JumpAndRun heißt jetzt: §e" + alias);

        // Spieler zurückteleportieren (Origin oder Fallback)
        Location origin = WorldSwitchListener.getOrigin(player);
        if (origin != null) {
            Bukkit.getScheduler().runTask(JumpAndRun.getPlugin(), () -> {
                WorldSwitchListener.markSkipClear(player); // Inventar-Clear beim WorldSwitch überspringen
                player.teleport(origin);
                WorldSwitchListener.clearOrigin(player);

                // Inventar manuell leeren und Schild geben
                player.getInventory().clear();
                giveSign(player);

                player.sendMessage(ChatColor.GRAY + "Du wurdest zurück in die Lobby teleportiert.");
            });
        } else {
            String fallbackName = JumpAndRun.getConfigManager().getFallbackWorld();
            World fallback = Bukkit.getWorld(fallbackName);
            if (fallback != null) {
                Bukkit.getScheduler().runTask(JumpAndRun.getPlugin(), () -> {
                    WorldSwitchListener.markSkipClear(player); // Inventar-Clear beim WorldSwitch überspringen
                    player.teleport(fallback.getSpawnLocation());

                    // Inventar manuell leeren und Schild geben
                    player.getInventory().clear();
                    giveSign(player);

                    player.sendMessage(ChatColor.GRAY + "Du wurdest zur Fallback-Welt '" + fallbackName + "' teleportiert.");
                });
            } else {
                player.sendMessage(ChatColor.RED + "Fehler: Weder eine gespeicherte Origin noch die Fallback-Welt '"
                        + fallbackName + "' sind verfügbar.");
            }
        }

        // Schild-Anleitungen
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "👉 So erstellst du ein Start-Schild:");
        player.sendMessage(ChatColor.AQUA + "Zeile 1: [JNR]");
        player.sendMessage(ChatColor.AQUA + "Zeile 2: " + alias);

        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "👉 So erstellst du ein Leaderboard-Schild:");
        player.sendMessage(ChatColor.DARK_BLUE + "[JNR-LEADER]");
        player.sendMessage(ChatColor.AQUA + alias);

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler " + player.getName()
                    + " hat Welt " + worldName + " den Alias '" + alias + "' vergeben.");
        }
    }

    private static void giveSign(Player player) {
        ItemStack sign = new ItemStack(Material.OAK_SIGN, 1);
        ItemMeta meta = sign.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "JumpAndRun Schild");
            sign.setItemMeta(meta);
        }
        player.getInventory().addItem(sign);
    }

    public static boolean isAwaiting(Player player) {
        return awaitingAlias.containsKey(player.getUniqueId());
    }

    public static String consumeAwaiting(Player player) {
        return awaitingAlias.remove(player.getUniqueId());
    }
}
