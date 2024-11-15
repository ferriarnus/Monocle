package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.function.Function;

public class SuppShaders {
    private static final Int2ObjectArrayMap<RenderType> TYPES = new Int2ObjectArrayMap<>();

    private static ResourceLocation ENTITY_CUTOFF = ResourceLocation.fromNamespaceAndPath("supplementaries", "entity_cutout_texture_offset");
    public static ResourceLocation STATIC = ResourceLocation.fromNamespaceAndPath("supplementaries", "static_noise");
    public static final ResourceLocation SLIME_ENTITY_OVERLAY = ResourceLocation.fromNamespaceAndPath("supplementaries", "textures/entity/slime_overlay.png");

    public static final Function<ResourceLocation, RenderType> STATIC_NOISE = Util.memoize((resourceLocation) -> {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModdedShaderPipeline.getShader(STATIC)))
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(RenderType.NO_TRANSPARENCY)
                .setLightmapState(RenderType.LIGHTMAP)
                .setOverlayState(RenderType.OVERLAY)
                .createCompositeState(true);
        return RenderType.create("static_noise", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, false, compositeState);
    });

    static {
        init();
    }

    public static void init() {
        ModdedShaderPipeline.addShaderFromJson(ENTITY_CUTOFF, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY, false, ProgramId.Entities);
        ModdedShaderPipeline.addShaderFromJson(STATIC, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY, false, ProgramId.Entities);
    }

    public static RenderType get(int width, int height) {
        return TYPES.computeIfAbsent((width << 16) | (height & 0xFFFF), k -> RenderType.create("slimed",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false, true,
                RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(SLIME_ENTITY_OVERLAY, false, false))
                        .setCullState(RenderType.NO_CULL)
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModdedShaderPipeline.getShader(ENTITY_CUTOFF)))
                        .setOverlayState(RenderType.OVERLAY)
                        .setLightmapState(RenderType.LIGHTMAP)
                        .setDepthTestState(RenderType.EQUAL_DEPTH_TEST)
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setTexturingState(new OffsetTexturing(width, height))
                        .createCompositeState(false)
        ));
    }

    private static class OffsetTexturing extends RenderStateShard.TexturingStateShard {
        public OffsetTexturing(int width, int height) {
            super("slime_offset_texturing",
                    () -> {
                        float u = (float) (System.currentTimeMillis() % 400000L) / 400000.0F;
                        Matrix4f translation = new Matrix4f().translation(0, -u, 0.0F);
                        float x = (float) width / 64;
                        float y = (float) height / 64;
                        translation.scale(x, y, 1);
                        RenderSystem.setTextureMatrix(translation);
                    },
                    RenderSystem::resetTextureMatrix);
        }
    }
}
