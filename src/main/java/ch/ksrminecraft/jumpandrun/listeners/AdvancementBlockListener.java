package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

/**
 * Blockiert Advancements (Erfolge) in JumpAndRun-Welten.
 * -----------------------------------------------------
 * - Gilt für Draft- und veröffentlichte JumpAndRun-Welten.
 * - Spieler erhalten keine Nachricht.
 * - Im Debugmodus wird im Log angezeigt, welches Advancement geblockt wurde.
 */
public class AdvancementBlockListener implements Listener {

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();

        // ✅ Schutzmechanismus: Nur in JumpAndRun-Welten ausführen
        if (player == null || player.getWorld() == null) return;
        String worldName = player.getWorld().getName();
        if (!WorldRepository.exists(worldName)) return;

        Advancement advancement = event.getAdvancement();

        // Chatnachricht unterdrücken
        event.message(null);

        // Alle verliehenen Kriterien des Erfolgs wieder entfernen
        var progress = player.getAdvancementProgress(advancement);
        for (String criterion : progress.getAwardedCriteria()) {
            progress.revokeCriteria(criterion);
        }

        // Debug-Logging
        if (JumpAndRun.getConfigManager().isDebug()) {
            String advName = advancement.getKey().getKey();
            JumpAndRun.getPlugin().getLogger().info(
                    "[JNR-DEBUG] Advancement '" + advName + "' wurde für Spieler " + player.getName() +
                            " in Welt '" + worldName + "' blockiert."
            );
        }
    }
}
