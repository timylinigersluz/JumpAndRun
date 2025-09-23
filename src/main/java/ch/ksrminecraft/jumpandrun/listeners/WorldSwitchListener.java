package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Merkt sich, aus welcher Welt/Position ein Spieler kommt,
 * wenn er in ein JumpAndRun wechselt, um später zurückzuteleportieren.
 */
public class WorldSwitchListener implements Listener {

    private static final Map<UUID, Location> originLocations = new HashMap<>();

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String newWorld = player.getWorld().getName();

        // Nur merken, wenn neue Welt ein JumpAndRun ist
        if (WorldRepository.exists(newWorld)) {
            // Exakte Position des Spielers vor dem Wechsel speichern
            Location fromLocation = player.getLocation();

            originLocations.put(player.getUniqueId(), fromLocation.clone());

            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler " + player.getName()
                    + " wechselt von " + event.getFrom().getName()
                    + " nach " + newWorld + " → Herkunft gespeichert: "
                    + formatLocation(fromLocation));
        }
    }

    /**
     * Herkunft explizit setzen (z. B. aus Commands heraus).
     */
    public static void setOrigin(Player player, Location loc) {
        if (loc != null) {
            originLocations.put(player.getUniqueId(), loc.clone());
        }
    }

    /**
     * Holt die gespeicherte Herkunfts-Location für einen Spieler.
     */
    public static Location getOrigin(Player player) {
        return originLocations.get(player.getUniqueId());
    }

    /**
     * Entfernt gespeicherte Herkunft (z. B. nach Teleport zurück).
     */
    public static void clearOrigin(Player player) {
        originLocations.remove(player.getUniqueId());
    }

    private static String formatLocation(Location loc) {
        return String.format("(%s | x=%.1f, y=%.1f, z=%.1f)",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ());
    }
}
