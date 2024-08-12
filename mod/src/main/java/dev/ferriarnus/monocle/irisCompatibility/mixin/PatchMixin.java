package dev.ferriarnus.monocle.irisCompatibility.mixin;

import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumPatch;
import net.irisshaders.iris.pipeline.transform.Patch;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Patch.class)
public class PatchMixin {

    @SuppressWarnings("target")
    @Shadow
    @Final
    @Mutable
    private static Patch[] $VALUES;

    static {
        int baseOrdinal = $VALUES.length;

        EmbeddiumPatch.EMBEDDIUM = PatchInvoker.createPatch("embeddium", baseOrdinal);

        $VALUES = ArrayUtils.addAll($VALUES, EmbeddiumPatch.EMBEDDIUM);
    }
}
