package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.listeners.WorldSwitchListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

        event.setCancelled(true);
        String alias = event.getMessage().trim();
        String worldName = awaitingAlias.remove(uuid);

        // === Eingabeprüfung ===
        if (!isAliasValid(alias, player, worldName)) {
            awaitingAlias.put(uuid, worldName);
            return;
        }

        // === Name gültig → speichern
        handleAlias(player, worldName, alias);
    }

    private boolean isAliasValid(String alias, Player player, String worldName) {
        if (alias.length() < 3 || alias.length() > 20) {
            player.sendMessage(ChatColor.RED + "Der Name muss zwischen 3 und 20 Zeichen lang sein.");
            return false;
        }

        if (alias.contains(" ")) {
            player.sendMessage(ChatColor.RED + "Der Name darf keine Leerzeichen enthalten. Bitte versuche es erneut:");
            return false;
        }

        if (!alias.matches("^[A-Za-z0-9_-]+$")) {
            player.sendMessage(ChatColor.RED + "Ungültiger Name! Erlaubt sind nur Buchstaben, Zahlen, Unterstrich und Bindestrich.");
            return false;
        }

        return true;
    }

    /**
     * Gemeinsame Logik für Chat-Prompt und /jnr name.
     */
    public static void handleAlias(Player player, String worldName, String alias) {
        // Alias speichern
        WorldRepository.setAlias(worldName, alias);

        player.sendMessage(ChatColor.GREEN + "✔ Dein JumpAndRun heißt jetzt: §e" + alias);

        // Spieler zurückteleportieren
        Location origin = WorldSwitchListener.getOrigin(player);
        if (origin != null) {
            Bukkit.getScheduler().runTask(JumpAndRun.getPlugin(), () -> {
                WorldSwitchListener.markSkipClear(player);
                player.teleport(origin);
                WorldSwitchListener.clearOrigin(player);

                player.getInventory().clear();
                giveSigns(player, alias);

                player.sendMessage(ChatColor.GRAY + "Du wurdest zurück in die Lobby teleportiert.");
            });
        } else {
            String fallbackName = JumpAndRun.getConfigManager().getFallbackWorld();
            World fallback = Bukkit.getWorld(fallbackName);
            if (fallback != null) {
                Bukkit.getScheduler().runTask(JumpAndRun.getPlugin(), () -> {
                    WorldSwitchListener.markSkipClear(player);
                    player.teleport(fallback.getSpawnLocation());

                    player.getInventory().clear();
                    giveSigns(player, alias);

                    player.sendMessage(ChatColor.GRAY + "Du wurdest zur Fallback-Welt '" + fallbackName + "' teleportiert.");
                });
            } else {
                player.sendMessage(ChatColor.RED + "Fehler: Keine gültige Lobby- oder Fallback-Welt gefunden.");
            }
        }

        // Schildanleitung anzeigen
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

    /**
     * Gibt dem Spieler zwei Schilder:
     * - ein Start-Schild
     * - ein Leaderboard-Schild
     */
    private static void giveSigns(Player player, String alias) {
        ItemStack startSign = new ItemStack(Material.OAK_SIGN, 1);
        ItemMeta meta1 = startSign.getItemMeta();
        if (meta1 != null) {
            meta1.setDisplayName(ChatColor.GREEN + "Start-Schild [JNR]");
            meta1.setLore(java.util.Arrays.asList(
                    ChatColor.GRAY + "Zeile 1: [JNR]",
                    ChatColor.GRAY + "Zeile 2: " + alias
            ));
            startSign.setItemMeta(meta1);
        }

        ItemStack leaderSign = new ItemStack(Material.OAK_SIGN, 1);
        ItemMeta meta2 = leaderSign.getItemMeta();
        if (meta2 != null) {
            meta2.setDisplayName(ChatColor.GOLD + "Leaderboard-Schild [JNR-LEADER]");
            meta2.setLore(java.util.Arrays.asList(
                    ChatColor.GRAY + "Zeile 1: [JNR-LEADER]",
                    ChatColor.GRAY + "Zeile 2: " + alias
            ));
            leaderSign.setItemMeta(meta2);
        }

        player.getInventory().addItem(startSign, leaderSign);
    }

    public static boolean isAwaiting(Player player) {
        return awaitingAlias.containsKey(player.getUniqueId());
    }

    public static String consumeAwaiting(Player player) {
        return awaitingAlias.remove(player.getUniqueId());
    }
}
