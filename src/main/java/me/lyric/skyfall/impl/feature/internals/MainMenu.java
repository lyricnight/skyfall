package me.lyric.skyfall.impl.feature.internals;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.ActionSetting;
import me.lyric.skyfall.api.setting.types.IntegerSetting;
import me.lyric.skyfall.api.setting.types.ModeSetting;
import me.lyric.skyfall.api.setting.types.StringSetting;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.shader.screen.ScreenShader;

import java.io.IOException;
import java.util.Arrays;

/**
 * feature that controls our tweaks to the Minecraft main menu
 * @author lyric
 */
public final class MainMenu extends Feature {

    public ModeSetting mainMenuMode = setting("Main Menu Rendering Mode", "Shader", Arrays.asList("Shader", "Image")).invokeTab("Tweaks");

    public StringSetting imagePath = setting("Image Path", "default.png").invokeTab("Tweaks").invokeVisibility(v -> mainMenuMode.getValue().equals("Image"));

    public ModeSetting mainMenuShader = setting("Main Menu Shader", "SPACE", Arrays.asList(Arrays.stream(MainMenuShaders.values()).map(MainMenuShaders::name).toArray(String[]::new))).invokeTab("Tweaks").invokeVisibility(v -> mainMenuMode.getValue().equals("Shader"));

    public ActionSetting reloadShader = setting("Reload", "Reload Main Menu Shader", this::setShader).invokeTab("Tweaks").invokeVisibility(v -> mainMenuMode.getValue().equals("Shader"));

    public IntegerSetting fps = setting("FPS In Menu", 120, 30, 500).invokeTab("Performance");

    @Getter
    private ScreenShader SHADER;

    public MainMenu() {
        super("MainMenu", Category.Internals);
    }

    @Override
    public void onInit()
    {
        if (mainMenuMode.getValue().equals("Shader") && this.isEnabled())
        {
            setShader();
        }
    }

    public void setShader()
    {
        try {
            Skyfall.LOGGER.info("Setting main menu shader to: /assets/minecraft/textures{}", MainMenuShaders.valueOf(mainMenuShader.getValue()).getShader());
            SHADER = new ScreenShader("/assets/minecraft/textures" + MainMenuShaders.valueOf(mainMenuShader.getValue()).getShader());
        } catch (IOException e) {
            ExceptionHandler.handle(e, this.getClass());
            SHADER = null;
        }
    }

    @AllArgsConstructor
    public enum MainMenuShaders {
        BLUEGRID("/shaders/bluegrid.fsh"),
        BLUENEBULA("/shaders/bluenebula.fsh"),
        CAVE("/shaders/cave.fsh"),
        JUPITER("/shaders/jupiter.fsh"),
        KIRBY("/shaders/kirby.fsh"),
        MATRIX("/shaders/matrix.fsh"),
        MINECRAFT("/shaders/minecraft.fsh"),
        PINWHEEL("/shaders/pinwheel.fsh"),
        PURPLEGRID("/shaders/purplegrid.fsh"),
        REDGLOW("/shaders/redglow.fsh"),
        SKY("/shaders/sky.fsh"),
        SNAKE("/shaders/snake.fsh"),
        SPACE("/shaders/space.fsh"),
        STORM("/shaders/storm.fsh"),
        WAIFU("/shaders/waifu.fsh"),
        MARIO("/shaders/mario.fsh"),
        STARS("/shaders/stars.fsh");
        @Getter
        final String shader;
    }
}
