package dev.ferriarnus.monocle.moddedshaders.mods;

import com.direwolf20.justdirethings.JustDireThings;
import com.direwolf20.justdirethings.client.renderers.shader.FixedMultiTextureStateShard;
import com.direwolf20.justdirethings.client.renderers.shader.ShaderTexture;
import com.direwolf20.justdirethings.common.items.interfaces.Ability;
import com.direwolf20.justdirethings.common.items.interfaces.ToggleableTool;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.opengl.GL32;

import java.util.List;

public class DireShaders {

    public static final RenderTarget GOO_TARGET = new TextureTarget(1, 1, true, Minecraft.ON_OSX);

    public static ResourceLocation PORTAL = ResourceLocation.fromNamespaceAndPath("justdirethings", "portal_entity");

    public static final RenderType PORTAL_TYPE = getPortalType();

    public static RenderType RenderBlockBackface = RenderType.create("GadgetRenderBlockBackface", DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,  256,  false,  false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> GameRenderer.rendertypeTranslucentShader))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setTextureState(RenderType.BLOCK_SHEET)
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderType.EQUAL_DEPTH_TEST)
                    .setCullState(RenderType.CULL)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(false));

    public static RenderType GooPattern = RenderType.create("GooPattern", DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,  256,  false,  false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> GameRenderer.rendertypeEntityAlphaShader))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setTextureState(RenderType.BLOCK_SHEET)
                    .setCullState(RenderType.NO_CULL)
                    .setWriteMaskState(RenderStateShard.DEPTH_WRITE)
                    .createCompositeState(true));

    static {
        init();
        NeoForge.EVENT_BUS.addListener(DireShaders::renderLevel);
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
        return helmet.getItem() instanceof ToggleableTool toggleableTool && toggleableTool.canUseAbilityAndDurability(helmet, Ability.NIGHTVISION);
    }

    public static void setupRenderTarget() {
        Window window = Minecraft.getInstance().getWindow();
        GOO_TARGET.setClearColor(0,0,0,0);
        if (GOO_TARGET.height != window.getHeight() || GOO_TARGET.width != window.getWidth()) {
            GOO_TARGET.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
        } else {
            GOO_TARGET.clear(Minecraft.ON_OSX);
        }
    }

    public static void renderLevel(RenderLevelStageEvent event) {
        if (!Iris.isPackInUseQuick()) {
            return;
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            setupRenderTarget();
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {

            RenderSystem.setShader(MinecraftShaders::getScreenShader);
            RenderSystem.setShaderTexture(0, GOO_TARGET.getColorTextureId());
            MinecraftShaders.setDepthTexture(GOO_TARGET.getDepthTextureId()); //Iris

            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL32.GL_LESS);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.addVertex( -1, -1, 0).setUv(0, 0);
            bufferbuilder.addVertex( 1, -1, 0).setUv(1, 0);
            bufferbuilder.addVertex( 1, 1, 0).setUv(1, 1);
            bufferbuilder.addVertex( -1, 1, 0).setUv(0, 1);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
        }
    }
}
