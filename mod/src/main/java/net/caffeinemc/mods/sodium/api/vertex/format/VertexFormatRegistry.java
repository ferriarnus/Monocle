package net.caffeinemc.mods.sodium.api.vertex.format;

import com.mojang.blaze3d.vertex.VertexFormat;

public interface VertexFormatRegistry {

    VertexFormatRegistry INSTANCE = new VertexFormatRegistry() {};

    static VertexFormatRegistry instance() {
        return INSTANCE;
    }

    default VertexFormatDescription get(VertexFormat dummy) {
        return null;
    }
}
