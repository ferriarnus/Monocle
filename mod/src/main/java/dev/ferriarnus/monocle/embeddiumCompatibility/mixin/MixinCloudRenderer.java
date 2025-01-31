package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.CloudVertex;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.embeddedt.embeddium.api.vertex.format.VertexFormatDescription;
import org.embeddedt.embeddium.api.vertex.format.common.ColorVertex;
import org.embeddedt.embeddium.impl.render.immediate.CloudRenderer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CloudRenderer.class)
public abstract class MixinCloudRenderer {
    @Shadow
    private ShaderInstance shader;
    @Shadow
    @Final
    private FogRenderer.FogData fogData;
    @Shadow
    private boolean hasCloudGeometry;
    @Unique
    private VertexBuffer vertexBufferWithNormals;
    @Unique
    private int prevCenterCellXIris, prevCenterCellYIris, cachedRenderDistanceIris;

    @Inject(method = "writeVertex", at = @At("HEAD"), cancellable = true, remap = false)
    private static void writeIrisVertex(long buffer, float x, float y, float z, int color, CallbackInfoReturnable<Long> cir) {
        if (IrisApi.getInstance().isShaderPackInUse()) {
            CloudVertex.write(buffer, x, y, z, color);
            cir.setReturnValue(buffer + 20L);
        }
    }

    @Shadow
    protected abstract void rebuildGeometry(BufferBuilder bufferBuilder, int cloudDistance, int centerCellX, int centerCellZ);

    @Shadow
    protected abstract void applyFogModifiers(ClientLevel world, FogRenderer.FogData fogData, LocalPlayer player, int cloudDistance, float tickDelta);

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private void buildIrisVertexBuffer(ClientLevel world, LocalPlayer player, PoseStack stack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float ticks, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (IrisApi.getInstance().isShaderPackInUse()) {
            ci.cancel();
            renderIris(world, player,modelViewMatrix, projectionMatrix, ticks, tickDelta, cameraX, cameraY, cameraZ);
        }
    }

    public void renderIris(@Nullable ClientLevel world, LocalPlayer player, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float ticks, float tickDelta, double cameraX, double cameraY, double cameraZ) {
        if (world == null) {
            return;
        }

        float cloudHeight = world.effects().getCloudHeight();

        // Vanilla uses NaN height as a way to disable cloud rendering
        if (Float.isNaN(cloudHeight)) {
            return;
        }

        Vec3 color = world.getCloudColor(tickDelta);

        double cloudTime = (ticks + tickDelta) * 0.03F;
        double cloudCenterX = (cameraX + cloudTime);
        double cloudCenterZ = (cameraZ) + 0.33D;

        int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
        int cloudDistance = Math.max(32, (renderDistance * 2) + 9);

        int centerCellX = (int) (Math.floor(cloudCenterX / 12));
        int centerCellZ = (int) (Math.floor(cloudCenterZ / 12));

        if (this.vertexBufferWithNormals == null || this.prevCenterCellXIris != centerCellX || this.prevCenterCellYIris != centerCellZ || this.cachedRenderDistanceIris != renderDistance) {
            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, IrisVertexFormats.CLOUDS);

            // Give some space for shaders
            this.rebuildGeometry(bufferBuilder, cloudDistance + 4, centerCellX, centerCellZ);

            if (this.vertexBufferWithNormals == null) {
                this.vertexBufferWithNormals = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            }

            this.vertexBufferWithNormals.bind();

            MeshData meshData = bufferBuilder.build();

            if(meshData != null) {
                this.vertexBufferWithNormals.upload(meshData);
                this.hasCloudGeometry = true;
            } else {
                this.hasCloudGeometry = false;
            }

            VertexBuffer.unbind();

            this.prevCenterCellXIris = centerCellX;
            this.prevCenterCellYIris = centerCellZ;
            this.cachedRenderDistanceIris = renderDistance;
        }

        // Skip render path if there is no cloud geometry
        if (!this.hasCloudGeometry) {
            return;
        }

        float previousEnd = RenderSystem.getShaderFogEnd();
        float previousStart = RenderSystem.getShaderFogStart();
        fogData.end = cloudDistance * 8;
        fogData.start = (cloudDistance * 8) - 16;

        applyFogModifiers(world, fogData, player, cloudDistance * 8, tickDelta);


        RenderSystem.setShaderFogEnd(fogData.end);
        RenderSystem.setShaderFogStart(fogData.start);

        float translateX = (float) (cloudCenterX - (centerCellX * 12));
        float translateZ = (float) (cloudCenterZ - (centerCellZ * 12));

        RenderSystem.enableDepthTest();

        this.vertexBufferWithNormals.bind();

        boolean insideClouds = cameraY < cloudHeight + 4.5f && cameraY > cloudHeight - 0.5f;

        if (insideClouds) {
            RenderSystem.disableCull();
        } else {
            RenderSystem.enableCull();
        }

        RenderSystem.setShaderColor((float) color.x, (float) color.y, (float) color.z, 0.8f);

        modelViewMatrix = new Matrix4f(modelViewMatrix);
        modelViewMatrix.translate(-translateX, cloudHeight - (float) cameraY + 0.33F, -translateZ);

        // PASS 1: Set up depth buffer
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);

        this.vertexBufferWithNormals.drawWithShader(modelViewMatrix, projectionMatrix, getClouds());

        // PASS 2: Render geometry
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL30C.GL_EQUAL);
        RenderSystem.colorMask(true, true, true, true);

        this.vertexBufferWithNormals.drawWithShader(modelViewMatrix, projectionMatrix, getClouds());

        VertexBuffer.unbind();

        RenderSystem.disableBlend();
        RenderSystem.depthFunc(GL30C.GL_LEQUAL);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.enableCull();

        RenderSystem.setShaderFogEnd(previousEnd);
        RenderSystem.setShaderFogStart(previousStart);
    }

    @ModifyArg(method = "rebuildGeometry", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;nmalloc(I)J"), remap = false)
    private int allocateNewSize(int size) {
        return IrisApi.getInstance().isShaderPackInUse() ? 480 : size;
    }

    @ModifyArg(method = "rebuildGeometry", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/api/vertex/buffer/VertexBufferWriter;push(Lorg/lwjgl/system/MemoryStack;JILorg/embeddedt/embeddium/api/vertex/format/VertexFormatDescription;)V"), index = 3, remap = false)
    private VertexFormatDescription modifyArgIris(VertexFormatDescription vertexFormatDescription) {
        if (IrisApi.getInstance().isShaderPackInUse()) {
            return CloudVertex.FORMAT;
        } else {
            return ColorVertex.FORMAT;
        }
    }

    private ShaderInstance getClouds() {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

        if (pipeline instanceof ShaderRenderingPipeline) {
            return ((ShaderRenderingPipeline) pipeline).getShaderMap().getShader(ShaderKey.CLOUDS_SODIUM);
        }

        return shader;
    }
}
