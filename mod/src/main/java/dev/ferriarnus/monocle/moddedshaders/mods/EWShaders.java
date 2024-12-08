package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class EWShaders {

    public static ResourceLocation PARTICLE = ResourceLocation.fromNamespaceAndPath("enchanted", "particle");

    public static final ParticleRenderType PARTICLE_TRANSLUCENT = new ParticleRenderType() {

        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(() -> ModdedShaderPipeline.getShader(PARTICLE));
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public String toString() {
            return "ENCHANTED_TRANSLUCENT";
        }
    };

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(PARTICLE, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.PARTICLE, false, ProgramId.ParticlesTrans);
    }
}
