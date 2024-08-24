package net.caffeinemc.mods.sodium.api.memory;

public class MemoryIntrinsics {

    public static void copyMemory(long src, long dst, int length) {
        org.embeddedt.embeddium.api.memory.MemoryIntrinsics.copyMemory(src, dst, length);
    }
}
