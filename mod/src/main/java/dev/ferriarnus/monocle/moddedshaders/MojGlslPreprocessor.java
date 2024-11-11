package dev.ferriarnus.monocle.moddedshaders;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import dev.ferriarnus.monocle.Monocle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.neoforge.client.ClientHooks;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MojGlslPreprocessor extends GlslPreprocessor {

    private final String path;
    private final ResourceProvider resourceProvider;
    private final Set<String> importedPaths = new HashSet<>();

    MojGlslPreprocessor(String path, ResourceProvider resourceProvider) {
        this.path = path;
        this.resourceProvider = resourceProvider;
    }

    public String applyImport(boolean p_173374_, @NotNull String p_173375_) {
        ResourceLocation resourcelocation = ClientHooks.getShaderImportLocation(path, p_173374_, p_173375_);
        if (!this.importedPaths.add(resourcelocation.toString())) {
            return null;
        } else {
            try {
                Reader reader = resourceProvider.openAsReader(resourcelocation);

                String s2;
                try {
                    s2 = IOUtils.toString(reader);
                } catch (Throwable var9) {
                    try {
                        reader.close();
                    } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                    }

                    throw var9;
                }

                reader.close();

                return s2;
            } catch (IOException var10) {
                Monocle.LOGGER.error("Could not open GLSL import {}: {}", resourcelocation, var10.getMessage());
                return "#error " + var10.getMessage();
            }
        }
    }

    @Override
    public List<String> process(String shaderData) {
        var result = super.process(shaderData);
        return result;
    }
}
