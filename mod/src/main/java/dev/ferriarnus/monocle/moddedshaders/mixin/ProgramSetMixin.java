package dev.ferriarnus.monocle.moddedshaders.mixin;

import dev.ferriarnus.monocle.moddedshaders.impl.ProgramSetExtension;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(ProgramSet.class)
public class ProgramSetMixin implements ProgramSetExtension {

    private ShaderProperties shaderProperties;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void capture(AbsolutePackPath directory, Function sourceProvider, ShaderProperties shaderProperties, ShaderPack pack, CallbackInfo ci) {
        this.shaderProperties = shaderProperties;
    }

    @Unique
    public ShaderProperties getShaderProperties() {
        return shaderProperties;
    }
}
