package dev.ferriarnus.monocle.embeddiumCompatibility.mixin.copyEntity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.embeddedt.embeddium.impl.model.ModelCuboidAccessor;
import org.embeddedt.embeddium.impl.render.immediate.model.ModelCuboid;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ModelPart.Cube.class)
public class CuboidMixin implements ModelCuboidAccessor {
	@Unique
	private ModelCuboid embeddium$cuboid;

	// Inject at the start of the function, so we don't capture modified locals
	@Inject(method = "<init>", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/model/geom/ModelPart$Cube;polygons:[Lnet/minecraft/client/model/geom/ModelPart$Polygon;", ordinal = 0))
	private void onInit(int u, int v, float x, float y, float z, float sizeX, float sizeY, float sizeZ, float extraX, float extraY, float extraZ, boolean mirror, float textureWidth, float textureHeight, Set<Direction> renderDirections, CallbackInfo ci) {
		this.embeddium$cuboid = new ModelCuboid(u, v, x, y, z, sizeX, sizeY, sizeZ, extraX, extraY, extraZ, mirror, textureWidth, textureHeight, renderDirections);
	}

	@Override
	public ModelCuboid sodium$copy() {
		return this.embeddium$cuboid;
	}

	@Override
	public @Nullable ModelCuboid embeddium$getSimpleCuboid() {
		return embeddium$cuboid;
	}
}
