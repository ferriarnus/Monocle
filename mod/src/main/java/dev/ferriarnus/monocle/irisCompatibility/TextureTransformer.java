package dev.ferriarnus.monocle.irisCompatibility;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import org.taumc.glsl.Transformer;
import org.taumc.glsl.grammar.GLSLParser;

public class TextureTransformer {
    public static void transform(GLSLParser.Translation_unitContext unit, TextureStage stage, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
        Transformer transformer = new Transformer(unit);
        textureMap.forEach((stringTextureTypeTextureStageTri, s) -> {
            if (stringTextureTypeTextureStageTri.third() == stage) {
                String name = stringTextureTypeTextureStageTri.first();

                if (transformer.hasVariable(name)) {
                    transformer.rename(stringTextureTypeTextureStageTri.first(), s);
                }
            }
        });
    }
}
