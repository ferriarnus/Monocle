package dev.ferriarnus.monocle.moddedshaders.config;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLPaths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class ConfigPlugin implements IMixinConfigPlugin {

    private static final String MIXINCONFIG = Type.getDescriptor(Config.class);
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("monocle.properties");
    public static final String FALSE = "false";
    private static final String[] MODS = new String[]{
            "justdirethings",
            "supplementaries",
            "caxton",
            "twilightforest",
            "enchanted",
            "the_bumblezone",
            "forbidden_arcanus",
            "xycraft",
            "immersiveengineering",
            "computercraft",
            "creeperoverhaul",
            "arsnouveau",
            "mekanism"
    };

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        try(InputStream stream = this.getClass().getClassLoader().getResourceAsStream(mixinClassName.replace('.', '/') + ".class")) {
            ClassReader reader = new ClassReader(stream);
            ClassNode node = new ClassNode();
            reader.accept(node,  ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
            if (node.invisibleAnnotations == null) {
                return true;
            }

            Properties config = new Properties();
            if (!Files.exists(CONFIG_PATH)) {
                makeConfig();
            }
            config.load(Files.newBufferedReader(CONFIG_PATH));

            for(AnnotationNode annotation : node.invisibleAnnotations) {
                if (annotation.desc.equals(MIXINCONFIG)) {
                    for(int i = 0; i < annotation.values.size(); i += 2) {
                        if(annotation.values.get(i).equals("value")) {
                            String modId = (String) annotation.values.get(i + 1);
                            if(modId != null) {
                                if (FALSE.equals(config.getProperty("All")) && FALSE.equals(config.getProperty(modId))) {
                                    return false;
                                }
                            }
                            break;
                        }
                    }
                }
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void makeConfig() {
        try (var writer = Files.newBufferedWriter(CONFIG_PATH);){
            writer.write("# Modded Shader Config\n");
            writer.write("# ====================\n");
            writer.write("# Enable/disable all modded shader compat (true/false)\n");
            writer.write("All = true\n");
            writer.write("# ====================\n");
            writer.write("# Enable/disable for individual mods (true/false)\n");
            for (String mod : MODS) {
                writer.write(mod + " = false\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
