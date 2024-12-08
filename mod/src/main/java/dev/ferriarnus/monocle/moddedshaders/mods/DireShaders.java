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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL32;

import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class DireShaders {

    public static RenderTarget GOO_TARGET;

    public static ResourceLocation PORTAL = ResourceLocation.fromNamespaceAndPath("justdirethings", "portal_entity");

    public static final RenderType PORTAL_TYPE = getPortalType();

    public static final RenderStateShard.ShaderStateShard RENDERTYPE_TRANSLUCENT_SHADER = new RenderStateShard.ShaderStateShard(
            () -> GameRenderer.rendertypeTranslucentShader) {
        @Override
        public void setupRenderState() {
            GOO_TARGET.bindWrite(true);
            super.setupRenderState();
        }

        @Override
        public void clearRenderState() {
            super.clearRenderState();
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        }
    };

    public static RenderType RenderBlockBackface = RenderType.create("GadgetRenderBlockBackface",DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setTextureState(RenderType.BLOCK_SHEET)
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderType.EQUAL_DEPTH_TEST)
                    .setCullState(RenderType.CULL)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(false));

    public static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_ALPHA_SHADER = new RenderStateShard.ShaderStateShard(
            () -> GameRenderer.rendertypeEntityAlphaShader) {
        @Override
        public void setupRenderState() {
            GOO_TARGET.bindWrite(true);
            super.setupRenderState();
        }

        @Override
        public void clearRenderState() {
            super.clearRenderState();
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        }
    };

    public static RenderType GooPattern = RenderType.create("GooPattern",DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_ALPHA_SHADER)
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setTextureState(RenderType.BLOCK_SHEET)
                    .setCullState(RenderType.NO_CULL)
                    .setWriteMaskState(RenderStateShard.DEPTH_WRITE)
                    .createCompositeState(true));

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

    public static void setupRenderTarget() {
        Window window = Minecraft.getInstance().getWindow();
        if(GOO_TARGET != null){
            GOO_TARGET.destroyBuffers();
        }
        GOO_TARGET = new TextureTarget(window.getWidth(), window.getHeight(), true, Minecraft.ON_OSX);
    }

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            setupRenderTarget();
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            RenderSystem.setShader(MinecraftShaders::getScreenShader);
            RenderSystem.setShaderTexture(0, GOO_TARGET.getColorTextureId());
            RenderSystem.setShaderTexture(3, GOO_TARGET.getDepthTextureId()); //Vanilla
            MinecraftShaders.setDepthTexture(GOO_TARGET.getDepthTextureId()); //Iris

            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL32.GL_LESS);
            RenderSystem.enableBlend();

            BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.addVertex( -1, -1, 0).setUv(0, 0);
            bufferbuilder.addVertex( 1, -1, 0).setUv(1, 0);
            bufferbuilder.addVertex( 1, 1, 0).setUv(1, 1);
            bufferbuilder.addVertex( -1, 1, 0).setUv(0, 1);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();

            GOO_TARGET.blitToScreen(Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
        }
    }
}
