package dev.ferriarnus.monocle;

import dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.terrain.IrisModelVertexFormats;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLPaths;
import org.embeddedt.embeddium.impl.Embeddium;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(value = Monocle.MODID, dist = Dist.CLIENT)
public class Monocle {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "monocle";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final Properties CONFIG = new Properties();
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("monocle.properties");
    private static boolean ALLOW_FULL_FORMAT = false;
    private static boolean MOVE_LAST = false;
    private static boolean END_BATCH = false;
    public static final String FALSE = "false";
    private static final String[] MODS = new String[]{
            "justdirethings",
            "supplementaries",
            "caxton",
            "twilightforest",
            "enchanted",
            "the_bumblezone",
            "forbidden_arcanus",
            "xycraft",
            "immersiveengineering",
            "computercraft",
            "creeperoverhaul",
            "arsnouveau",
            "mekanism",
            "eternal_starlight",
            "refurbished_furniture",
            "enderstorage"
    };

    public Monocle(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Loaded Monocle v{}", modContainer.getModInfo().getVersion().toString());
    }

    public static ChunkVertexType getVertexFormat() {
        if (!ALLOW_FULL_FORMAT) {
            return IrisModelVertexFormats.MODEL_VERTEX_XHFP;
        }
        return Embeddium.options().performance.useCompactVertexFormat ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : IrisModelVertexFormats.MODEL_VERTEX_XSFP;
    }

    public static boolean allowsFullFormat() {
        return ALLOW_FULL_FORMAT;
    }

    public static boolean moveRenderLastStage() {
        return MOVE_LAST;
    }

    public static boolean endBatch() {
        return END_BATCH;
    }

    public static Properties loadConfig() throws IOException {
        if (!Files.exists(Monocle.CONFIG_PATH)) {
            makeConfig();
        }
        CONFIG.load(Files.newBufferedReader(CONFIG_PATH));
        ALLOW_FULL_FORMAT = !FALSE.equals(CONFIG.getProperty("FullVertexFormat"));
        MOVE_LAST = !FALSE.equals(CONFIG.getProperty("MoveRenderLastStage"));
        END_BATCH = !FALSE.equals(CONFIG.getProperty("EndBatch"));
        return CONFIG;
    }

    private static void makeConfig() {
        try (var writer = Files.newBufferedWriter(CONFIG_PATH);) {
            writer.write("# Modded Shader Config\n");
            writer.write("# ====================\n");
            writer.write("# Enable/disable all modded shader compat (true/false)\n");
            writer.write("All = true\n");
            writer.write("# ====================\n");
            writer.write("# Enable/disable for individual mods (true/false)\n");
            for (String mod : MODS) {
                writer.write(mod + " = false\n");
            }
            writer.write("# Allows the use of the full vertex format (true/false)\n");
            writer.write("FullVertexFormat = true\n");
            writer.write("# Moves the render after level stage (true/false)\n");
            writer.write("MoveRenderLastStage = true\n");
            writer.write("# Causes an endBatch call to happen after the level stage (true/false)\n");
            writer.write("EndBatch = true\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
