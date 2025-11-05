package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Reagiert auf das spezielle Barrier-Item ("» Aufgeben & Welt verlassen «")
 * und teleportiert den Spieler zurück in seine Ursprung- oder Fallback-Welt.
 * Aktiv nur in JumpAndRun-Welten.
 */
public class LeaveItemListener implements Listener {

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getWorld() == null) return;

        // ✅ Nur in registrierten JumpAndRun-Welten aktiv
        String worldName = player.getWorld().getName();
        if (!WorldRepository.exists(worldName)) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());

        // === Nur für "Aufgeben & Welt verlassen" reagieren ===
        if (item.getType() == Material.BARRIER &&
                name.equalsIgnoreCase("» Aufgeben & Welt verlassen «")) {

            event.setCancelled(true);

            // Feedback-Nachricht
            player.sendMessage(ChatColor.RED + "✖ Du hast aufgegeben und die Welt verlassen.");

            // Inventar zurücksetzen
            player.getInventory().clear();
            player.updateInventory();

            // Effekt: Sound + Partikel
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.getWorld().spawnParticle(
                    Particle.SMOKE_LARGE, player.getLocation().add(0, 1, 0),
                    30, 0.5, 0.5, 0.5, 0.01
            );

            // Erst versuchen, zur Origin zu teleportieren
            if (WorldSwitchListener.getOrigin(player) != null) {
                player.teleport(WorldSwitchListener.getOrigin(player));
                WorldSwitchListener.clearOrigin(player);
                return;
            }

            // Fallback-Welt, falls keine Origin vorhanden
            String fallbackWorldName = JumpAndRun.getConfigManager().getFallbackWorld();
            World fallback = Bukkit.getWorld(fallbackWorldName);
            if (fallback != null) {
                player.teleport(fallback.getSpawnLocation());
            } else {
                player.sendMessage(ChatColor.RED + "Fehler: Keine gültige Fallback-Welt gefunden!");
            }

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] "
                        + player.getName() + " hat das Aufgeben-Item verwendet in Welt " + worldName);
            }
        }
    }
}
