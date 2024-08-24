package net.caffeinemc.mods.sodium.client.render.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteUtil {

    public static void markSpriteActive(TextureAtlasSprite sprite) {
        org.embeddedt.embeddium.api.render.texture.SpriteUtil.markSpriteActive(sprite);
    }
}
