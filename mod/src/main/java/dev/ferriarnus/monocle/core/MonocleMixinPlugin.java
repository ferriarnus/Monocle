package dev.ferriarnus.monocle.core;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MonocleMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if(mixinClassName.endsWith("FrustumSwapperMixin")) {
            targetClass.interfaces.removeIf(iface -> iface.startsWith("net/caffeinemc/mods/sodium"));
            // Remap sodium$createViewport to use our classname
            var createViewportMethod = targetClass.methods.stream().filter(m -> m.name.equals("sodium$createViewport")).findFirst();
            createViewportMethod.ifPresent(methodNode -> {
                methodNode.desc = "()Lorg/embeddedt/embeddium/impl/render/viewport/Viewport;";
                methodNode.instructions.forEach(insn -> {
                    if(insn.getOpcode() == Opcodes.NEW) {
                        ((TypeInsnNode)insn).desc = "org/embeddedt/embeddium/impl/render/viewport/Viewport";
                    } else if(insn.getOpcode() == Opcodes.INVOKESPECIAL && insn instanceof MethodInsnNode invoke && invoke.name.equals("<init>")) {
                        invoke.desc = "(Lorg/embeddedt/embeddium/impl/render/viewport/frustum/Frustum;Lorg/joml/Vector3d;)V";
                        invoke.owner = "org/embeddedt/embeddium/impl/render/viewport/Viewport";
                    }
                });
            });
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
