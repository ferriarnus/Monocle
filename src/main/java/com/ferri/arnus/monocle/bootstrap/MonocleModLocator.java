package com.ferri.arnus.monocle.bootstrap;

import cpw.mods.jarhandling.JarContents;
import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.IncompatibleFileReporting;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MonocleModLocator implements IModFileCandidateLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger("Monocle/IrisModParser");

    @Override
    public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
        URL url = MonocleModLocator.class.getResource("/META-INF/mod/monocle-mod-file.jar");
        try {
            Path path = url != null ? Paths.get(url.toURI()) : null;
            if(path == null || !Files.exists(path)) {
                throw new IllegalStateException("Monocle JAR does not exist!");
            }
            LOGGER.info("Located Monocle mod jar at {}", path);
            // Use JarContents so it doesn't detect that the primary JAR is already loaded and skip the path
            pipeline.addJarContent(JarContents.of(path), ModFileDiscoveryAttributes.DEFAULT, IncompatibleFileReporting.WARN_ALWAYS);
        } catch (Exception e) {
           LOGGER.error("Fatal error encountered locating Monocle JAR", e);
        }
    }
}
