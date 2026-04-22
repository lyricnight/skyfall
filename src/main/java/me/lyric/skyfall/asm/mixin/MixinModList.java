package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.Skyfall;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * @author lyric
 * @since 2021/5/3
 * @see net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage.ModList
 */
@Mixin(value = FMLHandshakeMessage.ModList.class, remap = false)
public class MixinModList {
    @Shadow private Map<String, String> modTags;

    @Inject(method = "<init>(Ljava/util/List;)V", at = @At("RETURN"))
    private void removeMod(List<ModContainer> modList, CallbackInfo ci)
    {
        if (Minecraft.getMinecraft().isIntegratedServerRunning()) return;
        modTags.remove(Skyfall.MOD_ID);
    }
}
