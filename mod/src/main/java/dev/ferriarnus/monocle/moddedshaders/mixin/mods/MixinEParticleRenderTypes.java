package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.mods.EWShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/favouriteless/enchanted/client/EParticleRenderTypes", remap = false)
public class MixinEParticleRenderTypes {

    @WrapOperation(method = "translucentParticle", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticleRenderType;PARTICLE_SHEET_TRANSLUCENT:Lnet/minecraft/client/particle/ParticleRenderType;"))
    private static ParticleRenderType removeIrisCheck(Operation<ParticleRenderType> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return EWShaders.PARTICLE_TRANSLUCENT;
        }
        return original.call();
    }
}
