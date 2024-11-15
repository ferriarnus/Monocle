package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class BumbleShaders {

    public static ResourceLocation ESSENCE = ResourceLocation.fromNamespaceAndPath("the_bumblezone","rendertype_bumblezone_essence");

    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath("the_bumblezone", "textures/block/essence/base_background.png");
    private static final ResourceLocation BEE_TEXTURE = ResourceLocation.fromNamespaceAndPath("the_bumblezone", "textures/block/essence/bee_icon_background.png");

    static {
        init();
    }

    public static RenderType ESSENCE_TYPE = RenderType.create("bumblezone_essence_block", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModdedShaderPipeline.getShader(ESSENCE)))
            .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
									.add(BASE_TEXTURE, false, false)
									.add(BEE_TEXTURE, false, false)
									.build())
            .createCompositeState(false)
			);

    public static void init() {
        ModdedShaderPipeline.addShaderFromJson(ESSENCE, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_COLOR_NORMAL, false, ProgramId.Block);
    }
}
