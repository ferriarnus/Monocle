package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.mods.DireShaders;
import dev.ferriarnus.monocle.moddedshaders.mods.MinecraftShaders;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommonUniforms.class)
public class MixinCommonUniforms {

    @WrapOperation(method = "getNightVision", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getNightVisionScale(Lnet/minecraft/world/entity/LivingEntity;F)F"))
    private static float wrapNightVision(LivingEntity livingEntity, float nanoTime, Operation<Float> original) {
        if (MinecraftShaders.JUSTDIRETHINGS) {
            if (DireShaders.nightVision(livingEntity)) {
                return 1.0f;
            }
        }
        return original.call(livingEntity, nanoTime);
    }
}
