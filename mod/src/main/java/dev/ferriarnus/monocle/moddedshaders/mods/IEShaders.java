package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.resources.ResourceLocation;

public class IEShaders {

    public static ResourceLocation FULLBRIGHT = ResourceLocation.fromNamespaceAndPath("immersiveengineering", "block_fullbright");
    public static ResourceLocation POINT = ResourceLocation.fromNamespaceAndPath("immersiveengineering", "rendertype_point");
    public static ResourceLocation VBO = ResourceLocation.fromNamespaceAndPath("immersiveengineering", "rendertype_vbo");

    private static VertexFormat BUFFER_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV0)
            .add("Normal", VertexFormatElement.NORMAL)
            .padding(1)
            .build();

    static {
        init();
    }

    public static void init() {
        ModdedShaderPipeline.addShaderFromJson(FULLBRIGHT, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.BLOCK, true, ProgramId.Block);
        ModdedShaderPipeline.addShaderFromJson(POINT, AlphaTests.ONE_TENTH_ALPHA, BUFFER_FORMAT, false, ProgramId.Block);
        ModdedShaderPipeline.addShaderFromJson(VBO, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_COLOR_NORMAL, false, ProgramId.Block);

    }
}
