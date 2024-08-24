package net.caffeinemc.mods.sodium.api.vertex.serializer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.embeddedt.embeddium.api.vertex.serializer.VertexSerializer;

public class VertexSerializerRegistry {

    private final static VertexSerializerRegistry INSTANCE = new VertexSerializerRegistry();

    public static VertexSerializerRegistry instance() {
        return INSTANCE;
    }

    int get(DefaultVertexFormat format) {
        return 0;
    }

    void registerSerializer(int dummy, int dummy2, VertexSerializer serializer) {

    }

}
