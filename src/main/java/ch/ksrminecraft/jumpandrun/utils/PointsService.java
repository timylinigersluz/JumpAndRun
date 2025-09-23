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
    private static boolean enabled = false;

    /**
     * Initialisiert die RankPointsAPI, falls in der Config aktiviert.
     */
    public static void init() {
        boolean pointsEnabled = JumpAndRun.getPlugin().getConfig().getBoolean("pointsdb.enabled", false);

        if (!pointsEnabled) {
            enabled = false;
            api = null;
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Punkte-DB ist deaktiviert → Punktevergabe abgeschaltet.");
            return;
        }

        try {
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

            enabled = true;
            Bukkit.getConsoleSender().sendMessage("[JNR] PointsService initialisiert (DB=" + db + ", excludeStaff=" + excludeStaff + ")");
        } catch (Exception e) {
            enabled = false;
            api = null;
            Bukkit.getConsoleSender().sendMessage("[JNR-ERROR] PointsService konnte nicht initialisiert werden!");
            e.printStackTrace();
        }
    }

    /**
     * Prüfen, ob Punktevergabe aktiv ist.
     */
    public static boolean isEnabled() {
        return enabled && api != null;
    }

    /**
     * Vergibt Punkte an einen Spieler für einen neuen Weltrekord.
     */
    public static void awardRecordPoints(Player player) {
        if (!isEnabled()) {
            if (JumpAndRun.getConfigManager().isDebug()) {
                Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] awardRecordPoints() aufgerufen, aber PointsService ist deaktiviert.");
            }
            return;
        }

        int points = JumpAndRun.getPlugin().getConfig().getInt("points.new-record", 50);
        UUID uuid = player.getUniqueId();

        try {
            api.addPoints(uuid, points);
            player.sendMessage("§aDu hast §e" + points + " Punkte §afür einen neuen Weltrekord erhalten!");
            Bukkit.getConsoleSender().sendMessage("[JNR] " + player.getName() + " hat " + points + " Punkte für neuen Rekord erhalten.");
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("[JNR-ERROR] Fehler beim Punkte vergeben an " + player.getName() + ": " + e.getMessage());
        }
    }
}
