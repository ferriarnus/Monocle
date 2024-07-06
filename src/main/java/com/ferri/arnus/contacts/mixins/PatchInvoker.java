package com.ferri.arnus.contacts.mixins;

import net.irisshaders.iris.pipeline.transform.Patch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Patch.class)
public interface PatchInvoker {

    @Invoker("<init>")
    static Patch createPatch(String name, int ordinal) {
        throw new AssertionError();
    }
}
