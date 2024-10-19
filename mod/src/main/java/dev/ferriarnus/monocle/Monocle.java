package dev.ferriarnus.monocle;

import net.neoforged.api.distmarker.Dist;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(value = Monocle.MODID, dist = Dist.CLIENT)
public class Monocle {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "monocle";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public Monocle(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Loaded Monocle v{}", modContainer.getModInfo().getVersion().toString());
    }
}
