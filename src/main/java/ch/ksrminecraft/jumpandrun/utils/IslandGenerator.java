package ch.ksrminecraft.jumpandrun.utils;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

/**
 * Utility-Klasse zur prozeduralen Erzeugung von Start- und Zielinseln
 * sowie zur Platzierung der zugehörigen Druckplatten für das Jump-and-Run.
 */
public class IslandGenerator {

    /** Auswahl an Blöcken für die obere Erd-/Gras-Schicht. */
    private static final Material[] TOP_LAYER_BLOCKS = {
            Material.DIRT, Material.GRASS_BLOCK, Material.MOSS_BLOCK
    };

    /** Steinschicht, die die Insel nach unten hin ausfüllt. */
    private static final Material[] STONE_BLOCKS = {
            Material.STONE, Material.COBBLESTONE, Material.ANDESITE,
            Material.DIORITE, Material.GRANITE
    };

    /** Erzblöcke, die an der Unterkante für optische Highlights sorgen. */
    private static final Material[] ORE_BLOCKS = {
            Material.COAL_ORE, Material.IRON_ORE, Material.REDSTONE_ORE,
            Material.LAPIS_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE
    };

    /** Abkürzung für den Debug-Check über den ConfigManager. */
    private static boolean isDebug() {
        return JumpAndRun.getConfigManager().isDebug();
    }

    /**
     * Baut die Startinsel inklusive Ranken und Druckplatten.
     *
     * @param centerLocation Mittelpunkt der Insel.
     * @param radius         Radius der runden Plattform.
     * @param height         Vertikale Ausdehnung der Insel.
     */
    public static void createFloatingIslandStart(Location centerLocation, int radius, int height) {
        World world = centerLocation.getWorld();
        Random random = new Random();

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Erzeuge Startinsel bei " + formatLocation(centerLocation));
        }

