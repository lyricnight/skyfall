package me.lyric.skyfall.impl.feature.internals;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.*;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.impl.event.ui.SkyfallUIClosedEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Arrays;

/**
 * @author lyric
 * feature that controls GUI interface.
 */
public final class Interface extends Feature {
    public ColourSetting colourSetting = setting("Colour", new Color(120, 81, 169)).invokeTab("Colouring");
    public ColourSetting background = setting("Background", new Color(33, 28, 44, 200)).invokeTab("Colouring");
    public ColourSetting notificationColour = setting("Notification Colour", new Color(120, 81, 169)).invokeTab("Notifications");
    public BooleanSetting notify = setting("Rendered Notifications", true).invokeTab("Notifications");
    public StringSetting prefix = setting("Command prefix", "-").invokeTab("Commands");
    public ActionSetting sync = setting("Sync", "Sync HUD colours", () -> {
       for (HUDBase hud : Managers.HUD.getHudModules()) {
           hud.colorHUD.invokeValue(this.background.getValue());
           hud.color1HUD.invokeValue(this.background.getValue());
           hud.color2HUD.invokeValue(this.colourSetting.getValue());
       }
    }).invokeTab("Colouring");
    public ActionSetting debug = setting("Debug", "Debug Shaders", Managers.SHADERS::debug).invokeTab("Debug");
    public ActionSetting location = setting("Location", "Location Debug", () -> Managers.MESSAGES.send("Current Location: " + Managers.LOCATION.getCurrentIsland())).invokeTab("Debug");
    public ActionSetting threads = setting("Threads", "Thread Pool Debug", () -> Managers.MESSAGES.send(Managers.THREADS.getDebugInfo())).invokeTab("Debug");
    //every time I use one of these settings I die a little inside.
    public ModeSetting internalCode = setting("Colour Code Name", "Black", Arrays.asList("None", "Black", "DarkGray", "Gray", "DarkBlue", "Blue", "DarkGreen", "Green", "DarkAqua", "Aqua", "DarkRed", "Red", "DarkPurple", "Purple", "Gold", "Yellow")).invokeTab("Commands");
    public ModeSetting internalCode2 = setting("Colour Code Bracket", "DarkGray", Arrays.asList("None", "Black", "DarkGray", "Gray", "DarkBlue", "Blue", "DarkGreen", "Green", "DarkAqua", "Aqua", "DarkRed", "Red", "DarkPurple", "Purple", "Gold", "Yellow")).invokeTab("Commands");
    public BooleanSetting boldName = setting("Bold Name", false).invokeTab("Commands");
    public BooleanSetting boldBracket = setting("Bold Bracket", false).invokeTab("Commands");

    public Interface()
    {
        super("Interface", Category.Internals);
        getKeybind().invokeValue(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {
        if (Null.is()) return;
        mc.displayGuiScreen(Skyfall.INTERFACE);
    }

    @Override
    public void onDisable() {
        if (Null.is() || mc.currentScreen == null) return;
        mc.displayGuiScreen(null);
    }

    @ITheAnnotation
    public void onUIClosed(SkyfallUIClosedEvent ignored)
    {
        toggle();
    }
}
