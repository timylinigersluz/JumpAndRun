package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.RankPointsAPI.PointsAPI;
import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Service-Klasse für Punktevergabe (Integration mit RankPointsAPI).
 */
public class PointsService {

    private static PointsAPI api;

    /**
     * Initialisiert die RankPointsAPI mit den Werten aus config.yml (pointsdb.*).
     */
    public static void init() {
        String host = JumpAndRun.getPlugin().getConfig().getString("pointsdb.host");
        int port = JumpAndRun.getPlugin().getConfig().getInt("pointsdb.port");
        String db = JumpAndRun.getPlugin().getConfig().getString("pointsdb.database");
        String user = JumpAndRun.getPlugin().getConfig().getString("pointsdb.user");
        String pass = JumpAndRun.getPlugin().getConfig().getString("pointsdb.password");

        boolean debug = JumpAndRun.getConfigManager().isDebug();
        boolean excludeStaff = JumpAndRun.getPlugin().getConfig().getBoolean("pointsdb.excludeStaff", true);

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + db;

        api = new PointsAPI(
                jdbcUrl,
                user,
                pass,
                JumpAndRun.getPlugin().getLogger(),
                debug,
                excludeStaff
        );

        Bukkit.getConsoleSender().sendMessage("[JNR] PointsService initialisiert (DB=" + db + ", excludeStaff=" + excludeStaff + ")");
    }

    /**
     * Vergibt Punkte an einen Spieler für einen neuen Weltrekord.
     */
    public static void awardRecordPoints(Player player) {
        if (api == null) {
            Bukkit.getConsoleSender().sendMessage("[JNR] Fehler: PointsService nicht initialisiert!");
            return;
        }

        int points = JumpAndRun.getPlugin().getConfig().getInt("points.new-record", 50);
        UUID uuid = player.getUniqueId();

        api.addPoints(uuid, points);

        player.sendMessage("§aDu hast §e" + points + " Punkte §afür einen neuen Weltrekord erhalten!");
        Bukkit.getConsoleSender().sendMessage("[JNR] " + player.getName() + " hat " + points + " Punkte für neuen Rekord erhalten.");
    }
}
