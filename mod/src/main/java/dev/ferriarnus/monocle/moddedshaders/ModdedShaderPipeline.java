package dev.ferriarnus.monocle.moddedshaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.moddedshaders.impl.ProgramDirectivesAccessor;
import dev.ferriarnus.monocle.moddedshaders.impl.ProgramSetExtension;
import dev.ferriarnus.monocle.moddedshaders.impl.WorldRenderingPipelineExtension;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.shader.StandardMacros;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.gl.uniform.UniformType;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ModdedShaderPipeline {

    private static final Pattern UNIFORM_REGEX = Pattern.compile("\"name\":[\\s]*?\"([\\s\\S]*?)\",");
    public static final List<String> UNIFORMS = new ArrayList<>();
    private static final Map<ResourceLocation, ShaderInstance> loadedShaders = new HashMap<>();
    private static final Map<ResourceLocation, Supplier<ShaderInstance>> shaders = new HashMap<>();

    public static void addShader(ResourceLocation name, Supplier<ShaderInstance> shader) {
        shaders.put(name, shader);
    }

    public static ShaderInstance getShader(ResourceLocation name) {
        if (loadedShaders.containsKey(name)) {
            return loadedShaders.get(name);
        } else if (shaders.containsKey(name)) {
            var instance = shaders.get(name).get();
            loadedShaders.put(name, instance);
            return instance;
        }
        return null;
    }

    public static void destroyShaders() {
        loadedShaders.forEach((rl, shader) -> {
            shader.clear();
            shader.close();
        });
        loadedShaders.clear();
    }

    public static void addShaderFromJson(ResourceLocation shader, AlphaTest alpha, VertexFormat format, ProgramId fallback) {
        addShader(shader, () -> {
            try {
                return getShaderFromJson(shader, alpha, format, fallback);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static ShaderInstance getShaderFromJson(ResourceLocation shader, AlphaTest alpha, VertexFormat format, ProgramId fallback) throws IOException {
        if (Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline pipeline && pipeline instanceof WorldRenderingPipelineExtension extension) {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            Reader reader = resourceManager.openAsReader(ResourceLocation.fromNamespaceAndPath(shader.getNamespace(), "shaders/core/" + shader.getPath() + ".json"));
            JsonObject jsonobject = GsonHelper.parse(reader);
            reader.close();
            String vertex = GsonHelper.getAsString(jsonobject, "vertex");
            String fragment = GsonHelper.getAsString(jsonobject, "fragment");

            ResourceLocation vertexRL = ResourceLocation.parse(vertex);
            //vertexRL = ResourceLocation.parse("monocle:mekasuit");
            jsonobject.remove("vertex");
            jsonobject.addProperty("vertex", vertexRL.getPath());
            var vertexReader = resourceManager.openAsReader(ResourceLocation.fromNamespaceAndPath(vertexRL.getNamespace(), "shaders/core/" + vertexRL.getPath() + ".vsh"));
            String vertexSource = IOUtils.toString(vertexReader);
            MojGlslPreprocessor vert = new MojGlslPreprocessor("shaders/core/" + vertexRL.getPath() + ".vsh", resourceManager);
            vertexSource = String.join("", vert.process(vertexSource));
            vertexSource = JcppProcessor.glslPreprocessSource(vertexSource, StandardMacros.createStandardEnvironmentDefines());

            ResourceLocation fragmentRL = ResourceLocation.parse(fragment);
            //fragmentRL = ResourceLocation.parse("monocle:mekasuit");
            jsonobject.remove("fragment");
            jsonobject.addProperty("fragment", fragmentRL.getPath());
            var fragmentReader = resourceManager.openAsReader(ResourceLocation.fromNamespaceAndPath(vertexRL.getNamespace(), "shaders/core/" + fragmentRL.getPath() + ".fsh"));
            String fragmentSource = IOUtils.toString(fragmentReader);
            MojGlslPreprocessor frag = new MojGlslPreprocessor("shaders/core/" + vertexRL.getPath() + ".fsh", resourceManager);
            fragmentSource = String.join("", frag.process(fragmentSource));
            fragmentSource = JcppProcessor.glslPreprocessSource(fragmentSource, StandardMacros.createStandardEnvironmentDefines());

            parseUniforms(GsonHelper.getAsJsonArray(jsonobject, "uniforms", null));

            String json = jsonobject.toString();

//            Matcher matcher = UNIFORM_REGEX.matcher(json);
//            matcher.replaceAll(r -> {
//                UNIFORMS.add(r.group(0));
//                return "\"name\": \"" + r.group(0) + "\",";
//            });

            for (String value : ModdedShaderTransformer.REPLACE) {
                json = json.replaceAll("\"" + value + "\"", "\"iris_" + value + "\"");
            }

            for (String value : UNIFORMS) {
            //    json = json.replaceAll("\"" + value + "\"", "\"iris_" + value + "\"");
            }

            var source = new ProgramSource(shader.getPath(), vertexSource, null, null, null, fragmentSource, extension.getProgramSet(), ((ProgramSetExtension) extension.getProgramSet()).getShaderProperties(), null);
            var opt = extension.getProgramSet().get(fallback);
            if (opt.isPresent()) {
                ((ProgramDirectivesAccessor) source.getDirectives()).setDrawBuffers(opt.get().getDirectives().getDrawBuffers());
            } else {
                opt = extension.getProgramSet().get(fallback.getFallback().get());
                opt.ifPresent(p -> ((ProgramDirectivesAccessor) source.getDirectives()).setDrawBuffers(p.getDirectives().getDrawBuffers()));
            }

            return ModdedShaderCreator.createShader(shader.getPath(), source, alpha, format, json, pipeline);
        }
        return null;
    }

    private static void parseUniforms(JsonArray array) {
        for (var element: array) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(element, "uniform");
            String name = GsonHelper.getAsString(jsonobject, "name");
            if (List.of(ModdedShaderTransformer.REPLACE).contains(name)) {
                continue;
            }
            UNIFORMS.add(name);
            String type = GsonHelper.getAsString(jsonobject, "type");
            type = type + GsonHelper.getAsString(jsonobject, "count");
            jsonobject.remove("name");
            jsonobject.addProperty("name", "iris_" + name);
        }
    }
}
