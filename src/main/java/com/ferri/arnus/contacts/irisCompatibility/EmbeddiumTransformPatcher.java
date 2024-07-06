package com.ferri.arnus.contacts.irisCompatibility;

import com.ferri.arnus.contacts.mixins.TransformPatcherInvoker;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.shaderpack.texture.TextureStage;

import java.util.Map;

public class EmbeddiumTransformPatcher {

    public static Map<PatchShaderType, String> patchEmbeddium(String name, String vertex, String geometry, String tessControl, String tessEval, String fragment,
                                                               AlphaTest alpha, ShaderAttributeInputs inputs,
                                                               Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
        return TransformPatcherInvoker.transform(name, vertex, geometry, tessControl, tessEval, fragment,
                new EmbeddiumParameters(EmbeddiumPatch.EMBEDDIUM, textureMap, alpha, inputs));
    }
}
