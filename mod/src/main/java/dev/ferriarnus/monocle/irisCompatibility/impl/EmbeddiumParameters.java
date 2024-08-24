package dev.ferriarnus.monocle.irisCompatibility.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;
import net.irisshaders.iris.pipeline.transform.parameter.SodiumParameters;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;

public class EmbeddiumParameters extends Parameters {
	public final ChunkVertexType vertexType;
	public final AlphaTest alpha;

	public EmbeddiumParameters(Patch patch, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap, AlphaTest alpha, ChunkVertexType vertexType) {
		super(patch, textureMap);
		this.vertexType = vertexType;
		this.alpha = alpha;
	}

	public AlphaTest getAlphaTest() {
		return this.alpha;
	}

	public TextureStage getTextureStage() {
		return TextureStage.GBUFFERS_AND_SHADOW;
	}

	public ChunkVertexType getVertexType() {
		return this.vertexType;
	}

	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (this.vertexType == null ? 0 : this.vertexType.hashCode());
		result = 31 * result + (this.alpha == null ? 0 : this.alpha.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!super.equals(obj)) {
			return false;
		} else if (this.getClass() != obj.getClass()) {
			return false;
		} else {
			EmbeddiumParameters other = (EmbeddiumParameters)obj;
			if (this.vertexType == null) {
				if (other.vertexType != null) {
					return false;
				}
			} else if (!this.vertexType.equals(other.vertexType)) {
				return false;
			}

			if (this.alpha == null) {
				return other.alpha == null;
			} else {
				return this.alpha.equals(other.alpha);
			}
		}
	}
}
