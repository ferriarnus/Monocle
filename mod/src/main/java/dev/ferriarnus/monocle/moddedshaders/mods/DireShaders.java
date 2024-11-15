package dev.ferriarnus.monocle.moddedshaders.mods;

import com.direwolf20.justdirethings.JustDireThings;
import com.direwolf20.justdirethings.client.renderers.shader.FixedMultiTextureStateShard;
import com.direwolf20.justdirethings.client.renderers.shader.ShaderTexture;
import com.direwolf20.justdirethings.common.items.interfaces.Ability;
import com.direwolf20.justdirethings.common.items.interfaces.ToggleableTool;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DireShaders {

    public static ResourceLocation PORTAL = ResourceLocation.fromNamespaceAndPath("justdirethings", "portal_entity");

    public static final RenderType PORTAL_TYPE = getPortalType();

    public static RenderType RenderBlockBackface = RenderType.create("GadgetRenderBlockBackface",DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setLightmapState(RenderType.LIGHTMAP).setTextureState(RenderType.BLOCK_SHEET).setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY).setDepthTestState(RenderType.EQUAL_DEPTH_TEST).setCullState(RenderType.NO_CULL).setOverlayState(RenderStateShard.OVERLAY).createCompositeState(false));
    public static RenderType GooPattern = RenderType.create("GooPattern",DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, false,RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_ENTITY_ALPHA_SHADER).setLightmapState(RenderType.LIGHTMAP).setTextureState(RenderType.BLOCK_SHEET).setCullState(RenderType.NO_CULL).setWriteMaskState(RenderStateShard.DEPTH_WRITE).createCompositeState(true));

    static {
        init();
    }

    public static void init() {
        ModdedShaderPipeline.addShaderFromJson(PORTAL, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_TEX, false, ProgramId.Block);
    }

    private static RenderType getPortalType() {
        RenderType.CompositeState compState = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModdedShaderPipeline.getShader(PORTAL)))
                .setTextureState(new FixedMultiTextureStateShard(List.of(
                        new ShaderTexture(ResourceLocation.fromNamespaceAndPath(JustDireThings.MODID, "textures/block/portal_shader.png")))))
                .createCompositeState(false);
        return RenderType.create(PORTAL.getPath(), DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, compState);
    }

    public static boolean nightVision(LivingEntity livingEntity) {
        ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof ToggleableTool toggleableTool && toggleableTool.canUseAbilityAndDurability(helmet, Ability.NIGHTVISION)) {
            return true;
        }
        return false;
    }
}
