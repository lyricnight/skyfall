package me.lyric.skyfall.asm;

import me.lyric.skyfall.Skyfall;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

/**
 * @author lyric
 */
@IFMLLoadingPlugin.Name("Skyfall " + Skyfall.VERSION)
@IFMLLoadingPlugin.MCVersion("1.8.9")
public class SkyfallCorePlugin implements IFMLLoadingPlugin {

    public SkyfallCorePlugin() {
        //system call here since Skyfall's own logger may not have properly instantiated at this point.
        System.out.println("----------Skyfall reached MixinLoading-----------");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.skyfall.json");
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}