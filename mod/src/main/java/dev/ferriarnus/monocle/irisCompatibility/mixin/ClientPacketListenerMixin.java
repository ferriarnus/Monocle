package dev.ferriarnus.monocle.irisCompatibility.mixin;

import net.irisshaders.iris.Iris;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    /**
     * @author embeddedt
     * @reason make sure the message directs users to us, not Iris, as our transformer likely has different behavior
     * from theirs
     */
    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;enforcesSecureChat()Z"))
    private void printMonocleErrorForIrisError(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        // Same logic as Iris' mixin, but run earlier to suppress their logic
        Iris.getStoredError().ifPresent(e -> {
            player.displayClientMessage(Component.translatable(
                    "monocle.shader_load_exception"
            ).append(Component.literal("Copy Info").withStyle(arg ->
                    arg.withUnderlined(true).withColor(ChatFormatting.BLUE)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, e.getMessage()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))
            )), false);
        });
    }
}
