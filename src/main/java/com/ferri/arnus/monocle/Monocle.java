package com.ferri.arnus.monocle;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Monocle.MODID)
public class Monocle {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "monocle";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Monocle(IEventBus modEventBus, ModContainer modContainer) {

    }
}
