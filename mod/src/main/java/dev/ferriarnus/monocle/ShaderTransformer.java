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
import org.taumc.glsl.Transformer;
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
            if ((profile == null && Integer.parseInt(versionString) >= 150 || profile != null && profile.equals("core"))) {
                if (Integer.parseInt(versionString) < 330) {
                    profileString = "#version 330 core";
                }

                ShaderTransformer.patchCore(translationUnit, parameters);
            } else {
                if (Integer.parseInt(versionString) < 330) {
                    profileString = "#version 330 core";
                } else {
                    profileString = "#version " + versionString + " core";
                }
                ShaderTransformer.patch(translationUnit, parameters);
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

    private static void patch(GLSLParser.Translation_unitContext root, EmbeddiumParameters parameters) {
        Transformer transformer = new Transformer(root);
        commonPatch(transformer, parameters);

        replaceMidTexCoord(transformer, 1.0f / 32768.0f);
        replaceMCEntity(transformer);

        transformer.replaceExpression("gl_TextureMatrix[0]", "mat4(1.0f)");
        transformer.replaceExpression("gl_TextureMatrix[1]", "iris_LightmapTextureMatrix");
        transformer.injectFunction("uniform mat4 iris_LightmapTextureMatrix;");
        transformer.rename("gl_ProjectionMatrix", "iris_ProjectionMatrix");

        if (parameters.type.glShaderType == ShaderType.VERTEX) {

            transformer.rename("gl_MultiTexCoord2", "gl_MultiTexCoord1");
            transformer.replaceExpression("gl_MultiTexCoord0", "vec4(_vert_tex_diffuse_coord, 0.0f, 1.0f)");
            transformer.replaceExpression("gl_MultiTexCoord1", "vec4(_vert_tex_light_coord, 0.0f, 1.0f)");

            patchMultiTexCoord3(transformer, parameters);

            // gl_MultiTexCoord0 and gl_MultiTexCoord1 are the only valid inputs (with
            // gl_MultiTexCoord2 and gl_MultiTexCoord3 as aliases), other texture
            // coordinates are not valid inputs.
            replaceGlMultiTexCoordBounded(transformer, 4, 7);
        }

        transformer.rename("gl_Color", "_vert_color");

        if (parameters.type.glShaderType == ShaderType.VERTEX) {
            transformer.rename("gl_Normal", "iris_Normal");
            transformer.injectVariable("in vec3 iris_Normal;");
        }

        // TODO: Should probably add the normal matrix as a proper uniform that's
        // computed on the CPU-side of things
        transformer.replaceExpression("gl_NormalMatrix",
                "iris_NormalMatrix");
        transformer.injectVariable("uniform mat3 iris_NormalMatrix;");

        transformer.injectVariable("uniform mat4 iris_ModelViewMatrixInverse;");

        transformer.injectVariable("uniform mat4 iris_ProjectionMatrixInverse;");

        // TODO: All of the transformed variants of the input matrices, preferably
        // computed on the CPU side...
        transformer.rename("gl_ModelViewMatrix", "iris_ModelViewMatrix");
        transformer.rename("gl_ModelViewMatrixInverse", "iris_ModelViewMatrixInverse");
        transformer.rename("gl_ProjectionMatrixInverse", "iris_ProjectionMatrixInverse");

        if (parameters.type.glShaderType == ShaderType.VERTEX) {
            // TODO: Vaporwave-Shaderpack expects that vertex positions will be aligned to
            // chunks.
            if (transformer.containsCall("ftransform")) {
                transformer.injectFunction("vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
            }
            transformer.injectFunction(
                    "uniform mat4 iris_ProjectionMatrix;");
            transformer.injectFunction(
                    "uniform mat4 iris_ModelViewMatrix;");
            transformer.injectFunction(
                    "uniform vec3 u_RegionOffset;");
            transformer.injectFunction(
                    // _draw_translation replaced with Chunks[_draw_id].offset.xyz
                    "vec4 getVertexPosition() { return vec4(_vert_position + u_RegionOffset + _get_draw_translation(_draw_id), 1.0f); }");
            transformer.replaceExpression("gl_Vertex", "getVertexPosition()");

            // inject here so that _vert_position is available to the above. (injections
            // inject in reverse order if performed piece-wise but in correct order if
            // performed as an array of injections)
            injectVertInit(transformer, parameters);
        } else {
            transformer.injectVariable(
                    "uniform mat4 iris_ModelViewMatrix;");
            transformer.injectFunction(
                    "uniform mat4 iris_ProjectionMatrix;");
        }

        transformer.replaceExpression("gl_ModelViewProjectionMatrix",
                "(iris_ProjectionMatrix * iris_ModelViewMatrix)");

        applyIntelHd4000Workaround(transformer);

    }

    public static void applyIntelHd4000Workaround(Transformer transformer) {
        transformer.renameFunctionCall("ftransform", "iris_ftransform");
    }


    private static void replaceGlMultiTexCoordBounded(Transformer transformer, int min, int max) {
        for (int i = min; i <= max; i++) {
            transformer.replaceExpression("gl_MultiTexCoord" + i, "vec4(0.0, 0.0, 0.0, 1.0)");
        }
    }

    private static void patchMultiTexCoord3(Transformer transformer, EmbeddiumParameters parameters) {
        if (parameters.type.glShaderType == ShaderType.VERTEX && transformer.hasVariable("gl_MultiTexCoord3") && !transformer.hasVariable("mc_midTexCoord")) {
            transformer.rename("gl_MultiTexCoord3", "mc_midTexCoord");
            transformer.injectVariable("attribute vec4 mc_midTexCoord;");
        }
    }

    private static void patchCore(GLSLParser.Translation_unitContext root, EmbeddiumParameters parameters) {
        Transformer transformer = new Transformer(root);
        transformer.rename("alphaTestRef", "iris_currentAlphaTest");
        transformer.rename("modelViewMatrix", "iris_ModelViewMatrix");
        transformer.rename("modelViewMatrixInverse", "iris_ModelViewMatrixInverse");
        transformer.rename("projectionMatrix", "iris_ProjectionMatrix");
        transformer.rename("projectionMatrixInverse", "iris_ProjectionMatrixInverse");
        transformer.rename("normalMatrix", "iris_NormalMatrix");
        transformer.rename("chunkOffset", "u_RegionOffset");
        transformer.injectVariable("uniform mat4 iris_LightmapTextureMatrix;");

        if (parameters.type == PatchShaderType.VERTEX) {
            // _draw_translation replaced with Chunks[_draw_id].offset.xyz
            transformer.replaceExpression("vaPosition", "_vert_position + _get_draw_translation(_draw_id)");
            transformer.replaceExpression("vaColor", "_vert_color");
            transformer.rename("vaNormal", "iris_Normal");
            transformer.replaceExpression("vaUV0", "_vert_tex_diffuse_coord");
            transformer.replaceExpression("vaUV1", "ivec2(0, 10)");
            transformer.rename("vaUV2", "a_LightCoord");

            transformer.replaceExpression("textureMatrix", "mat4(1.0f)");
            replaceMidTexCoord(transformer, 1.0f / 32768.0f);
            replaceMCEntity(transformer);

            injectVertInit(transformer, parameters);
        }
    }

    public static void replaceMidTexCoord(Transformer transformer, float textureScale) {
        int type = transformer.findType("mc_midTexCoord");
        if (type != 0) {
            transformer.removeVariable("mc_midTexCoord");
        }
        transformer.replaceExpression("mc_midTexCoord", "iris_MidTex");
        switch (type) {
            case 0:
                return;
            case GLSLLexer.BOOL:
                return;
            case GLSLLexer.FLOAT:
                transformer.injectFunction("float iris_MidTex = (mc_midTexCoord.x * " + textureScale + ").x;"); //TODO go back to variable if order is fixed
                break;
            case GLSLLexer.VEC2:
                transformer.injectFunction("vec2 iris_MidTex = (mc_midTexCoord.xy * " + textureScale + ").xy;");
                break;
            case GLSLLexer.VEC3:
                transformer.injectFunction("vec3 iris_MidTex = vec3(mc_midTexCoord.xy * " + textureScale + ", 0.0);");
                break;
            case GLSLLexer.VEC4:
                transformer.injectFunction("vec4 iris_MidTex = vec4(mc_midTexCoord.xy * " + textureScale + ", 0.0, 1.0);");
                break;
            default:

        }

        transformer.injectVariable("in vec2 mc_midTexCoord;"); //TODO why is this inserted oddly?

    }

    public static void replaceMCEntity(Transformer transformer) {
        int type = transformer.findType("mc_Entity");
        if (type != 0) {
            transformer.removeVariable("mc_Entity");
        }
        transformer.replaceExpression("mc_Entity", "iris_Entity");
        switch (type) {
            case 0:
                return;
            case GLSLLexer.BOOL:
                return;
            case GLSLLexer.FLOAT:
                transformer.injectFunction("float iris_Entity = int(mc_Entity >> 1u) - 1;");
                break;
            case GLSLLexer.VEC2:
                transformer.injectFunction("vec2 iris_Entity = vec2(int(mc_Entity >> 1u) - 1, mc_Entity & 1u);");
                break;
            case GLSLLexer.VEC3:
                transformer.injectFunction("vec3 iris_Entity = vec3(int(mc_Entity >> 1u) - 1, mc_Entity & 1u, 0.0);");
                break;
            case GLSLLexer.VEC4:
                transformer.injectFunction("vec4 iris_Entity = vec4(int(mc_Entity >> 1u) - 1, mc_Entity & 1u, 0.0, 1.0);");
                break;
            case GLSLLexer.UINT:
                transformer.injectFunction("uint iris_Entity = int(mc_Entity >> 1u) - 1;");
                break;
            case GLSLLexer.IVEC2:
                transformer.injectFunction("ivec2 iris_Entity = ivec2(int(mc_Entity >> 1u) - 1, mc_Entity & 1u);");
                break;
            case GLSLLexer.IVEC3:
                transformer.injectFunction("ivec3 iris_Entity = ivec3(int(mc_Entity >> 1u) - 1, mc_Entity & 1u, 0);");
                break;
            case GLSLLexer.IVEC4:
                transformer.injectFunction("ivec4 iris_Entity = ivec4(int(mc_Entity >> 1u) - 1, mc_Entity & 1u, 0, 1);");
                break;
            default:

        }

        transformer.injectVariable("in uint mc_Entity;"); //TODO why is this inserted oddly?

    }

    private static void injectVertInit(Transformer transformer, EmbeddiumParameters parameters) {
        String separateAo = WorldRenderingSettings.INSTANCE.shouldUseSeparateAo() ? "a_Color" : "vec4(a_Color.rgb * a_Color.a, 1.0f)";
        transformer.injectVariable(
                // translated from sodium's chunk_vertex.glsl
                "vec3 _vert_position;");
        transformer.injectVariable(
                "vec2 _vert_tex_diffuse_coord;");
        transformer.injectVariable(
                "ivec2 _vert_tex_light_coord;");
        transformer.injectVariable(
                "vec4 _vert_color;");
        transformer.injectVariable(
                "uint _draw_id;");
        transformer.injectFunction(
                "const uint MATERIAL_USE_MIP_OFFSET = 0u;");

        transformer.injectFunction(
                "vec3 _get_draw_translation(uint pos) {\n" +
                        "    return _get_relative_chunk_coord(pos) * vec3(16.0f);\n" +
                        "}");

        transformer.injectFunction(

                "uvec3 _get_relative_chunk_coord(uint pos) {\n" +
                        "    // Packing scheme is defined by LocalSectionIndex\n" +
                        "    return uvec3(pos) >> uvec3(5u, 0u, 2u) & uvec3(7u, 3u, 7u);\n" +
                        "}");

        transformer.injectFunction(
                "void _vert_init() {" +
                        "_vert_position = (vec3(a_PosId.xyz) * 4.8828125E-4f + -8.0f"
                        + ");" +
                        "_vert_tex_diffuse_coord = (a_TexCoord * " + (1.0f / 32768.0f) + ");" +
                        "_vert_tex_light_coord = a_LightCoord;" +
                        "_vert_color = " + separateAo + ";" +
                        "_draw_id = (a_PosId.w >> 8u) & 0xFFu; }");

        transformer.injectFunction(
                "float _material_mip_bias(uint material) {\n" +
                        "    return ((material >> MATERIAL_USE_MIP_OFFSET) & 1u) != 0u ? 0.0f : -4.0f;\n" +
                        "}");

        addIfNotExists(transformer, "a_PosId", "in uvec4 a_PosId;");
        addIfNotExists(transformer, "a_TexCoord", "in vec2 a_TexCoord;");
        addIfNotExists(transformer, "a_Color", "in vec4 a_Color;");
        addIfNotExists(transformer, "a_LightCoord", "in ivec2 a_LightCoord;");
        transformer.prependMain("_vert_init();");
    }

    private static void addIfNotExists(Transformer transformer, String name, String code) {
        if (!transformer.hasVariable(name)) {
            transformer.injectVariable(code);
        }
    }

    private static void commonPatch(Transformer transformer, EmbeddiumParameters parameters) {
        transformer.rename("gl_FogFragCoord", "iris_FogFragCoord");
        if (parameters.type.glShaderType == ShaderType.VERTEX) {
            transformer.injectVariable("out float iris_FogFragCoord;");
            transformer.prependMain("iris_FogFragCoord = 0.0f;");
        } else if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
            transformer.injectVariable("in float iris_FogFragCoord;");
        }

        if (parameters.type.glShaderType == ShaderType.VERTEX) {
            transformer.injectVariable("vec4 iris_FrontColor;");
            transformer.replaceExpression("gl_FrontColor", "iris_FrontColor");
        }

        if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
            if (transformer.containsCall("gl_FragColor")) {
                transformer.replaceExpression("gl_FragColor", "gl_FragData[0]");
            }

            if (transformer.containsCall("gl_TexCoord")) {
                transformer.rename("gl_TexCoord", "irs_texCoords");
                transformer.injectVariable("in vec4 irs_texCoords[3];");
            }

            if (transformer.containsCall("gl_Color")) {
                transformer.rename("gl_Color", "irs_Color");
                transformer.injectVariable("in vec4 irs_Color;");
            }

            Set<Integer> found = new HashSet<>();
            transformer.renameArray("gl_FragData", "iris_FragData", found);

            for (Integer i : found) {
                transformer.injectFunction("layout (location = " + i + ") out vec4 iris_FragData" + i + ";");
            }

            if (parameters.getAlphaTest() != AlphaTest.ALWAYS && found.contains(0)) {
                transformer.injectVariable("uniform float iris_currentAlphaTest;");
                transformer.appendMain(parameters.getAlphaTest().toExpression("iris_FragData0.a", "iris_currentAlphaTest", ""));
            }

        }

        if (parameters.type.glShaderType == ShaderType.VERTEX || parameters.type.glShaderType == ShaderType.FRAGMENT) {
            upgradeStorageQualifiers(transformer, parameters);
        }

        if (transformer.containsCall("texture") && transformer.hasVariable("texture")) {
            transformer.rename("texture", "gtexture");
        }

        if (transformer.containsCall("gcolor") && transformer.hasVariable("gcolor")) {
            transformer.rename("gcolor", "gtexture");
        }

        transformer.rename("gl_Fog", "iris_Fog");
        transformer.injectVariable("uniform float iris_FogDensity;");
        transformer.injectVariable("uniform float iris_FogStart;");
        transformer.injectVariable("uniform float iris_FogEnd;");
        transformer.injectVariable("uniform vec4 iris_FogColor;");
        transformer.injectFunction("struct iris_FogParameters {vec4 color;float density;float start;float end;float scale;};");
        transformer.injectFunction("iris_FogParameters iris_Fog = iris_FogParameters(iris_FogColor, iris_FogDensity, iris_FogStart, iris_FogEnd, 1.0f / (iris_FogEnd - iris_FogStart));");

        transformer.renameFunctionCall("texture2D", "texture");
        transformer.renameFunctionCall("texture3D", "texture");
        transformer.renameFunctionCall("texture2DLod", "textureLod");
        transformer.renameFunctionCall("texture3DLod", "textureLod");
        transformer.renameFunctionCall("texture2DProj", "textureProj");
        transformer.renameFunctionCall("texture3DProj", "textureProj");
        transformer.renameFunctionCall("texture2DGrad", "textureGrad");
        transformer.renameFunctionCall("texture2DGradARB", "textureGrad");
        transformer.renameFunctionCall("texture3DGrad", "textureGrad");
        transformer.renameFunctionCall("texelFetch2D", "texelFetch");
        transformer.renameFunctionCall("texelFetch3D", "texelFetch");
        transformer.renameFunctionCall("textureSize2D", "textureSize");
        transformer.renameAndWrapShadow("shadow2D", "texture");
        transformer.renameAndWrapShadow("shadow2DLod", "textureLod");
    }

    public static void upgradeStorageQualifiers(Transformer transformer, EmbeddiumParameters parameters) {
        List<TerminalNode> tokens = transformer.collectStorage();

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
