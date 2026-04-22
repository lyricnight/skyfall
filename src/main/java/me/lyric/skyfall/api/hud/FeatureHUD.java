package me.lyric.skyfall.api.hud;

import lombok.Getter;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.Setting;
import me.lyric.skyfall.api.setting.types.*;
import me.lyric.skyfall.api.utils.interfaces.Globals;

import java.awt.*;
import java.util.List;

/**
 * @author lyric
 * Base class for HUD elements that are tied to a specific feature.
 * The HUD element will be enabled/disabled with the feature,
 * and will be configured in the feature's settings.
 * @apiNote whenever creating a setting in a FeatureHUD, always append 'HUD' to the feature name.
 */
@Getter
public class FeatureHUD extends HUDBase implements Globals {
    /**
     * The feature this HUD element is tied to.
     */
    private final Feature parentFeature;

    /**
     * Constructor for a feature-tied HUD element
     * @param name - name of the HUD element
     * @param parentFeature - the feature this HUD is tied to
     */
    public FeatureHUD(String name, Feature parentFeature) {
        super(name);
        this.parentFeature = parentFeature;
        this.isTied = true;
        this.settingsHUD.remove(enabledHUD);
        this.settingsHUD.forEach(this::addToFeature);
        parentFeature.registerHUD(this);
        Managers.HUD.add(this);
        updateEnabledState();
    }

    /**
     * Override the setEnabled method to ensure we don't allow manual enabling/disabling
     * for feature-tied HUD elements - they should only be enabled/disabled with the feature
     */
    @Override
    public void setEnabled(boolean enabled) {
        if (enabled)
        {
            enable();
        }
        else
        {
            disable();
        }
    }

    /**
     * Override the toggle method to prevent manual toggling
     */
    @Override
    public void toggle() {
        // no-op
    }

    /**
     * Update the enabled state based on the associated feature
     * This should be called when the feature's state changes
     */
    public void updateEnabledState() {
        setEnabled(parentFeature.isEnabled());
    }

    /**
     * Override the setting method to redirect settings to the parent feature
     * This ensures that settings are stored with the feature, not in the HUD module
     */
    @Override
    public boolean getEnabled() {
        return parentFeature.isEnabled();
    }

    /**
     * Override the setting methods from HUDBase to redirect settings to the parent feature
     * This ensures that settings are stored with the feature, not in the HUD module
     */
    private void addToFeature(Setting<?> setting) {
        setting.invokeTab("HUD: " + getName());
        parentFeature.getSettings().add(setting);
    }

    /**
     * setting overrides.
     * @apiNote these are stupid code, but required due to Java running the class's constructor code before loading variable methods, meaning that settings defined inside the FeatureHUD wouldn't be added to feature setting list.
     */
    @Override
    public BindSetting settingHUD(String name, int key) {
        BindSetting setting = new BindSetting(name, key);
        settingsHUD.add(setting);
        addToFeature(setting);
        return setting;
    }

    @Override
    public BooleanSetting settingHUD(String name, boolean value) {
        BooleanSetting setting = new BooleanSetting(name, value);
        settingsHUD.add(setting);
        addToFeature(setting);
        return setting;
    }

    @Override
    public FloatSetting settingHUD(String name, float value, float min, float max) {
        FloatSetting setting = new FloatSetting(name, value, min, max);
        settingsHUD.add(setting);
        addToFeature(setting);
        return setting;
    }

    @Override
    public IntegerSetting settingHUD(String name, int value, int min, int max) {
        IntegerSetting setting = new IntegerSetting(name, value, min, max);
        settingsHUD.add(setting);
        addToFeature(setting);
        return setting;
    }

    @Override
    public ModeSetting settingHUD(String name, String value, List<String> values) {
        ModeSetting setting = new ModeSetting(name, value, values);
        settingsHUD.add(setting);
        addToFeature(setting);
        return setting;
    }

    @Override
    public ColourSetting settingHUD(String name, Color value) {
        ColourSetting setting = new ColourSetting(name, value);
        settingsHUD.add(setting);
        addToFeature(setting);
        return setting;
    }

    @Override
    public StringSetting settingHUD(String name, String value) {
        StringSetting setting = new StringSetting(name, value);
        settingsHUD.add(setting);
        addToFeature(setting);
        return setting;
    }

    @Override
    public ActionSetting settingHUD(String name, String buttonName, Runnable value) {
        ActionSetting setting = new ActionSetting(name, buttonName, value);
        settingsHUD.add(setting);
        addToFeature(setting);
        return setting;
    }
}

