package dev.ferriarnus.monocle.irisCompatibility.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumCoreTransformer;
import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumParameters;
import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumPatch;
import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumTransformer;
import io.github.douira.glsl_transformer.ast.node.Profile;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.Version;
import io.github.douira.glsl_transformer.ast.node.VersionStatement;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.EnumASTTransformer;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;
import net.irisshaders.iris.pipeline.transform.parameter.SodiumParameters;
import net.irisshaders.iris.pipeline.transform.parameter.VanillaParameters;
import net.irisshaders.iris.pipeline.transform.transformer.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Objects;

@Mixin(TransformPatcher.class)
public class TransformPatcherMixin {

    @Shadow
    @Final
    private static EnumASTTransformer<Parameters, PatchShaderType> transformer;

    @ModifyArg(method = "lambda$static$2", at = @At(value = "INVOKE", target = "Lio/github/douira/glsl_transformer/ast/query/Root;indexBuildSession(Ljava/lang/Runnable;)V"))
    private static Runnable changePatch(Runnable session, @Local Parameters parameters, @Local Root root, @Local TranslationUnit tree) {
        return () -> {
            VersionStatement versionStatement = tree.getVersionStatement();
            if (versionStatement == null) {
                throw new IllegalStateException("Missing the version statement!");
            }
            Profile profile = versionStatement.profile;
            Version version = versionStatement.version;
            if (Objects.requireNonNull(parameters.patch) == Patch.COMPUTE) {// we can assume the version is at least 400 because it's a compute shader
                versionStatement.profile = Profile.CORE;
                CommonTransformer.transform(transformer, tree, root, parameters, true);
            } else {// handling of Optifine's special core profile mode
                boolean isLine = (parameters.patch == Patch.VANILLA && ((VanillaParameters) parameters).isLines());

                if (profile == Profile.CORE || version.number >= 150 && profile == null || isLine) {
                    // patch the version number to at least 330
                    if (version.number < 330) {
                        versionStatement.version = Version.GLSL33;
                    }

                    switch (parameters.patch) {
                        case COMPOSITE:
                            CompositeCoreTransformer.transform(transformer, tree, root, parameters);
                            break;
                        case SODIUM:
                            SodiumParameters sodiumParameters = (SodiumParameters) parameters;
                            SodiumCoreTransformer.transform(transformer, tree, root, sodiumParameters);
                            break;
                        case VANILLA:
                            VanillaCoreTransformer.transform(transformer, tree, root, (VanillaParameters) parameters);
                            break;
                        default:
                            if (parameters.patch == EmbeddiumPatch.EMBEDDIUM) {
                                EmbeddiumParameters embeddiumParameters = (EmbeddiumParameters) parameters;
                                EmbeddiumCoreTransformer.transform(transformer, tree, root, embeddiumParameters);
                            } else {
                                throw new UnsupportedOperationException("Unknown patch type: " + parameters.patch);
                            }
                    }

                    if (parameters.type == PatchShaderType.FRAGMENT) {
                        CompatibilityTransformer.transformFragmentCore(transformer, tree, root, parameters);
                    }
                } else {
                    // patch the version number to at least 330
                    if (version.number < 330) {
                        versionStatement.version = Version.GLSL33;
                    }
                    versionStatement.profile = Profile.CORE;

                    switch (parameters.patch) {
                        case COMPOSITE:
                            CompositeTransformer.transform(transformer, tree, root, parameters);
                            break;
                        case SODIUM:
                            SodiumParameters sodiumParameters = (SodiumParameters) parameters;
                            SodiumTransformer.transform(transformer, tree, root, sodiumParameters);
                            break;
                        case VANILLA:
                            VanillaTransformer.transform(transformer, tree, root, (VanillaParameters) parameters);
                            break;
                        case DH_TERRAIN:
                            DHTerrainTransformer.transform(transformer, tree, root, parameters);
                            break;
                        case DH_GENERIC:
                            DHGenericTransformer.transform(transformer, tree, root, parameters);
                            break;
                        default:
                            if (parameters.patch == EmbeddiumPatch.EMBEDDIUM) {
                                EmbeddiumParameters embeddiumParameters = (EmbeddiumParameters) parameters;
                                EmbeddiumTransformer.transform(transformer, tree, root, embeddiumParameters);
                            } else {
                                throw new UnsupportedOperationException("Unknown patch type: " + parameters.patch);
                            }
                    }
                }
            }
            TextureTransformer.transform(transformer, tree, root,
                    parameters.getTextureStage(), parameters.getTextureMap());
            CompatibilityTransformer.transformEach(transformer, tree, root, parameters);
        };
    }
}
