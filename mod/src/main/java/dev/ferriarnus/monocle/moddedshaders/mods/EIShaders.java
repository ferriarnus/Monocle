package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.resources.ResourceLocation;

public class EIShaders {
    public static final ResourceLocation ARC = ResourceLocation.fromNamespaceAndPath("extended_industrialization", "tesla_arc");
    public static final ResourceLocation PLASMA = ResourceLocation.fromNamespaceAndPath("extended_industrialization", "tesla_plasma");
    public static final ResourceLocation QUANTUM = ResourceLocation.fromNamespaceAndPath("extended_industrialization", "nano_quantum");
    public static final ResourceLocation ARMOR_CUTOUT = ResourceLocation.fromNamespaceAndPath("extended_industrialization", "armor_cutout_glow");

    private static final VertexFormat NANO_QUANTUM_VERTEX_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("UV0", VertexFormatElement.UV0)
            .add("Color", VertexFormatElement.COLOR)
            .build();

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(ARC, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_TEX, false, ProgramId.EntitiesTrans);
        ModdedShaderPipeline.addShaderFromJson(PLASMA, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_TEX, false, ProgramId.EntitiesTrans);
        ModdedShaderPipeline.addShaderFromJson(QUANTUM, AlphaTests.NON_ZERO_ALPHA, NANO_QUANTUM_VERTEX_FORMAT, false, ProgramId.EntitiesTrans);
        ModdedShaderPipeline.addShaderFromJson(ARMOR_CUTOUT, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.NEW_ENTITY, false, ProgramId.Entities);
    }

}
