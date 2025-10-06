package ch.ksrminecraft.jumpandrun.commands;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import ch.ksrminecraft.jumpandrun.db.WorldRepository;
import ch.ksrminecraft.jumpandrun.utils.ConfigManager;
import ch.ksrminecraft.jumpandrun.utils.IslandGenerator;
import ch.ksrminecraft.jumpandrun.utils.WorldUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

/**
 * Subcommand: /jnr create <länge>
 */
public class JnrCreateCommand implements CommandExecutor {

    private final JumpAndRun plugin;

    public JnrCreateCommand(JumpAndRun plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern verwendet werden.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("jumpandrun.create")) {
            player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für /jnr create.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Verwendung: /jnr create <länge>");
            return true;
        }

        // Länge parsen
        int length = 15;
        try {
            length = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.YELLOW + "Ungültige Zahl. Standardwert 15 wird verwendet.");
        }

        // Dynamischer Weltname
        String worldId = "JumpAndRun_" + player.getName() + "_" + UUID.randomUUID().toString().substring(0, 8);

        // Welt erstellen & in MV registrieren
        World world = WorldUtils.loadOrRegisterWorld(worldId, World.Environment.NORMAL, WorldType.FLAT);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Fehler beim Erstellen der Welt!");
            return true;
        }

        // Welt konfigurieren
        WorldUtils.configureWorld(world);

        // Inseln erstellen
        Location start = new Location(world, -(length / 2) - 10, 100, 0);
        IslandGenerator.createFloatingIslandStart(start, 10, JumpAndRun.height);

        Location goal = new Location(world, (length / 2) + 10, 100, 0);
        IslandGenerator.createFloatingIslandGoal(goal, 10, JumpAndRun.height);

        // Welt in DB registrieren
        WorldRepository.registerWorld(start, JumpAndRun.height, player.getUniqueId().toString());

        // === Teleport nach 1 Tick (sichert DB-Sync) ===
        Bukkit.getScheduler().runTask(plugin, () -> {
            Location tpLoc = start.clone().add(0, JumpAndRun.height + 1, 0);
            tpLoc.setDirection(goal.toVector().subtract(tpLoc.toVector()));

            player.teleport(tpLoc);
            player.setGameMode(GameMode.CREATIVE);

            // Sound + Partikel
            player.playSound(tpLoc, Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
            player.spawnParticle(Particle.CLOUD, tpLoc, 30, 0.3, 0.3, 0.3, 0.01);

            // Inventar
            giveBuildItems(player);

            // Chatnachricht
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "🎯 Du bist jetzt in deiner neuen Jump & Run Welt!");
            player.sendMessage(ChatColor.GRAY + "Baue dein Jump & Run mit den erhaltenen Blöcken.");
            player.sendMessage(ChatColor.GRAY + "Platziere die " + ChatColor.GOLD + "Checkpoint-Platte " + ChatColor.GRAY + "an wichtigen Stellen.");
            player.sendMessage(ChatColor.DARK_AQUA + "Wenn du fertig bist, nutze " + ChatColor.GREEN + "/jnr ready" + ChatColor.DARK_AQUA + " um das Jump & Run zu veröffentlichen.");
            player.sendMessage("");

            Bukkit.getConsoleSender().sendMessage("[JNR] Welt " + worldId + " wurde erstellt.");
        });

        return true;
    }

    /**
     * Gibt dem Spieler typische Jump & Run Baublöcke + Checkpoint-Platte + Barrier.
     */
    private void giveBuildItems(Player player) {
        player.getInventory().clear();

        ConfigManager cfg = JumpAndRun.getConfigManager();

        // Checkpoint-Platte aus Config
        Material plateMat = cfg.getCheckpointPlate();
        ItemStack checkpoint = new ItemStack(plateMat, 1);
        ItemMeta cpMeta = checkpoint.getItemMeta();
        cpMeta.setDisplayName(ChatColor.GOLD + "Checkpoint-Platte");
        cpMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Setze diese Platte an wichtigen Stellen,",
                ChatColor.GRAY + "um den Fortschritt der Spieler zu speichern."
        ));
        checkpoint.setItemMeta(cpMeta);

        // Typische Baublöcke
        ItemStack slime = namedItem(Material.SLIME_BLOCK, ChatColor.GREEN + "Sprungblock", "Gibt Spielern extra Sprungkraft!");
        ItemStack honey = namedItem(Material.HONEY_BLOCK, ChatColor.GOLD + "Klebriger Block", "Verlangsamt den Spieler beim Kontakt.");
        ItemStack glass = namedItem(Material.GLASS, ChatColor.AQUA + "Transparenter Block", "Ideal für präzise Sprünge.");
        ItemStack ladder = namedItem(Material.LADDER, ChatColor.YELLOW + "Leiter", "Erlaubt vertikale Passagen.");
        ItemStack ice = namedItem(Material.PACKED_ICE, ChatColor.BLUE + "Rutschiger Block", "Achtung: Rutschgefahr!");
        ItemStack snow = namedItem(Material.SNOW_BLOCK, ChatColor.WHITE + "Schnee-Block", "Für dekorative oder rutschige Stellen.");
        ItemStack lamp = namedItem(Material.REDSTONE_LAMP, ChatColor.RED + "Checkpoint-Markierung", "Zur Beleuchtung oder Dekoration.");

        // Barrier (aus WorldSwitchListener übernommen)
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName(ChatColor.RED + "» Aufgeben & Welt verlassen «");
            barrierMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Teleportiert dich zurück in die Hauptwelt.",
                    ChatColor.DARK_GRAY + "(Verliert ungespeicherte Fortschritte)"
            ));
            barrier.setItemMeta(barrierMeta);
        }

        // Items setzen
        player.getInventory().setItem(0, checkpoint);
        player.getInventory().setItem(1, slime);
        player.getInventory().setItem(2, honey);
        player.getInventory().setItem(3, glass);
        player.getInventory().setItem(4, ladder);
        player.getInventory().setItem(5, ice);
        player.getInventory().setItem(6, snow);
        player.getInventory().setItem(7, lamp);
        player.getInventory().setItem(8, barrier);
        player.updateInventory();

        // Feedback-Sound & Partikel
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
        player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation(), 20, 0.5, 0.5, 0.5, 0.05);
    }

    /** Hilfsmethode: erstellt Item mit Namen & Lore */
    private ItemStack namedItem(Material mat, String name, String loreLine) {
        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(ChatColor.GRAY + loreLine));
            item.setItemMeta(meta);
        }
        return item;
    }
}
