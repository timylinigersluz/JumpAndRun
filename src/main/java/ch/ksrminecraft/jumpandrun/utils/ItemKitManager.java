package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Verwaltet die Item-Kits für JumpAndRun-Spieler.
 *
 * Enthält das Standard-Bauset (Checkpoints, Blöcke, Barrier etc.),
 * das bei /jnr create und /jnr continue vergeben wird.
 */
public class ItemKitManager {

    /**
     * Gibt dem Spieler das Standard-Bauset für JumpAndRun.
     * Enthält Checkpoint-Platte, verschiedene Blöcke und Barrier-Item.
     */
    public static void giveBuildKit(Player player) {
        player.getInventory().clear();

        ConfigManager cfg = JumpAndRun.getConfigManager();

        // === Checkpoint-Platte ===
        Material plateMat = cfg.getCheckpointPlate();
        ItemStack checkpoint = new ItemStack(plateMat, 1);
        ItemMeta cpMeta = checkpoint.getItemMeta();
        if (cpMeta != null) {
            cpMeta.setDisplayName(ChatColor.GOLD + "Checkpoint-Platte");
            cpMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Setze diese Platte an wichtigen Stellen,",
                    ChatColor.GRAY + "um den Fortschritt der Spieler zu speichern."
            ));
            checkpoint.setItemMeta(cpMeta);
        }

        // === Typische Jump & Run Baublöcke ===
        ItemStack slime = namedItem(Material.SLIME_BLOCK, ChatColor.GREEN + "Sprungblock", "Gibt Spielern extra Sprungkraft!");
        ItemStack honey = namedItem(Material.HONEY_BLOCK, ChatColor.GOLD + "Klebriger Block", "Verlangsamt den Spieler beim Kontakt.");
        ItemStack glass = namedItem(Material.GLASS, ChatColor.AQUA + "Transparenter Block", "Ideal für präzise Sprünge.");
        ItemStack ladder = namedItem(Material.LADDER, ChatColor.YELLOW + "Leiter", "Erlaubt vertikale Passagen.");
        ItemStack ice = namedItem(Material.PACKED_ICE, ChatColor.BLUE + "Rutschiger Block", "Achtung: Rutschgefahr!");
        ItemStack snow = namedItem(Material.SNOW_BLOCK, ChatColor.WHITE + "Schnee-Block", "Für dekorative oder rutschige Stellen.");
        ItemStack lamp = namedItem(Material.REDSTONE_LAMP, ChatColor.RED + "Checkpoint-Markierung", "Zur Beleuchtung oder Dekoration.");

        // === Barrier (Aufgeben & Welt verlassen) ===
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName(ChatColor.RED + "» Aufgeben & Welt verlassen «");
            barrierMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Teleportiert dich zurück in die Hauptwelt.",
                    ChatColor.DARK_GRAY + "(Verliert ungespeicherte Fortschritte)"
            ));
            barrier.setItemMeta(barrierMeta);
        }

        // === Items setzen ===
        player.getInventory().setItem(0, checkpoint);
        player.getInventory().setItem(1, slime);
        player.getInventory().setItem(2, honey);
        player.getInventory().setItem(3, glass);
        player.getInventory().setItem(4, ladder);
        player.getInventory().setItem(5, ice);
        player.getInventory().setItem(6, snow);
        player.getInventory().setItem(7, lamp);
        player.getInventory().setItem(8, barrier);
        player.updateInventory();

        // === Feedback ===
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
        player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.05);
    }

    /**
     * Erstellt ein Item mit benutzerdefiniertem Namen und Lore.
     */
    private static ItemStack namedItem(Material mat, String name, String loreLine) {
        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(ChatColor.GRAY + loreLine));
            item.setItemMeta(meta);
        }
        return item;
    }
}
