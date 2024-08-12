package dev.ferriarnus.monocle.embeddiumCompatibility.mixin.block_id;

import dev.ferriarnus.monocle.embeddiumCompatibility.impl.block_context.ChunkBuildBuffersExt;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.shaderpack.materialmap.BlockMaterialMapping;
import net.irisshaders.iris.shaderpack.materialmap.BlockRenderType;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.embeddedt.embeddium.impl.model.quad.properties.ModelQuadFacing;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildBuffers;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildOutput;
import org.embeddedt.embeddium.impl.render.chunk.compile.buffers.ChunkModelBuilder;
import org.embeddedt.embeddium.impl.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.DefaultMaterials;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;
import org.embeddedt.embeddium.impl.util.task.CancellationToken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Passes additional information indirectly to the vertex writer to support the mc_Entity and at_midBlock parts of the vertex format.
 */
@Mixin(ChunkBuilderMeshingTask.class)
public class MixinChunkRenderRebuildTask {
	private final ChunkVertexEncoder.Vertex[] vertices = ChunkVertexEncoder.Vertex.uninitializedQuad();

	@Inject(method = "execute(Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildContext;Lorg/embeddedt/embeddium/impl/util/task/CancellationToken;)Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE",
		target = "net/minecraft/world/level/block/state/BlockState.getRenderShape()" +
			"Lnet/minecraft/world/level/block/RenderShape;"))
	private void iris$setLocalPos(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local(ordinal = 0) BlockPos.MutableBlockPos relBlockPos, @Local(ordinal = 0) BlockState blockState) {
		ChunkBuildBuffers buffers = buildContext.buffers;

		int relX = relBlockPos.getX();
		int relY = relBlockPos.getY();
		int relZ = relBlockPos.getZ();

		if (WorldRenderingSettings.INSTANCE.shouldVoxelizeLightBlocks() && blockState.getBlock() instanceof LightBlock) {
			ChunkModelBuilder buildBuffers = buffers.get(DefaultMaterials.CUTOUT);
			((ChunkBuildBuffersExt) buffers).iris$setLocalPos(0, 0, 0);
			((ChunkBuildBuffersExt) buffers).iris$ignoreMidBlock(true);
			((ChunkBuildBuffersExt) buffers).iris$setMaterialId(blockState, (short) 0, (byte) blockState.getLightEmission());
			for (int i = 0; i < 4; i++) {
				vertices[i].x = (float) ((relX & 15)) + 0.25f;
				vertices[i].y = (float) ((relY & 15)) + 0.25f;
				vertices[i].z = (float) ((relZ & 15)) + 0.25f;
				vertices[i].u = 0;
				vertices[i].v = 0;
				vertices[i].color = 0;
				vertices[i].light = blockState.getLightEmission() << 4 | blockState.getLightEmission() << 20;
			}
			buildBuffers.getVertexBuffer(ModelQuadFacing.UNASSIGNED).push(vertices, DefaultMaterials.CUTOUT);
			((ChunkBuildBuffersExt) buffers).iris$ignoreMidBlock(false);
			return;
		}

		if (buildContext.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buildContext.buffers).iris$setLocalPos(relX, relY, relZ);
		}
	}

	@Inject(method = "execute(Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildContext;Lorg/embeddedt/embeddium/impl/util/task/CancellationToken;)Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/BakedModel;"))
	private void iris$wrapGetBlockLayer(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local(ordinal = 0) BlockState blockState) {
		if (buildContext.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buildContext.buffers).iris$setMaterialId(blockState, ExtendedDataHelper.BLOCK_RENDER_TYPE, (byte) blockState.getLightEmission());
		}
	}

	@Inject(method = "execute(Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildContext;Lorg/embeddedt/embeddium/impl/util/task/CancellationToken;)Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE",
		target = "Lorg/embeddedt/embeddium/impl/render/chunk/compile/pipeline/FluidRenderer;render(Lorg/embeddedt/embeddium/impl/world/WorldSlice;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildBuffers;)V"))
	private void iris$wrapGetFluidLayer(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local(ordinal = 0) BlockState blockState, @Local(ordinal = 0) FluidState fluidState) {
		if (buildContext.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buildContext.buffers).iris$setMaterialId(fluidState.createLegacyBlock(), ExtendedDataHelper.FLUID_RENDER_TYPE, (byte) blockState.getLightEmission());
		}
	}

	@Inject(method = "execute(Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildContext;Lorg/embeddedt/embeddium/impl/util/task/CancellationToken;)Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildOutput;",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"))
	private void iris$resetContext(ChunkBuildContext buildContext, CancellationToken cancellationSource, CallbackInfoReturnable<ChunkBuildOutput> cir) {
		if (buildContext.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buildContext.buffers).iris$resetBlockContext();
		}
	}

	private static final ChunkRenderTypeSet[] IRIS_TYPE_TO_FORGE_SET;

	static {
		var values = BlockRenderType.values();
		IRIS_TYPE_TO_FORGE_SET = new ChunkRenderTypeSet[values.length];
		for(int i = 0; i < values.length; i++) {
			IRIS_TYPE_TO_FORGE_SET[i] = ChunkRenderTypeSet.of(BlockMaterialMapping.convertBlockToRenderType(values[i]));
		}
	}

	private ChunkRenderTypeSet getOverridenRenderType(BlockState state) {
		var idMap = WorldRenderingSettings.INSTANCE.getBlockTypeIds();
		if (idMap != null) {
			BlockRenderType type = idMap.get(state.getBlock());
			if (type != null) {
				return IRIS_TYPE_TO_FORGE_SET[type.ordinal()];
			}
		}
		return null;
	}

	/**
	 * @author embeddedt
	 * @reason Use Iris' block render type overrides if present
	 */
	@WrapOperation(method = "execute(Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildContext;Lorg/embeddedt/embeddium/impl/util/task/CancellationToken;)Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildOutput;",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getRenderTypes(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;)Lnet/neoforged/neoforge/client/ChunkRenderTypeSet;"))
	private ChunkRenderTypeSet monocle$replaceRenderType(BakedModel model, BlockState state, RandomSource random, ModelData data, Operation<ChunkRenderTypeSet> original) {
		var type = getOverridenRenderType(state);
		if (type != null) {
			return type;
		} else {
			return original.call(model, state, random, data);
		}
	}
}
