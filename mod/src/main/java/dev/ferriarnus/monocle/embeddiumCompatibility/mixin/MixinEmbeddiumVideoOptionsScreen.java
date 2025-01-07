package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import dev.ferriarnus.monocle.Monocle;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import org.embeddedt.embeddium.impl.Embeddium;
import org.embeddedt.embeddium.impl.gui.EmbeddiumVideoOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(EmbeddiumVideoOptionsScreen.class)
public class MixinEmbeddiumVideoOptionsScreen {

    private boolean compactFormat = Embeddium.options().performance.useCompactVertexFormat;

    @Inject(method = "applyChanges", at = @At("TAIL"))
    private void injectReload(CallbackInfo ci) {
        if (compactFormat != Embeddium.options().performance.useCompactVertexFormat) {
            compactFormat = !compactFormat;
            try {
                WorldRenderingSettings.INSTANCE.setVertexFormat((ChunkVertexType) Monocle.getVertexFormat());
                Iris.reload();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
