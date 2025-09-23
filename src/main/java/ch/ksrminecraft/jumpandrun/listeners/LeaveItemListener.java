package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
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

public class LeaveItemListener implements Listener {

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.BARRIER) return;
        if (!event.getItem().hasItemMeta()) return;

        Player player = event.getPlayer();
        String name = event.getItem().getItemMeta().getDisplayName();

        if (ChatColor.stripColor(name).equalsIgnoreCase("» Aufgeben & Welt verlassen «")) {
            event.setCancelled(true);

            // Feedback-Nachricht
            player.sendMessage(ChatColor.RED + "✖ Du hast aufgegeben und die Welt verlassen.");

            // Inventar zurücksetzen
            player.getInventory().clear();
            player.updateInventory();

            // Effekt: Sound + Partikel
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.01);

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
        }
    }
}
