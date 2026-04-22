package me.lyric.skyfall.api.utils.player;

import lombok.experimental.UtilityClass;
import net.minecraft.client.settings.KeyBinding;

import static me.lyric.skyfall.api.utils.interfaces.Globals.mc;
import static org.lwjgl.input.Keyboard.isKeyDown;

/**
 * currently unused methods, kept if needed for future convenience.
 */
@UtilityClass
public final class MovementUtils {
    public static void setKey(KeyBinding keyBind, boolean down) {
        KeyBinding.setKeyBindState(keyBind.getKeyCode(), down);
    }

    public static void stopMovement() {
        setKey(mc.gameSettings.keyBindForward, false);
        setKey(mc.gameSettings.keyBindBack, false);
        setKey(mc.gameSettings.keyBindRight, false);
        setKey(mc.gameSettings.keyBindLeft, false);
    }

    public static void restartMovement() {
        setKey(mc.gameSettings.keyBindForward, isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
        setKey(mc.gameSettings.keyBindBack, isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
        setKey(mc.gameSettings.keyBindRight, isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
        setKey(mc.gameSettings.keyBindLeft, isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
    }

    public static boolean isMoving()
    {
        return mc.thePlayer.moveForward != 0.0 || mc.thePlayer.moveStrafing != 0.0;
    }
}
