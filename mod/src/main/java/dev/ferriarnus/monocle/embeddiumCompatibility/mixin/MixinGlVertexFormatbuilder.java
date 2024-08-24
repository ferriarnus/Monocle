package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.embeddedt.embeddium.impl.gl.attribute.GlVertexAttribute;
import org.embeddedt.embeddium.impl.gl.attribute.GlVertexAttributeFormat;
import org.embeddedt.embeddium.impl.gl.attribute.GlVertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(	GlVertexFormat.Builder.class)
public class MixinGlVertexFormatbuilder {

	GlVertexAttribute dummy = new GlVertexAttribute(GlVertexAttributeFormat.UNSIGNED_BYTE, 0,false, 0, 0, false);

	@ModifyVariable(method = "build", at = @At(value = "LOAD", target = "Ljava/util/EnumMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private GlVertexAttribute putDummy(GlVertexAttribute value) {
		if (value == null) {
			return dummy;
		}
		return value;
	}

	@WrapOperation(method = "build", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"))
	private int putDummy(int a, int b, Operation<Integer> original, @Local GlVertexAttribute attribute) {
		if (attribute == dummy) {
			return a;
		}
		return original.call(a,b);
	}
}
