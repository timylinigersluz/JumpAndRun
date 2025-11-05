package ch.ksrminecraft.jumpandrun.listeners;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Verhindert, dass Spieler direkt in einer JumpAndRun-Welt spawnen.
 * Falls sich ein Spieler beim Logout in einer JNR-Welt befand,
 * wird er beim Join automatisch in die Lobby (Fallback-Welt) teleportiert.
 */
public class JoinRedirectListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getWorld() == null) return;

        String currentWorld = player.getWorld().getName();

        // kleine Verzögerung, damit Spigot den Spawnpunkt korrekt setzt
        Bukkit.getScheduler().runTaskLater(JumpAndRun.getPlugin(), () -> {

            // ✅ Nur reagieren, wenn der Spieler sich in einer registrierten JumpAndRun-Welt befindet
            if (!WorldRepository.exists(currentWorld)) return;

            String fallbackWorldName = JumpAndRun.getConfigManager().getFallbackWorld();
            World fallbackWorld = Bukkit.getWorld(fallbackWorldName);

            if (fallbackWorld == null) {
                JumpAndRun.getPlugin().getLogger().severe("[JNR] Fallback-Welt '" + fallbackWorldName + "' existiert nicht!");
                player.sendMessage(ChatColor.RED + "Fehler: Fallback-Welt '" + fallbackWorldName + "' nicht gefunden!");
                return;
            }

            // Teleport in die Lobby
            player.teleport(fallbackWorld.getSpawnLocation());
            player.setFallDistance(0);
            player.setNoDamageTicks(40);

            player.sendMessage(ChatColor.GRAY + "Du kannst JumpAndRuns nur über die Schilder oder /mvtp betreten.");
            JumpAndRun.getPlugin().getLogger().info("[JNR] Spieler " + player.getName() +
                    " wurde beim Login aus JnR-Welt '" + currentWorld + "' in die Lobby teleportiert.");

            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] " + player.getName() +
                        " wurde aus Welt '" + currentWorld + "' beim Join in '" + fallbackWorldName + "' verschoben.");
            }

        }, 5L); // leicht verzögert (0.25s)
    }
}
