package dev.ferriarnus.monocle.irisCompatibility.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;

import java.util.Objects;

public final class EmbeddiumParameters {
    private final Patch patch;
    private final Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap;
    private final AlphaTest alpha;
    private final ChunkVertexType vertexType;
    public PatchShaderType type;
    public String name;

    public EmbeddiumParameters(Patch patch, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap, AlphaTest alpha, ChunkVertexType vertexType) {
        this.patch = patch;
        this.textureMap = textureMap;
        this.alpha = alpha;
        this.vertexType = vertexType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EmbeddiumParameters) obj;
        return Objects.equals(this.patch, that.patch) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.textureMap, that.textureMap) &&
                Objects.equals(this.alpha, that.alpha) &&
                Objects.equals(this.vertexType, that.vertexType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patch, type, textureMap, alpha, vertexType);
    }

    public AlphaTest getAlphaTest() {
        return alpha;
    }

    public TextureStage getTextureStage() {
        return TextureStage.GBUFFERS_AND_SHADOW;
    }

    public Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getTextureMap() {
        return textureMap;
    }
}
