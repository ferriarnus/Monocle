package dev.ferriarnus.monocle.irisCompatibility.mixin;

import net.minecraft.CrashReport;
import net.neoforged.neoforge.logging.CrashReportExtender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReportExtender.class)
public class CrashReportExtenderMixin {
    @Inject(method = "addCrashReportHeader", at = @At("HEAD"), remap = false)
    private static void injectMonocleHeader(StringBuilder builder, CrashReport crashReport, CallbackInfo ci) {
        builder.append("// Monocle is installed, do not ask Iris for support unless you are 100% sure your issue is not caused by Monocle or Embeddium.\n");
    }
}
