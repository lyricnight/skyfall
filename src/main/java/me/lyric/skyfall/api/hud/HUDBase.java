package me.lyric.skyfall.api.hud;

import lombok.Getter;
import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.Setting;
import me.lyric.skyfall.api.setting.types.*;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.render.font.CustomFont;
import me.lyric.skyfall.api.utils.shader.GradientShader;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author lyric
 * base class for HUD elements
 */
public class HUDBase implements Globals {
    /**
     * typical stuff.
     */
    @Getter
    private final String name;

    /**
     * more typical stuff
     */
    @Getter
    protected final List<Setting<?>> settingsHUD = new ArrayList<>();

    /**
     * represents if we're dragging the HUD element in HUD editor.
     */
    public boolean dragging = false;

    /**
     * represents if the HUD element is tied to a feature.
     */
    @Getter
    protected boolean isTied;

    /**
     * position values.
     */
    public float x, y, width, height, dragX, dragY;

    /**
     * bunch of settings. these are only used if the HUDBase is not bound to a feature.
     */
    protected final BooleanSetting enabledHUD = new BooleanSetting("Enabled", false);
    protected final IntegerSetting fontSizeHUD = new IntegerSetting("Font Size", 18, 10, 50);
    protected final ModeSetting modeHUD = new ModeSetting("Color Mode", "Gradient", Arrays.asList("Gradient", "Static"));
    public final ColourSetting colorHUD = new ColourSetting("Colour", new Color(113, 93, 214)).invokeVisibility(z -> modeHUD.getValue().equals("Static"));
    public final ColourSetting color1HUD = new ColourSetting("Primary", new Color(113, 93, 214)).invokeVisibility(z -> modeHUD.getValue().equals("Gradient"));
    public final ColourSetting color2HUD = new ColourSetting("Secondary", new Color(113, 220, 214)).invokeVisibility(z -> modeHUD.getValue().equals("Gradient"));
    protected final FloatSetting speedHUD = new FloatSetting("Speed", 1.0f, 0.1f, 15f).invokeVisibility(z -> modeHUD.getValue().equals("Gradient"));
    protected final FloatSetting stepHUD = new FloatSetting("Step", 0.3f, 0.1f, 1.0f).invokeVisibility(z -> modeHUD.getValue().equals("Gradient"));
    private transient CustomFont cachedFont;
    private transient int cachedFontSize = -1;

    /**
     * Cached gradient array
     */
    private final transient Color[] cachedGradient = new Color[2];

    /**
     * constructor for HUDBases that are not bound to a feature.
     * @param name - name of the HUD element.
     */
    public HUDBase(String name)
    {
        this.name = name;
        this.settingsHUD.addAll(Arrays.asList(enabledHUD, fontSizeHUD, modeHUD, colorHUD, color1HUD, color2HUD, speedHUD, stepHUD));
    }

    /**
     * where we treat the HUD element as a feature.
     */

    protected void enable()
    {
        EventBus.getInstance().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    protected void disable()
    {
        EventBus.getInstance().unregister(this);
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public void toggle() {
        enabledHUD.invokeValue(!enabledHUD.getValue());
        if (enabledHUD.getValue()) {
            enable();
        } else {
            disable();
        }
    }

    public void setEnabled(boolean enabled) {
        if (enabled && this.enabledHUD.getValue() || !enabled && !this.enabledHUD.getValue()) {
            return;
        }
        this.enabledHUD.invokeValue(enabled);
        if (enabled) {
            enable();
        } else {
            disable();
        }
    }

    /**
     * this is what to use when rendering something that has the potential to be gradient shaded.
     * @param renderCallback - code to run
     */
    public void renderWithShader(Runnable renderCallback) {
        if (isGradient()) {
            GradientShader.setup(stepHUD.getValue(), speedHUD.getValue(), getGradient()[0], getGradient()[1]);
            try {
                renderCallback.run();
            } finally {
                GradientShader.finish();
                GL11.glLineWidth(1.0f);
            }
        } else {
            renderCallback.run();
        }
    }

    /**
     * lightning overrides.
     */

    public void onRender2D() {}

    public void onTickPre() {}

    public void onTickPost() {}

    /**
     * setting stuff
     */
    public BindSetting settingHUD(String name, int key) {
        BindSetting setting = new BindSetting(name, key);
        settingsHUD.add(setting);
        return setting;
    }

    public BooleanSetting settingHUD(String name, boolean value) {
        BooleanSetting setting = new BooleanSetting(name, value);
        settingsHUD.add(setting);
        return setting;
    }

    public FloatSetting settingHUD(String name, float value, float min, float max) {
        FloatSetting setting = new FloatSetting(name, value, min, max);
        settingsHUD.add(setting);
        return setting;
    }

    public IntegerSetting settingHUD(String name, int value, int min, int max) {
        IntegerSetting setting = new IntegerSetting(name, value, min, max);
        settingsHUD.add(setting);
        return setting;
    }

    public ModeSetting settingHUD(String name, String value, List<String> values) {
        ModeSetting setting = new ModeSetting(name, value, values);
        settingsHUD.add(setting);
        return setting;
    }

    public ColourSetting settingHUD(String name, Color value) {
        ColourSetting setting = new ColourSetting(name, value);
        settingsHUD.add(setting);
        return setting;
    }

    public StringSetting settingHUD(String name, String value) {
        StringSetting setting = new StringSetting(name, value);
        settingsHUD.add(setting);
        return setting;
    }

    public ActionSetting settingHUD(String name, String buttonName, Runnable value) {
        ActionSetting setting = new ActionSetting(name, buttonName, value);
        settingsHUD.add(setting);
        return setting;
    }

    /**
     * ease of access getters for settings.
     * can't lombok this, so it's quite boilerplate
     */

    public BooleanSetting getSetting() {
        return enabledHUD;
    }

    public boolean getEnabled() {
        return enabledHUD.getValue();
    }

    protected boolean isGradient() {
        return modeHUD.getValue().equals("Gradient");
    }

    public Color getRenderColor() {
        return modeHUD.getValue().equals("Static") ? getStatic() : Color.WHITE;
    }

    public Color[] getGradient() {
        cachedGradient[0] = color1HUD.getValue();
        cachedGradient[1] = color2HUD.getValue();
        return cachedGradient;
    }

    public Color getStatic() {
        return colorHUD.getValue();
    }

    public void invalidateFontCache() {
        cachedFont = null;
        cachedFontSize = -1;
    }

    /**
     * Get cached font instance to avoid HashMap lookups on every render.
     * Cache is invalidated via invalidateFontCache() when font family changes.
     */
    private CustomFont getCachedFont() {
        int currentSize = fontSizeHUD.getValue();
        if (cachedFont == null || cachedFontSize != currentSize) {
            cachedFont = Managers.TEXT.getCustomFont(currentSize);
            cachedFontSize = currentSize;
        }
        return cachedFont;
    }

    protected void drawHUDString(String text, float x, float y, Color color) {
        getCachedFont().drawStringWithShadow(text, x, y, color);
    }

    protected void drawHUDStringNoShadow(String text, float x, float y, Color color) {
        getCachedFont().drawString(text, x, y, color, false);
    }

    protected float getHUDStringWidth(String text) {
        return getCachedFont().getStringWidth(text);
    }

    protected float getHUDStringHeight() {
        return getCachedFont().getHeight();
    }
}

