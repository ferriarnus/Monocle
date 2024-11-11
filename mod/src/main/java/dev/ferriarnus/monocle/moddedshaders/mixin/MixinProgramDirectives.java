package dev.ferriarnus.monocle.moddedshaders.mixin;

import dev.ferriarnus.monocle.moddedshaders.impl.ProgramDirectivesAccessor;
import net.irisshaders.iris.shaderpack.properties.ProgramDirectives;
import org.spongepowered.asm.mixin.*;

@Mixin(ProgramDirectives.class)
public class MixinProgramDirectives implements ProgramDirectivesAccessor {

    @Final
    @Mutable
    @Shadow
    private int[] drawBuffers;

    @Unique
    public void setDrawBuffers(int[] drawBuffers) {
        this.drawBuffers = drawBuffers;
    }
}
