package dev.ferriarnus.monocle;

import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumParameters;
import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumPatch;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.shader.ShaderType;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;
import net.irisshaders.iris.pipeline.transform.transformer.CommonTransformer;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.taumc.glsl.StorageCollector;
import org.taumc.glsl.Util;
import org.taumc.glsl.grammar.GLSLLexer;
import org.taumc.glsl.grammar.GLSLParser;
import org.taumc.glsl.grammar.GLSLPreParser;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShaderTransformer {
    static String tab = "";

    private static final int CACHE_SIZE = 100;
    private static final Object2ObjectLinkedOpenHashMap<TransformKey, Map<PatchShaderType, String>> shaderTransformationCache = new Object2ObjectLinkedOpenHashMap<>();

    private record TransformKey(EnumMap<PatchShaderType, String> inputs, EmbeddiumParameters params) {}

    public static Map<PatchShaderType, String> transform(String name, String vertex, String geometry, String tessControl, String tessEval, String fragment, AlphaTest alpha, ChunkVertexType vertexType, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
        EmbeddiumParameters parameters = new EmbeddiumParameters(EmbeddiumPatch.EMBEDDIUM, textureMap, alpha, vertexType);

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

    private static Map<PatchShaderType, String> transformInternal(String name, EnumMap<PatchShaderType, String> inputs, EmbeddiumParameters parameters) {
        EnumMap<PatchShaderType, String> result = new EnumMap<>(PatchShaderType.class);
        EnumMap<PatchShaderType, GLSLParser.Translation_unitContext> types = new EnumMap<>(PatchShaderType.class);
        EnumMap<PatchShaderType, String> prepatched = new EnumMap<>(PatchShaderType.class);

        for (PatchShaderType type : PatchShaderType.values()) {
            parameters.type = type;
            if (inputs.get(type) == null) {
                continue;
            }
            GLSLLexer lexer = new GLSLLexer(CharStreams.fromString(inputs.get(type)));
            GLSLPreParser preParser = new GLSLPreParser(new BufferedTokenStream(lexer));
            GLSLParser parser = new GLSLParser(new CommonTokenStream(lexer));
            parser.setBuildParseTree(true);
            var pre = preParser.translation_unit();
            var translationUnit = parser.translation_unit();
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
            String profileString = "#version " + versionString + " " + profile;
            if ((profile == null || !profile.equals("core")) && Integer.parseInt(versionString) < 150) {
                if (Integer.parseInt(versionString) < 330) {
                    profileString = "#version 330 core";
                } else {
                    profileString = "#version " + versionString + " core";
                }


                ShaderTransformer.patch(translationUnit, parameters);
            } else {
                if (Integer.parseInt(versionString) < 330) {
                    profileString = "#version 330 core";
                }
                ShaderTransformer.patchCore(translationUnit, parameters);
            }
            CompTransformer.transformEach(translationUnit, parameters);
            types.put(type, translationUnit);
            prepatched.put(type, getFormattedShader(pre, profileString));
        }
        CompTransformer.transformGrouped(types, parameters);
        for (var entry : types.entrySet()) {
            result.put(entry.getKey(), getFormattedShader(entry.getValue(), prepatched.get(entry.getKey())));
        }
        return result;
    }

    private static void patch(GLSLParser.Translation_unitContext translationUnit, EmbeddiumParameters parameters) {
        commonPatch(translationUnit, parameters);

        replaceMidTexCoord(translationUnit, 1.0f / 32768.0f);

        Util.replaceExpression(translationUnit, "gl_TextureMatrix[0]", "mat4(1.0f)");
        Util.replaceExpression(translationUnit, "gl_TextureMatrix[1]", "iris_LightmapTextureMatrix");
        Util.injectFunction(translationUnit, "uniform mat4 iris_LightmapTextureMatrix;");
        Util.rename(translationUnit, "gl_ProjectionMatrix", "iris_ProjectionMatrix");

        if (parameters.type.glShaderType == ShaderType.VERTEX) {

            Util.rename(translationUnit, "gl_MultiTexCoord2", "gl_MultiTexCoord1");
            Util.replaceExpression(translationUnit, "gl_MultiTexCoord0", "vec4(_vert_tex_diffuse_coord, 0.0f, 1.0f)");
            Util.replaceExpression(translationUnit, "gl_MultiTexCoord1", "vec4(_vert_tex_light_coord, 0.0f, 1.0f)");

            patchMultiTexCoord3(translationUnit, parameters);

            // gl_MultiTexCoord0 and gl_MultiTexCoord1 are the only valid inputs (with
            // gl_MultiTexCoord2 and gl_MultiTexCoord3 as aliases), other texture
            // coordinates are not valid inputs.
            replaceGlMultiTexCoordBounded(translationUnit, 4, 7);
        }

        Util.rename(translationUnit, "gl_Color", "_vert_color");

        if (parameters.type.glShaderType == ShaderType.VERTEX) {
            Util.rename(translationUnit,"gl_Normal", "iris_Normal");
            Util.injectVariable(translationUnit,"in vec3 iris_Normal;");
        }

        // TODO: Should probably add the normal matrix as a proper uniform that's
        // computed on the CPU-side of things
        Util.replaceExpression(translationUnit, "gl_NormalMatrix",
                "iris_NormalMatrix");
        Util.injectVariable(translationUnit,"uniform mat3 iris_NormalMatrix;");

        Util.injectVariable(translationUnit, "uniform mat4 iris_ModelViewMatrixInverse;");

        Util.injectVariable(translationUnit, "uniform mat4 iris_ProjectionMatrixInverse;");

        // TODO: All of the transformed variants of the input matrices, preferably
        // computed on the CPU side...
        Util.rename(translationUnit,"gl_ModelViewMatrix", "iris_ModelViewMatrix");
        Util.rename(translationUnit,"gl_ModelViewMatrixInverse", "iris_ModelViewMatrixInverse");
        Util.rename(translationUnit,"gl_ProjectionMatrixInverse", "iris_ProjectionMatrixInverse");

        if (parameters.type.glShaderType == ShaderType.VERTEX) {
            // TODO: Vaporwave-Shaderpack expects that vertex positions will be aligned to
            // chunks.
            if (Util.containsCall(translationUnit, "ftransform")) {
                Util.injectFunction(translationUnit, "vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
            }
            Util.injectFunction(translationUnit,
                    "uniform mat4 iris_ProjectionMatrix;");
            Util.injectFunction(translationUnit,
                    "uniform mat4 iris_ModelViewMatrix;");
            Util.injectFunction(translationUnit,
                    "uniform vec3 u_RegionOffset;");
            Util.injectFunction(translationUnit,
                    // _draw_translation replaced with Chunks[_draw_id].offset.xyz
                    "vec4 getVertexPosition() { return vec4(_vert_position + u_RegionOffset + _get_draw_translation(_draw_id), 1.0f); }");
            Util.replaceExpression(translationUnit, "gl_Vertex", "getVertexPosition()");

            // inject here so that _vert_position is available to the above. (injections
            // inject in reverse order if performed piece-wise but in correct order if
            // performed as an array of injections)
            injectVertInit(translationUnit, parameters);
        } else {
            Util.injectVariable(translationUnit,
                    "uniform mat4 iris_ModelViewMatrix;");
            Util.injectFunction(translationUnit,
                    "uniform mat4 iris_ProjectionMatrix;");
        }

        Util.replaceExpression(translationUnit, "gl_ModelViewProjectionMatrix",
                "(iris_ProjectionMatrix * iris_ModelViewMatrix)");

        applyIntelHd4000Workaround(translationUnit);

    }

    public static void applyIntelHd4000Workaround(GLSLParser.Translation_unitContext translationUnit) {
        Util.renameFunctionCall(translationUnit, "ftransform", "iris_ftransform");
    }


    private static void replaceGlMultiTexCoordBounded(GLSLParser.Translation_unitContext translationUnit, int min, int max) {
        for (int i = min; i <= max; i++) {
            Util.replaceExpression(translationUnit, "gl_MultiTexCoord" + i, "vec4(0.0, 0.0, 0.0, 1.0)");
        }
    }

    private static void patchMultiTexCoord3(GLSLParser.Translation_unitContext translationUnit, EmbeddiumParameters parameters) {
        if (parameters.type.glShaderType == ShaderType.VERTEX && Util.hasVariable(translationUnit, "gl_MultiTexCoord3") && !Util.hasVariable(translationUnit, "mc_midTexCoord")) {
            Util.rename(translationUnit, "gl_MultiTexCoord3", "mc_midTexCoord");
            Util.injectVariable(translationUnit, "attribute vec4 mc_midTexCoord;");
        }
    }

    private static void patchCore(GLSLParser.Translation_unitContext translationUnit, EmbeddiumParameters parameters) {
        Util.rename(translationUnit,"alphaTestRef", "iris_currentAlphaTest");
        Util.rename(translationUnit,"modelViewMatrix", "iris_ModelViewMatrix");
        Util.rename(translationUnit,"modelViewMatrixInverse", "iris_ModelViewMatrixInverse");
        Util.rename(translationUnit,"projectionMatrix", "iris_ProjectionMatrix");
        Util.rename(translationUnit,"projectionMatrixInverse", "iris_ProjectionMatrixInverse");
        Util.rename(translationUnit,"normalMatrix", "iris_NormalMatrix");
        Util.rename(translationUnit,"chunkOffset", "u_RegionOffset");
        Util.injectVariable(translationUnit, "uniform mat4 iris_LightmapTextureMatrix;");

        if (parameters.type == PatchShaderType.VERTEX) {
            // _draw_translation replaced with Chunks[_draw_id].offset.xyz
            Util.replaceExpression(translationUnit,"vaPosition", "_vert_position + _get_draw_translation(_draw_id)");
            Util.replaceExpression(translationUnit, "vaColor", "_vert_color");
            Util.rename(translationUnit,"vaNormal", "iris_Normal");
            Util.replaceExpression(translationUnit, "vaUV0", "_vert_tex_diffuse_coord");
            Util.replaceExpression(translationUnit, "vaUV1", "ivec2(0, 10)");
            Util.rename(translationUnit,"vaUV2", "a_LightCoord");

            Util.replaceExpression(translationUnit, "textureMatrix", "mat4(1.0f)");

            injectVertInit(translationUnit, parameters);
        }
    }

    public static void replaceMidTexCoord(GLSLParser.Translation_unitContext translationUnit, float textureScale) {
        int type = Util.findType(translationUnit, "mc_midTexCoord");
        if (type != 0) {
            Util.removeVariable(translationUnit, "mc_midTexCoord");
        }
        Util.replaceExpression(translationUnit, "mc_midTexCoord", "iris_MidTex");
        switch (type) {
            case GLSLLexer.BOOL:
                break;
            case GLSLLexer.FLOAT:
                Util.injectFunction(translationUnit, "float iris_MidTex = (mc_midTexCoord.x * " + textureScale + ").x;"); //TODO go back to variable if order is fixed
                break;
            case GLSLLexer.VEC2:
                Util.injectFunction(translationUnit, "vec2 iris_MidTex = (mc_midTexCoord.xy * " + textureScale + ").xy;");
                break;
            case GLSLLexer.VEC3:
                Util.injectFunction(translationUnit, "vec3 iris_MidTex = vec3(mc_midTexCoord.xy * " + textureScale + ", 0.0);");
                break;
            case GLSLLexer.VEC4:
                Util.injectFunction(translationUnit, "vec4 iris_MidTex = vec4(mc_midTexCoord.xy * " + textureScale + ", 0.0, 1.0);");
                break;
            default:

        }

        Util.injectVariable(translationUnit, "in vec2 mc_midTexCoord;"); //TODO why is this inserted oddly?

    }

    private static void injectVertInit(GLSLParser.Translation_unitContext translationUnit, EmbeddiumParameters parameters) {
        String separateAo = WorldRenderingSettings.INSTANCE.shouldUseSeparateAo() ? "a_Color" : "vec4(a_Color.rgb * a_Color.a, 1.0f)";
        Util.injectVariable(translationUnit,
                // translated from sodium's chunk_vertex.glsl
                "vec3 _vert_position;");
        Util.injectVariable(translationUnit,
                "vec2 _vert_tex_diffuse_coord;");
        Util.injectVariable(translationUnit,
                "ivec2 _vert_tex_light_coord;");
        Util.injectVariable(translationUnit,
                "vec4 _vert_color;");
        Util.injectVariable(translationUnit,
                "uint _draw_id;");
        Util.injectFunction(translationUnit,
                "const uint MATERIAL_USE_MIP_OFFSET = 0u;");

        Util.injectFunction(translationUnit,
                "vec3 _get_draw_translation(uint pos) {\n" +
                        "    return _get_relative_chunk_coord(pos) * vec3(16.0f);\n" +
                        "}");

        Util.injectFunction(translationUnit,

                "uvec3 _get_relative_chunk_coord(uint pos) {\n" +
                        "    // Packing scheme is defined by LocalSectionIndex\n" +
                        "    return uvec3(pos) >> uvec3(5u, 0u, 2u) & uvec3(7u, 3u, 7u);\n" +
                        "}");

        Util.injectFunction(translationUnit,
                "void _vert_init() {" +
                        "_vert_position = (vec3(a_PosId.xyz) * 4.8828125E-4f + -8.0f"
                        + ");" +
                        "_vert_tex_diffuse_coord = (a_TexCoord * " + (1.0f / 32768.0f) + ");" +
                        "_vert_tex_light_coord = a_LightCoord;" +
                        "_vert_color = " + separateAo + ";" +
                        "_draw_id = (a_PosId.w >> 8u) & 0xFFu; }");

        Util.injectFunction(translationUnit,
                "float _material_mip_bias(uint material) {\n" +
                        "    return ((material >> MATERIAL_USE_MIP_OFFSET) & 1u) != 0u ? 0.0f : -4.0f;\n" +
                        "}");

        addIfNotExists(translationUnit, "a_PosId", "in uvec4 a_PosId;");
        addIfNotExists(translationUnit, "a_TexCoord", "in vec2 a_TexCoord;");
        addIfNotExists(translationUnit, "a_Color", "in vec4 a_Color;");
        addIfNotExists(translationUnit, "a_LightCoord", "in ivec2 a_LightCoord;");
        Util.prependMain(translationUnit, "_vert_init();");
    }

    private static void addIfNotExists(GLSLParser.Translation_unitContext translationUnit, String name, String code) {
        if (!Util.hasVariable(translationUnit, name)) {
            Util.injectVariable(translationUnit, code);
        }
    }

    private static void commonPatch(GLSLParser.Translation_unitContext root, EmbeddiumParameters parameters) {
        Util.rename(root, "gl_FogFragCoord", "iris_FogFragCoord");
        if (parameters.type.glShaderType == ShaderType.VERTEX) {
            Util.injectVariable(root, "out float iris_FogFragCoord;");
            Util.prependMain(root, "iris_FogFragCoord = 0.0f;");
        } else if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
            Util.injectVariable(root, "in float iris_FogFragCoord;");
        }

        if (parameters.type.glShaderType == ShaderType.VERTEX) {
            Util.injectVariable(root, "vec4 iris_FrontColor;");
            Util.replaceExpression(root, "gl_FrontColor", "iris_FrontColor");
        }

        if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
            if (Util.containsCall(root, "gl_FragColor")) {
                Util.replaceExpression(root, "gl_FragColor", "gl_FragData[0]");
            }

            if (Util.containsCall(root, "gl_TexCoord")) {
                Util.rename(root, "gl_TexCoord", "irs_texCoords");
                Util.injectVariable(root, "in vec4 irs_texCoords[3];");
            }

            if (Util.containsCall(root, "gl_Color")) {
                Util.rename(root, "gl_Color", "irs_Color");
                Util.injectVariable(root, "in vec4 irs_Color;");
            }

            Set<Integer> found = new HashSet<>();
            Util.renameArray(root, "gl_FragData", "iris_FragData", found);

            for (Integer i : found) {
                Util.injectFunction(root, "layout (location = " + i + ") out vec4 iris_FragData" + i + ";");
            }

            if (parameters.getAlphaTest() != AlphaTest.ALWAYS && found.contains(0)) {
                Util.injectVariable(root, "uniform float iris_currentAlphaTest;");
                Util.appendMain(root, parameters.getAlphaTest().toExpression("iris_FragData0.a", "iris_currentAlphaTest", ""));
            }

        }

        if (parameters.type.glShaderType == ShaderType.VERTEX || parameters.type.glShaderType == ShaderType.FRAGMENT) {
            upgradeStorageQualifiers(root, parameters);
        }

        if (Util.containsCall(root, "texture") && Util.hasVariable(root, "texture")) {
            Util.rename(root, "texture", "gtexture");
        }

        if (Util.containsCall(root, "gcolor") && Util.hasVariable(root, "gcolor")) {
            Util.rename(root, "gcolor", "gtexture");
        }

        Util.rename(root, "gl_Fog", "iris_Fog");
        Util.injectVariable(root, "uniform float iris_FogDensity;");
        Util.injectVariable(root,"uniform float iris_FogStart;");
        Util.injectVariable(root,"uniform float iris_FogEnd;");
        Util.injectVariable(root,"uniform vec4 iris_FogColor;");
        Util.injectFunction(root,"struct iris_FogParameters {vec4 color;float density;float start;float end;float scale;};");
        Util.injectFunction(root,"iris_FogParameters iris_Fog = iris_FogParameters(iris_FogColor, iris_FogDensity, iris_FogStart, iris_FogEnd, 1.0f / (iris_FogEnd - iris_FogStart));");

        Util.renameFunctionCall(root, "texture2D", "texture");
        Util.renameFunctionCall(root, "texture3D", "texture");
        Util.renameFunctionCall(root, "texture2DLod", "textureLod");
        Util.renameFunctionCall(root, "texture3DLod", "textureLod");
        Util.renameFunctionCall(root, "texture2DGrad", "textureGrad");
        Util.renameFunctionCall(root, "texture2DGradARB", "textureGrad");
        Util.renameFunctionCall(root, "texture3DGrad", "textureGrad");
        Util.renameFunctionCall(root, "texelFetch2D", "texelFetch");
        Util.renameFunctionCall(root, "texelFetch3D", "texelFetch");
        Util.renameFunctionCall(root, "textureSize2D", "textureSize");
        Util.renameAndWrapShadow(root, "shadow2D", "texture");
        Util.renameAndWrapShadow(root, "shadow2DLod", "textureLod");
    }



    public static void upgradeStorageQualifiers(GLSLParser.Translation_unitContext root, EmbeddiumParameters parameters) {
        List<TerminalNode> tokens = new ArrayList<>();
        ParseTreeWalker.DEFAULT.walk(new StorageCollector(tokens), root);

        for (TerminalNode node : tokens) {
            if (!(node.getSymbol() instanceof CommonToken token)) {
                return;
            }
            if (token.getType() == GLSLParser.ATTRIBUTE) {
                token.setType(GLSLParser.IN);
                token.setText(GLSLParser.VOCABULARY.getLiteralName(GLSLParser.IN).replace("'", ""));
            }
            else if (token.getType() == GLSLParser.VARYING) {
                if (parameters.type.glShaderType == ShaderType.VERTEX) {
                    token.setType(GLSLParser.OUT);
                    token.setText(GLSLParser.VOCABULARY.getLiteralName(GLSLParser.OUT).replace("'", ""));
                } else {
                    token.setType(GLSLParser.IN);
                    token.setText(GLSLParser.VOCABULARY.getLiteralName(GLSLParser.IN).replace("'", ""));
                }
            }
        }
    }

    private static String getFormattedShader(ParseTree tree, String string) {
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
