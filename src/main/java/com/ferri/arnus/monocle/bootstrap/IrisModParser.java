package com.ferri.arnus.monocle.bootstrap;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import cpw.mods.jarhandling.JarContents;
import cpw.mods.jarhandling.SecureJar;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.loading.moddiscovery.ModJarMetadata;
import net.neoforged.fml.loading.moddiscovery.NightConfigWrapper;
import net.neoforged.fml.loading.moddiscovery.readers.JarModsDotTomlModFileReader;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModFileReader;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class IrisModParser implements IModFileReader {
    private static final Logger LOGGER = LoggerFactory.getLogger("Monocle/IrisModParser");

    @Override
    public @Nullable IModFile read(JarContents contents, ModFileDiscoveryAttributes attributes) {
        var modifiedToml = getModifiedTomlFile(contents);
        if(modifiedToml.isEmpty()) {
            return null;
        }
        LOGGER.info("Handling parsing of mods.toml from {}", contents.getPrimaryPath().getFileName().toString());
        var mjm = new ModJarMetadata(contents);

        var mod = IModFile.create(SecureJar.from(contents, mjm), modFile -> {
            var configWrapper = new NightConfigWrapper(modifiedToml.get());
            // TODO see if this can be done without the internal constructor
            return new ModFileInfo((ModFile)modFile, configWrapper, configWrapper::setFile, List.of());
        }, attributes);
        mjm.setModFile(mod);
        return mod;
    }

    private static final Set<String> DEPS_TO_STRIP = Set.of("sodium", "embeddium");

    private static Optional<CommentedConfig> getModifiedTomlFile(JarContents contents) {
        var modsToml = contents.findFile(JarModsDotTomlModFileReader.MODS_TOML);
        if(modsToml.isPresent()) {
            try(var reader = new InputStreamReader(modsToml.get().toURL().openStream())) {
                var config = new TomlParser().parse(reader);
                List<CommentedConfig> deps = config.get("dependencies.iris");
                if (deps != null) {
                    // This mod declares deps for Iris
                    if(!deps.removeIf(dep -> DEPS_TO_STRIP.contains((String)dep.get("modId")))) {
                        // No incompatible dep, skip rewrite
                        return Optional.empty();
                    }
                    if(deps.isEmpty()) {
                        config.remove("dependencies.iris");
                    }
                    return Optional.of(config);
                }
            } catch(Exception e) {
                LOGGER.error("Exception reading mod file", e);
            }
        }
        return Optional.empty();
    }
    @Override
    public int getPriority() {
        return HIGHEST_SYSTEM_PRIORITY + 1;
    }
}
