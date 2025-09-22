package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwalter für Welt-Locks: Stellt sicher, dass nur ein Spieler
 * gleichzeitig in einer JumpAndRun-Welt aktiv ist.
 */
public class WorldLockManager {

    // Map<WorldName, PlayerUUID>
    private static final Map<String, UUID> lockedWorlds = new ConcurrentHashMap<>();

    /**
     * Versucht, einen Spieler in eine Welt eintreten zu lassen.
     * @return true, wenn erfolgreich; false, wenn bereits besetzt.
     */
    public static boolean tryEnter(String worldName, Player player) {
        UUID occ = lockedWorlds.get(worldName);
        if (occ == null) {
            lockedWorlds.put(worldName, player.getUniqueId());
            return true;
        }
        return occ.equals(player.getUniqueId()); // Spieler selbst darf weiter rein
    }

    /**
     * Gibt eine Welt frei, wenn der Spieler der aktuelle Occupant ist.
     */
    public static void leave(String worldName, Player player) {
        UUID occ = lockedWorlds.get(worldName);
        if (occ != null && occ.equals(player.getUniqueId())) {
            lockedWorlds.remove(worldName);
        }
    }

    /**
     * Gibt alle Welten frei, die von einem Spieler belegt wurden.
     */
    public static void leave(Player player) {
        lockedWorlds.entrySet().removeIf(entry -> entry.getValue().equals(player.getUniqueId()));
    }

    /**
     * Holt den aktuellen Spieler, der die Welt blockiert.
     */
    public static Player getOccupant(String worldName) {
        UUID occ = lockedWorlds.get(worldName);
        return (occ != null) ? Bukkit.getPlayer(occ) : null;
    }

    /**
     * Prüft, ob eine Welt aktuell gesperrt ist.
     */
    public static boolean isLocked(String worldName) {
        return lockedWorlds.containsKey(worldName);
    }
}
