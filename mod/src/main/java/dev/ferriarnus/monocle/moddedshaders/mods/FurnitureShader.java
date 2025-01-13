package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.opengl.GL32;

public class FurnitureShader {

    public static final RenderTarget LIGHTNING = new TextureTarget(1,1, true, Minecraft.ON_OSX);

    static {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, FurnitureShader::renderLevel);
    }

    public static RenderTarget getLightning() {
        Window window = Minecraft.getInstance().getWindow();
        if (LIGHTNING.height != window.getHeight() || LIGHTNING.width != window.getWidth()) {
            LIGHTNING.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
        } else {
            LIGHTNING.clear(true);
        }
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        return LIGHTNING;
    }

    public static void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {

            RenderSystem.setShader(MinecraftShaders::getScreenShader);
            RenderSystem.setShaderTexture(0, LIGHTNING.getColorTextureId());

            RenderSystem.disableDepthTest();

            BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.addVertex( -1, -1, 0).setUv(0, 0);
            bufferbuilder.addVertex( 1, -1, 0).setUv(1, 0);
            bufferbuilder.addVertex( 1, 1, 0).setUv(1, 1);
            bufferbuilder.addVertex( -1, 1, 0).setUv(0, 1);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        }
    }
}
