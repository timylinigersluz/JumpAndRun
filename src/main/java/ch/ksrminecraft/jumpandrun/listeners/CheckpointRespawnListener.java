package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Listener, der Spieler nach einem Tod zum letzten Checkpoint
 * (oder zum Startpunkt) zurücksetzt, ohne den Timer zurückzusetzen.
 * Nur für echte JumpAndRun-Welten (DB geprüft).
 * Reset von Health, Hunger und Sättigung für faire Bedingungen.
 */
public class CheckpointRespawnListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String worldName = player.getWorld().getName();

        // Nur reagieren, wenn es eine registrierte JnR-Welt ist
        if (!WorldRepository.exists(worldName)) {
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage(
                        "[JNR-DEBUG] Spieler " + player.getName() +
                                " ist in Welt " + worldName +
                                " gestorben (keine JnR-Welt → kein Respawn-Handling)."
                );
            }
            return;
        }

        // Ziel bestimmen: Checkpoint oder Start
        Location checkpoint = PressurePlateListener.getLastCheckpoint(player);
        Location target = (checkpoint != null)
                ? checkpoint.clone().add(0, 1, 0)
                : WorldRepository.getStartLocation(worldName) != null
                ? WorldRepository.getStartLocation(worldName).clone().add(0, 1, 0)
                : null;

        if (target == null) {
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage(
                        "[JNR-DEBUG] Keine Respawn-Location für Spieler " +
                                player.getName() + " in Welt " + worldName + " gefunden."
                );
            }
            return;
        }

        // Respawn muss 1 Tick delayed passieren
        Location finalTarget = target;
        Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), () -> {
            if (!player.isOnline()) return;
            if (!player.getWorld().getName().equals(worldName)) return;

            // Spielerzustände resetten (Health, Hunger, Saturation, FallDamage)
            PlayerUtils.resetState(player);

            // Teleport
            player.teleport(finalTarget);
            player.sendMessage("§eDu wurdest zu deinem letzten Checkpoint zurückgesetzt.");

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage(
                        "[JNR-DEBUG] Spieler " + player.getName() +
                                " wurde nach Tod zu " + formatLocation(finalTarget) +
                                " teleportiert (ActiveRun bleibt bestehen, Timer läuft weiter)."
                );
            }
        }, 1L);
    }

    private String formatLocation(Location loc) {
        return String.format("(%s | x=%.1f, y=%.1f, z=%.1f)",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ()
        );
    }
}
