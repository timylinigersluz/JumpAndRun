package ch.ksrminecraft.jumpandrun.models;

import ch.ksrminecraft.jumpandrun.JumpAndRun;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;

/**
 * Datenmodell für eine konfigurierbare Jump-and-Run-Welt.
 * Kann für dynamische Welt-Erstellung und spätere Erweiterungen genutzt werden.
 */
public class JnRWorld {

    /** Eindeutiger Name der Welt. */
    private String worldName;

    /** Umgebungstyp, z.B. NORMAL oder NETHER. */
    private World.Environment envType;

    /** Zufallsseed der Welt. */
    private long seed;

    /** WorldType, z.B. FLAT. */
    private WorldType worldType;

    /** Flag, ob Strukturen generiert werden sollen. */
    private boolean structures;

    /** Optionaler Generatorname für eigene Terrain-Generatoren. */
    private String customGen;

    /** Länge des geplanten Spielfeldes. */
    private int length;

    /** Breite des geplanten Spielfeldes. */
    private int width;

    // ===========================
    // Konstruktoren
    // ===========================

    public JnRWorld(String worldName, World.Environment envType, long seed, WorldType worldType,
                    boolean structures, String customGen, int length, int width) {
        this.worldName = worldName;
        this.envType = envType;
        this.seed = seed;
        this.worldType = worldType;
        this.structures = structures;
        this.customGen = customGen;
        this.length = length;
        this.width = width;

        if (JumpAndRun.getConfigManager().isDebug()) {
            Bukkit.getConsoleSender().sendMessage("[JNR-DEBUG] Neue JnRWorld erstellt: " + this);
        }
    }

    public JnRWorld(String worldName) {
        this(worldName, World.Environment.NORMAL, 0L, WorldType.NORMAL, false, null, 15, 15);
    }

    // ===========================
    // Getter & Setter
    // ===========================

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public World.Environment getEnvType() {
        return envType;
    }

    public void setEnvType(World.Environment envType) {
        this.envType = envType;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public WorldType getWorldType() {
        return worldType;
    }

    public void setWorldType(WorldType worldType) {
        this.worldType = worldType;
    }

    public boolean isStructures() {
        return structures;
    }

    public void setStructures(boolean structures) {
        this.structures = structures;
    }

    public String getCustomGen() {
        return customGen;
    }

    public void setCustomGen(String customGen) {
        this.customGen = customGen;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    // ===========================
    // Hilfsmethoden
    // ===========================

    @Override
    public String toString() {
        return "JnRWorld{" +
                "name='" + worldName + '\'' +
                ", envType=" + envType +
                ", seed=" + seed +
                ", worldType=" + worldType +
                ", structures=" + structures +
                ", customGen='" + customGen + '\'' +
                ", length=" + length +
                ", width=" + width +
                '}';
    }
}
