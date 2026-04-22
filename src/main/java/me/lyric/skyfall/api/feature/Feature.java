package me.lyric.skyfall.api.feature;

import com.mojang.realmsclient.gui.ChatFormatting;
import lombok.Getter;
import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.hud.FeatureHUD;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.Setting;
import me.lyric.skyfall.api.setting.types.*;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.impl.feature.internals.Interface;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author lyric
 */

@SuppressWarnings("EmptyMethod")
@Getter
public class Feature implements Globals {
    protected final String name;
    protected final Category category;
    protected final BooleanSetting enabled;
    protected final BooleanSetting drawn;
    protected final BindSetting keybind;
    protected final List<Setting<?>> settings = new ArrayList<>();
    protected final List<FeatureHUD> hudElements = new ArrayList<>();

    /**
     * kinda hacky way to do animations for features
     * 0.0f = disabled
     * 1.0f = maximum
     */
    public float anim = 0.0f;

    /**
     * constructor
     * @param name - name
     * @param category - category
     */

    public Feature(String name, Category category)
    {
        this.name = name;
        this.category = category;
        this.enabled = new BooleanSetting("Enabled", false);
        this.drawn = new BooleanSetting("Drawn", true);
        this.keybind = new BindSetting("Bind", Keyboard.KEY_NONE);
        this.settings.addAll(Arrays.asList(enabled, keybind, drawn));
    }

    /**
     * internal methods
     */

    private void enable()
    {
        EventBus.getInstance().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        hudElements.forEach(FeatureHUD::updateEnabledState);
        onEnable();
        if (Null.is()) return;
        Managers.MESSAGES.sendOverwrite(ChatFormatting.BOLD + name + ChatFormatting.RESET + " has been " + ChatFormatting.GREEN + "enabled.", hashCode());
        if (Managers.FEATURES.get(Interface.class).notify.getValue()) Managers.NOTIFICATIONS.notify("Feature Enabled!", name + " has been enabled.", 2500, "other/tick.png");
    }

    private void disable()
    {
        EventBus.getInstance().unregister(this);
        MinecraftForge.EVENT_BUS.unregister(this);
        hudElements.forEach(FeatureHUD::updateEnabledState);
        onDisable();
        if (Null.is()) return;
        Managers.MESSAGES.sendOverwrite(ChatFormatting.BOLD + name + ChatFormatting.RESET + " has been " + ChatFormatting.RED + "disabled.", hashCode());
        if (Managers.FEATURES.get(Interface.class).notify.getValue()) Managers.NOTIFICATIONS.notify("Feature Disabled!", name + " has been disabled.", 2500, "other/cross.png");

    }

    /**
     * lightning overrides.
     * can't make these protected as they're called in Features (Manager)
     */

    public void onEnable()
    {

    }

    public void onDisable()
    {

    }

    public void onRender2D()
    {

    }

    public void onRender3D() {

    }

    public void onUpdate()
    {
        
    }

    public void onTickPre()
    {

    }

    public void onTickPost()
    {

    }

    public void onInit()
    {

    }

    public void onWorldChange()
    {

    }

    public String displayAppend()
    {
        return "";
    }

    public void toggle() {
        enabled.invokeValue(!enabled.getValue());
        if (enabled.getValue()) {
            enable();
        } else {
            disable();
        }
    }

    public void setEnabled(boolean enable)
    {
        if (enable)
        {
            if (!isEnabled())
            {
                enabled.invokeValue(true);
                enable();
            }
        }
        else
        {
            if (isEnabled())
            {
                enabled.invokeValue(false);
                disable();
            }
        }
    }

    /**
     * Registers a HUD element with this feature
     * @param hudElement - the HUD element to register
     */
    public void registerHUD(FeatureHUD hudElement) {
        if (!hudElements.contains(hudElement)) {
            hudElements.add(hudElement);
        }
        else throw new IllegalStateException("HUD element attempted to be registered twice to: " + this.getName() + " : " + hudElement.getName());
    }

    /**
     * setting stuff
     */
    public BindSetting setting(String name, int key) {
        BindSetting setting = new BindSetting(name, key);
        settings.add(setting);
        return setting;
    }

    public BooleanSetting setting(String name, boolean value) {
        BooleanSetting setting = new BooleanSetting(name, value);
        settings.add(setting);
        return setting;
    }

    public FloatSetting setting(String name, float value, float min, float max) {
        FloatSetting setting = new FloatSetting(name, value, min, max);
        settings.add(setting);
        return setting;
    }

    public IntegerSetting setting(String name, int value, int min, int max) {
        IntegerSetting setting = new IntegerSetting(name, value, min, max);
        settings.add(setting);
        return setting;
    }

    public ModeSetting setting(String name, String value, List<String> values) {
        ModeSetting setting = new ModeSetting(name, value, values);
        settings.add(setting);
        return setting;
    }

    public ColourSetting setting(String name, Color value) {
        ColourSetting setting = new ColourSetting(name, value);
        settings.add(setting);
        return setting;
    }

    public StringSetting setting(String name, String value) {
        StringSetting setting = new StringSetting(name, value);
        settings.add(setting);
        return setting;
    }

    public ActionSetting setting(String name, String buttonName, Runnable value) {
        ActionSetting setting = new ActionSetting(name, buttonName, value);
        settings.add(setting);
        return setting;
    }

    /**
     * getters and setters
     */
    public boolean isEnabled() {
        return enabled.getValue();
    }

    public float getStringWidthFull(int fontSize) {
        return -(Managers.TEXT.hudStringWidth(name, fontSize) + Managers.TEXT.hudStringWidth(displayAppend(), fontSize));
    }

    public float getStringWidth(int fontSize) {
        return Managers.TEXT.hudStringWidth(name, fontSize);
    }
}
