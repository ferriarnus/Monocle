package net.caffeinemc.mods.sodium.api.vertex.serializer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;

public interface VertexSerializerRegistry {

    VertexSerializerRegistry INSTANCE = new VertexSerializerRegistry() {};

    static VertexSerializerRegistry instance() {
        return INSTANCE;
    }

    default void registerSerializer(VertexFormatDescription dummy, VertexFormatDescription dummy2, VertexSerializer serializer) {

    }

}
