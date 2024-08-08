package com.ferri.arnus.monocle.irisCompatibility.mixin;

import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(TransformPatcher.class)
public interface TransformPatcherInvoker {

    @Invoker("transform")
    static Map<PatchShaderType, String> transform(String name, String vertex, String geometry, String tessControl, String tessEval, String fragment, Parameters parameters) {
        throw new AssertionError();
    }
}
