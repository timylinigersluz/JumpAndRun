package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String worldName = player.getWorld().getName();

        // Nur reagieren, wenn Spieler in einer JnR-Welt ist
        if (!WorldRepository.exists(worldName)) return;

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Spieler " + player.getName() +
                    " ist in Welt " + worldName + " gestorben → Run wird abgebrochen, Spieler wird ausgeloggt.");
        }

        // Nachricht an Spieler
        player.sendMessage(ChatColor.RED + "Du bist gestorben. Dein Run wurde abgebrochen. "
                + "Starte einen neuen Versuch über das Start-Schild.");
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();

        // Nur reagieren, wenn Spieler in einer JnR-Welt war
        if (!WorldRepository.exists(worldName)) return;

        // Spieler nach Respawn in die Lobby / Origin teleportieren
        String fallbackWorldName = JumpAndRun.getConfigManager().getFallbackWorld();
        World fallbackWorld = Bukkit.getWorld(fallbackWorldName);
        if (fallbackWorld != null) {
            event.setRespawnLocation(fallbackWorld.getSpawnLocation());
            player.sendMessage(ChatColor.AQUA + "Du wurdest nach deinem Tod in die Lobby zurückgebracht.");
        } else {
            player.sendMessage(ChatColor.RED + "Fehler: Keine Fallback-Welt gefunden!");
        }
    }
}
