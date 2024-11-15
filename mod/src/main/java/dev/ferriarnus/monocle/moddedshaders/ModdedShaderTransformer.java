package dev.ferriarnus.monocle.moddedshaders;

import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumParameters;
import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumPatch;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.shader.ShaderType;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.VanillaParameters;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.taumc.glsl.ShaderParser;
import org.taumc.glsl.Transformer;
import org.taumc.glsl.grammar.GLSLParser;
import org.taumc.glsl.grammar.GLSLPreParser;

import java.util.EnumMap;
import java.util.Map;

public class ModdedShaderTransformer {
    static String tab = "";

    private static final int CACHE_SIZE = 100;
    private static final Object2ObjectLinkedOpenHashMap<TransformKey, Map<PatchShaderType, String>> shaderTransformationCache = new Object2ObjectLinkedOpenHashMap<>();
    public static final String[] REPLACE = new String[] {"Position", "Color", "UV0", "UV1", "UV2", "Normal"};

    private record TransformKey(EnumMap<PatchShaderType, String> inputs, VanillaParameters params) {}

    public static Map<PatchShaderType, String> transform(String name, String vertex, String geometry, String tessControl, String tessEval, String fragment, AlphaTest alpha, ShaderAttributeInputs input, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
        VanillaParameters parameters = new VanillaParameters(Patch.VANILLA, textureMap, alpha, false, true, input, geometry != null, tessControl != null || tessEval != null);

        if (vertex == null && geometry == null && tessControl == null && tessEval == null && fragment == null) {
            return null;
        } else {
            Map<PatchShaderType, String> result;

            EnumMap<PatchShaderType, String> inputs = new EnumMap<>(PatchShaderType.class);
            inputs.put(PatchShaderType.VERTEX, vertex);
            inputs.put(PatchShaderType.GEOMETRY, geometry);
            inputs.put(PatchShaderType.TESS_CONTROL, tessControl);
            inputs.put(PatchShaderType.TESS_EVAL, tessEval);
            inputs.put(PatchShaderType.FRAGMENT, fragment);

            var key = new TransformKey(inputs, parameters);

            result = shaderTransformationCache.getAndMoveToFirst(key);
            if(result == null) {
                result = transformInternal(name, inputs, parameters);
                // Clear this, we don't want whatever random type was last transformed being considered for the key
                parameters.type = null;
                if(shaderTransformationCache.size() >= CACHE_SIZE) {
                    shaderTransformationCache.removeLast();
                }
                shaderTransformationCache.putAndMoveToLast(key, result);
            }

            return result;
        }
    }

    private static Map<PatchShaderType, String> transformInternal(String name, EnumMap<PatchShaderType, String> inputs, VanillaParameters parameters) {
        EnumMap<PatchShaderType, String> result = new EnumMap<>(PatchShaderType.class);
        EnumMap<PatchShaderType, GLSLParser.Translation_unitContext> types = new EnumMap<>(PatchShaderType.class);
        EnumMap<PatchShaderType, String> prepatched = new EnumMap<>(PatchShaderType.class);

        for (PatchShaderType type : PatchShaderType.values()) {
            parameters.type = type;
            if (inputs.get(type) == null) {
                continue;
            }
            var parsedShader = ShaderParser.parseShader(inputs.get(type));
            var pre = parsedShader.pre();
            var translationUnit = parsedShader.full();
            var preparsed = pre.compiler_directive();
            String profile = null;
            String versionString = null;
            GLSLPreParser.Compiler_directiveContext version = null;
            for (var entry: preparsed) {
                if (entry.version_directive() != null) {
                    version = entry;
                    if (entry.version_directive().number() != null) {
                        versionString = entry.version_directive().number().getText();
                    }
                    if (entry.version_directive().profile() != null) {
                        profile = entry.version_directive().profile().getText();
                    }
                }
            }
            pre.children.remove(version);
            if (versionString == null) {
                continue;
            }
            String profileString = "#version " + versionString + " " + (profile == null ? "" : profile);
            if ((profile == null && Integer.parseInt(versionString) >= 150 || profile != null && profile.equals("core"))) {
                if (Integer.parseInt(versionString) < 330) {
                    profileString = "#version 330 core";
                }

                patchCore(translationUnit, parameters);
            } else {
                if (Integer.parseInt(versionString) < 330) {
                    profileString = "#version 330 core";
                } else {
                    profileString = "#version " + versionString + " core";
                }
                patch(translationUnit, parameters);
            }
            //CompTransformer.transformEach(translationUnit, parameters);
            types.put(type, translationUnit);
            prepatched.put(type, getFormattedShader(pre, profileString));
        }
        //CompTransformer.transformGrouped(types, parameters);
        for (var entry : types.entrySet()) {
            result.put(entry.getKey(), getFormattedShader(entry.getValue(), prepatched.get(entry.getKey())));
        }
        return result;
    }

    private static void patch(GLSLParser.Translation_unitContext root, VanillaParameters parameters) {
        Transformer transformer = new Transformer(root);
    }


    private static void patchCore(GLSLParser.Translation_unitContext root, VanillaParameters parameters) {
        Transformer transformer = new Transformer(root);

        for (String value : REPLACE) {
            transformer.rename(value, "iris_" + value);
        }

        for (String value : ModdedShaderPipeline.UNIFORMS) {
            transformer.rename(value, "iris_" + value);
        }

        transformer.rename("Sampler0", "gtexture");
        transformer.rename("Sampler1", "iris_overlay");
        transformer.rename("Sampler2", "lightmap");

        if (!parameters.inputs.hasLight()) {
            transformer.rename("iris_UV1", "iris_OverlayUV");
            transformer.removeVariable("iris_OverlayUV");
            transformer.prependMain("ivec2 iris_OverlayUV = ivec2(0, 0);");
            transformer.injectVariable("in ivec2 iris_UV1;");
        }
    }

    public static String getFormattedShader(ParseTree tree, String string) {
        StringBuilder sb = new StringBuilder(string + "\n");
        getFormattedShader(tree, sb);
        return sb.toString();
    }

    private static void getFormattedShader(ParseTree tree, StringBuilder stringBuilder) {
        if (tree instanceof TerminalNode) {
            String text = tree.getText();
            if (text.equals("<EOF>")) {
                return;
            }
            if (text.equals("#")) {
                stringBuilder.append("\n#");
                return;
            }
            stringBuilder.append(text);
            if (text.equals("{")) {
                stringBuilder.append(" \n\t");
                tab = "\t";
            }

            if (text.equals("}")) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 2);
                tab = "";
            }
            stringBuilder.append(text.equals(";") ? " \n" + tab : " ");
        } else {
            for(int i = 0; i < tree.getChildCount(); ++i) {
                getFormattedShader(tree.getChild(i), stringBuilder);
            }
        }

    }
}