        // Erzeuge die obere Plattform
        for (int x = -radius; x <= radius - 2; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    Block block = world.getBlockAt(
                            centerLocation.getBlockX() + x,
                            centerLocation.getBlockY() + height,
                            centerLocation.getBlockZ() + z
                    );
                    block.setType(TOP_LAYER_BLOCKS[random.nextInt(TOP_LAYER_BLOCKS.length)]);
                }
            }
        }

        // Erzeuge die Schichtstruktur unterhalb
        for (int y = height - 1; y >= 1; y--) {
            double semiCircleRadius = Math.sqrt(radius * radius - Math.pow((height - y), 2));

            for (int x = -radius; x <= radius - 2; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= semiCircleRadius * semiCircleRadius) {
                        Block block = world.getBlockAt(
                                centerLocation.getBlockX() + x,
                                centerLocation.getBlockY() + y,
                                centerLocation.getBlockZ() + z
                        );

                        Material material = (y == 1)
                                ? ORE_BLOCKS[random.nextInt(ORE_BLOCKS.length)]
                                : STONE_BLOCKS[random.nextInt(STONE_BLOCKS.length)];

                        block.setType(material);
                    }
                }
            }
        }

        // Ranken + Start-Druckplatte setzen
        int vineStartY = centerLocation.getBlockY() + height * 2 / 3;
        int vineEndY = centerLocation.getBlockY() + height;
        addVinesOnTop1(world, centerLocation, radius, vineStartY, vineEndY, random);
        addStartPressurePlate(centerLocation, height);

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Startinsel fertig erstellt.");
        }
    }

    /**
     * Ergänzt zufällig platzierte Ranken entlang der äusseren Kanten der Startinsel.
     */
    private static void addVinesOnTop1(World world, Location centerLocation, int radius, int startY, int endY, Random random) {
        for (int y = startY; y <= endY; y++) {
            for (int x = -radius; x <= radius - 2; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius) {
                        Block block = world.getBlockAt(
                                centerLocation.getBlockX() + x,
                                y,
                                centerLocation.getBlockZ() + z
                        );
                        if (block.getType() == Material.AIR && random.nextInt(4) == 0) {
                            block.setType(Material.VINE);
                        }
                    }
                }
            }
        }
    }

    /**
     * Platziert die Startdruckplatte 10 Blöcke östlich der Startinsel.
     */
    private static void addStartPressurePlate(Location centerLocation, int height) {
        Location startPlateLoc = centerLocation.clone().add(5, height + 1, 0);
        Block startBlock = startPlateLoc.getWorld().getBlockAt(startPlateLoc);
        startBlock.setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Startdruckplatte bei " + formatLocation(startPlateLoc));
        }
    }

    /**
     * Generiert die Zielinsel und platziert die End-Druckplatte darauf.
     */
    public static void createFloatingIslandGoal(Location centerLocation, int radius, int height) {
        World world = centerLocation.getWorld();
        Random random = new Random();

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Erzeuge Zielinsel bei " + formatLocation(centerLocation));
        }

        // Oberfläche
        for (int x = 2 - radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    Block block = world.getBlockAt(
                            centerLocation.getBlockX() + x,
                            centerLocation.getBlockY() + height,
                            centerLocation.getBlockZ() + z
                    );
                    block.setType(TOP_LAYER_BLOCKS[random.nextInt(TOP_LAYER_BLOCKS.length)]);
                }
            }
        }

        // Unterbau
        for (int y = height - 1; y >= 1; y--) {
            double semiCircleRadius = Math.sqrt(radius * radius - Math.pow((height - y), 2));

            for (int x = 2 - radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= semiCircleRadius * semiCircleRadius) {
                        Block block = world.getBlockAt(
                                centerLocation.getBlockX() + x,
                                centerLocation.getBlockY() + y,
                                centerLocation.getBlockZ() + z
                        );

                        Material material = (y == 1)
                                ? ORE_BLOCKS[random.nextInt(ORE_BLOCKS.length)]
                                : STONE_BLOCKS[random.nextInt(STONE_BLOCKS.length)];

                        block.setType(material);
                    }
                }
            }
        }

        // Ranken
        int vineStartY = centerLocation.getBlockY() + height * 2 / 3;
        int vineEndY = centerLocation.getBlockY() + height;
        addVinesOnTop2(world, centerLocation, radius, vineStartY, vineEndY, random);

        // Enddruckplatte auf der Zielinsel
        addEndPressurePlate(centerLocation, height);

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Zielinsel fertig erstellt.");
        }
    }

    /**
     * Ergänzt Ranken entlang der Zielinsel.
     */
    private static void addVinesOnTop2(World world, Location centerLocation, int radius, int startY, int endY, Random random) {
        for (int y = startY; y <= endY; y++) {
            for (int x = 2 - radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius) {
                        Block block = world.getBlockAt(
                                centerLocation.getBlockX() + x,
                                y,
                                centerLocation.getBlockZ() + z
                        );
                        if (block.getType() == Material.AIR && random.nextInt(4) == 0) {
                            block.setType(Material.VINE);
                        }
                    }
                }
            }
        }
    }

    /**
     * Platziert die End-Druckplatte zentral auf der Zielinsel.
     */
    private static void addEndPressurePlate(Location centerLocation, int height) {
        Location endPlateLoc = centerLocation.clone().add(0, height + 1, 0);
        Block endBlock = endPlateLoc.getWorld().getBlockAt(endPlateLoc);
        endBlock.setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);

        if (isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Zieldruckplatte bei " + formatLocation(endPlateLoc));
        }
    }

    /**
     * Hilfsmethode zur schönen Formatierung einer Location für Debug-Ausgaben.
     */
    private static String formatLocation(Location loc) {
        return String.format("(%s | x=%.1f, y=%.1f, z=%.1f)",
                loc.getWorld() != null ? loc.getWorld().getName() : "null",
                loc.getX(), loc.getY(), loc.getZ()
        );
    }
}
