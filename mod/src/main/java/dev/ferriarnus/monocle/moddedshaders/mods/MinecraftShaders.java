package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.Monocle;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = Monocle.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class MinecraftShaders {

    public static final boolean JUSTDIRETHINGS = LoadingModList.get().getModFileById("justdirethings") != null;

    private static int DEPTH_ID;

    public static ResourceLocation END_PORTAL = ResourceLocation.parse("rendertype_end_portal");
    public static ResourceLocation END_GATEWAY = ResourceLocation.parse("rendertype_end_gateway");
    public static ResourceLocation POSITION_COLOR_LIGHTMAP = ResourceLocation.parse("position_color_lightmap");
    public static ResourceLocation SCREEN = ResourceLocation.fromNamespaceAndPath(Monocle.MODID, "screen");

    private static ShaderInstance SCREEN_SHADER;

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(END_PORTAL, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION, false, ProgramId.Entities);
        ModdedShaderPipeline.addShaderFromJson(END_GATEWAY, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION, false, ProgramId.Entities);
        ModdedShaderPipeline.addShaderFromJson(POSITION_COLOR_LIGHTMAP, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, false, ProgramId.Entities);
        ModdedShaderPipeline.addShaderFromJson(SCREEN, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_TEX, false, ProgramId.BlockTrans);
    }

    public static ShaderInstance getScreenShader() {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(SCREEN);
        }
        return SCREEN_SHADER;
    }

    public static boolean needsDepth() {
        return JUSTDIRETHINGS;
    }

    @SubscribeEvent
    public static void registerShader(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), SCREEN,
                DefaultVertexFormat.POSITION_TEX), s -> SCREEN_SHADER = s );
    }

    public static void setDepthTexture(int depthTextureId) {
        DEPTH_ID = depthTextureId;
    }

    public static int getDepthId() {
        return DEPTH_ID;
    }
}
