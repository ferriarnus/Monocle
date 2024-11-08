package dev.ferriarnus.monocle.moddedshaders;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.Monocle;
import dev.ferriarnus.monocle.moddedshaders.impl.ProgramSetExtension;
import dev.ferriarnus.monocle.moddedshaders.impl.WorldRenderingPipelineExtension;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.ClientHooks;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ModdedShaderPipeline {

    private static final Pattern REGEX_MOJ_IMPORT = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*moj_import(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(?:\"(.*)\"|<(.*)>))");
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

    public static Shader getShaderFromJson(ResourceLocation shader, AlphaTest alpha, VertexFormat format) throws IOException {
        if (Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline pipeline && pipeline instanceof WorldRenderingPipelineExtension extension) {
            Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(ResourceLocation.fromNamespaceAndPath(shader.getNamespace(), "shaders/core/" + shader.getPath() + ".json"));
            String json = IOUtils.toString(reader);
            JsonObject jsonobject = GsonHelper.parse(reader);
            String vertex = GsonHelper.getAsString(jsonobject, "vertex");
            String fragment = GsonHelper.getAsString(jsonobject, "fragment");

            ResourceLocation vertexRL = ResourceLocation.parse(vertex);
            var vertexReader = Minecraft.getInstance().getResourceManager().openAsReader(ResourceLocation.fromNamespaceAndPath(vertexRL.getNamespace(), "shaders/core/" + vertexRL.getPath() + ".vsh"));
            String vertexSource = IOUtils.toString(vertexReader);

            ResourceLocation fragmentRL = ResourceLocation.parse(fragment);
            var fragmentReader = Minecraft.getInstance().getResourceManager().openAsReader(ResourceLocation.fromNamespaceAndPath(vertexRL.getNamespace(), "shaders/core/" + fragmentRL.getPath() + ".fsh"));
            String fragmentSource = IOUtils.toString(fragmentReader);

            var source = new ProgramSource(shader.getPath(), vertexSource, fragmentSource, null, null, null, extension.getProgramSet(), ((ProgramSetExtension) extension.getProgramSet()).getShaderProperties(), null);

            return ModdedShaderCreator.createShader(shader.getPath(), source, alpha, format, json, pipeline);
        }
        return null;
    }
}
