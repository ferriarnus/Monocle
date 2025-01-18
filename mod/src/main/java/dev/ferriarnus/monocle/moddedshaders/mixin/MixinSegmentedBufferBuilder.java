package dev.ferriarnus.monocle.moddedshaders.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.batchedentityrendering.impl.BufferSegment;
import net.irisshaders.batchedentityrendering.impl.ByteBufferBuilderHolder;
import net.irisshaders.batchedentityrendering.impl.RenderTypeUtil;
import net.irisshaders.batchedentityrendering.impl.SegmentedBufferBuilder;
import net.irisshaders.batchedentityrendering.mixin.RenderTypeAccessor;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(SegmentedBufferBuilder.class)
public abstract class MixinSegmentedBufferBuilder {

    @Final
    @Shadow
    private Map<RenderType, ByteBufferBuilderHolder> buffers;

    @Final
    @Shadow
    private Map<RenderType, BufferBuilder> builders;

    @Final
    @Shadow
    private List<BufferSegment> segments;

    @Inject(method = "getBuffer", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", ordinal = 0, shift = At.Shift.BEFORE))
    public void injectLine(RenderType renderType, CallbackInfoReturnable<VertexConsumer> cir) {
        if (requiresSegmentSplits(renderType)) {
            monocle$endAndGenSegmentForType(renderType);
        }
    }


    @Unique
    private void monocle$endAndGenSegmentForType(RenderType renderType) {
        var bufferBuilder = builders.remove(renderType);

        if (bufferBuilder == null) {
            return;
        }

        try {
            MeshData meshData = bufferBuilder.build();

            if (meshData == null) return;

            if (shouldSortOnUpload(renderType)) {
                meshData.sortQuads(buffers.get(renderType).getBuffer(), RenderSystem.getVertexSorting());
            }

            segments.add(new BufferSegment(meshData, renderType));
        } catch (OutOfMemoryError e) {
            // we're freaked. try to clear memory for the next one, but don't bother about this one.

            weAreOutOfMemory();
        }
    }

    @Unique
    private static boolean requiresSegmentSplits(RenderType renderType) {
        var mode = renderType.mode();
        return mode == VertexFormat.Mode.TRIANGLE_FAN || mode == VertexFormat.Mode.DEBUG_LINE_STRIP || mode == VertexFormat.Mode.LINE_STRIP;
    }

    @Shadow
    private void weAreOutOfMemory() {
        throw new UnsupportedOperationException();
    }

    @Shadow
    private static boolean shouldSortOnUpload(RenderType type) {
        throw new UnsupportedOperationException();
    }

}
