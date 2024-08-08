package com.ferri.arnus.monocle.irisCompatibility.mixin;

import com.ferri.arnus.monocle.Monocle;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Inject(method = "getGameInformation", at = @At("RETURN"))
    private void injectMonocleInfo(CallbackInfoReturnable<List<String>> cir) {
        String version = ModList.get().getModContainerById(Monocle.MODID).orElseThrow().getModInfo().getVersion().toString();
        cir.getReturnValue().add("");
        cir.getReturnValue().add("[Monocle] v" + version);
    }
}
