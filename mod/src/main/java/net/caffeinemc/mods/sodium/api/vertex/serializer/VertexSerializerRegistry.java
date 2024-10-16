package net.caffeinemc.mods.sodium.api.vertex.serializer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

public interface VertexSerializerRegistry {

    VertexSerializerRegistry INSTANCE = new VertexSerializerRegistry() {};

    static VertexSerializerRegistry instance() {
        return INSTANCE;
    }

    default void registerSerializer(VertexFormat dummy, VertexFormat dummy2, VertexSerializer serializer) {

    }

}
