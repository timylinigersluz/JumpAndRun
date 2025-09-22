package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.utils.TeleportUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Listener, der nach einem Spielertod den Respawn-Punkt auf die Jump-and-Run-Insel
 * setzt, sofern sich der Spieler in der entsprechenden Welt befindet.
 */
public class Spawnpoint implements Listener {

    /** Referenz auf die überwachte Welt (Jump-and-Run). */
    private World world;

    /**
     * Registriert die Welt, für die dieser Listener zuständig ist.
     *
     * @param worldInput Weltobjekt des Jump-and-Run Bereichs.
     */
    public void registerWorld(World worldInput) {
        this.world = worldInput;

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spawnpoint-Listener für Welt " + worldInput.getName() + " registriert.");
        }
    }

    /**
     * Reagiert auf Spielertode und teleportiert sie zum Start der Insel.
     *
     * @param event PlayerDeathEvent, wenn ein Spieler stirbt.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (world != null && player.getWorld().equals(world)) {
            TeleportUtils.teleportToIsland(player, world.getName());

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage(
                        "[JNR-DEBUG] Spieler " + player.getName() +
                                " ist in Welt " + world.getName() +
                                " gestorben und wurde auf die Insel teleportiert."
                );
            }
        }
    }
}
