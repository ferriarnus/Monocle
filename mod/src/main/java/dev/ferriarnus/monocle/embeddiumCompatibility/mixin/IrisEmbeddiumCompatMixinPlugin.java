package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import net.irisshaders.iris.platform.IrisPlatformHelpers;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Semi-critical mixin config plugin, disables mixins if Embeddium isn't present,
 * since there are mixins into Iris classes that crash the game instead of just
 * spamming the log if Embeddium isn't present.
 */
public class IrisEmbeddiumCompatMixinPlugin implements IMixinConfigPlugin {
	private boolean validEmbeddiumVersion = false;

	@Override
	public void onLoad(String mixinPackage) {

		validEmbeddiumVersion = IrisPlatformHelpers.getInstance().isModLoaded("embeddium");

		if (!validEmbeddiumVersion) {
			System.err.println("[Monocle] Invalid/missing version of Embeddium detected, disabling compatibility mixins!");
		}

	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return validEmbeddiumVersion;
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

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
