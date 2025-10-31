package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Verhindert, dass dekorative Fireworks Spielern Schaden zufügen.
 */
public class FireworkNoDamageListener implements Listener {

    @EventHandler
    public void onFireworkDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof Firework) {
            Firework firework = (Firework) damager;

            // Wenn Feuerwerk unser "dekoratives" ist → Schaden unterdrücken
            if (firework.hasMetadata("jnr_nodamage")) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Setzt Metadaten an das Feuerwerk, damit es später erkannt wird.
     */
    public static void markNoDamage(Firework firework) {
        firework.setMetadata("jnr_nodamage",
                new FixedMetadataValue(JumpAndRun.getPlugin(), true));
    }
}
