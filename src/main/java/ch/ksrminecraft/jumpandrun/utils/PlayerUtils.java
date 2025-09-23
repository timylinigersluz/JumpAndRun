package ch.ksrminecraft.jumpandrun.utils;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 * Utility-Methoden für Spieleraktionen.
 */
public class PlayerUtils {

    /**
     * Setzt den Zustand des Spielers zurück:
     * - volle Herzen
     * - volle Sättigung & Hunger
     * - kein Fallschaden
     * - alle aktiven Effekte entfernt
     *
     * @param player Spieler, dessen Zustand zurückgesetzt wird
     */
    public static void resetState(Player player) {
        if (player == null) return;

        player.setFallDistance(0);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);

        // Alle aktiven Tränkeffekte entfernen
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
}
