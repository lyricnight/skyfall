package me.lyric.skyfall.api.ui.drawables.gui.feature;

import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.Setting;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.Drawable;
import me.lyric.skyfall.api.ui.drawables.gui.feature.tab.FeatureTab;
import me.lyric.skyfall.api.ui.drawables.gui.screens.impl.DefaultScreen;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.impl.event.mc.ScrollEvent;

import java.util.ArrayList;

public class ActiveFeature implements Drawable {
    private final ArrayList<FeatureTab> featureTabs = new ArrayList<>();
    private final Feature feature;
    public float x, y, width, height, scroll, scrollTarget;

    public ActiveFeature(Feature feature) {
        this.feature = feature;
        for (Setting<?> setting : feature.getSettings()) {
            if (setting.getName().equals("Enabled") || setting.getName().equals("Keybind")) {
                continue;
            }
            boolean contains = false;
            for (FeatureTab featureTab : featureTabs) {
                if (featureTab.getName().equals(setting.getTab())) {
                    featureTab.settings.add(setting);
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                FeatureTab featureTab = new FeatureTab(feature, setting.getTab());
                featureTab.settings.add(setting);
                featureTabs.add(featureTab);
            }
        }
        featureTabs.forEach(FeatureTab::init);
        EventBus.getInstance().register(this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (DefaultScreen.getActiveFeature() != null && DefaultScreen.getActiveFeature().equals(feature)) {
            scroll = MathsUtils.lerp(scroll, scrollTarget, Interface.getDelta());
        } else {
            scrollTarget = 0.0f;
        }

        float leftY = y + 10.0f, rightY = y + 10.0f;
        for (FeatureTab featureTab : featureTabs) {
            float x = this.x;
            boolean right = rightY < leftY;
            if (right) {
                x += width / 2.0f + 5.0f;
            }
            featureTab.targetX = x + (width + 5.0f) * featureTab.anim * (right ? 1.0f : -1.0f);
            featureTab.targetY = (right ? rightY : leftY) + scroll;
            featureTab.width = width / 2.0f - 5.0f;
            featureTab.guiX = this.x;
            featureTab.guiY = y;
            featureTab.guiWidth = width;
            featureTab.guiHeight = height;
            featureTab.drawScreen(mouseX, mouseY, partialTicks);
            if (right) {
                rightY += featureTab.getHeight() + 15.0f;
            } else {
                leftY += featureTab.getHeight() + 15.0f;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if ((DefaultScreen.getActiveFeature() != null && DefaultScreen.getActiveFeature() == feature) || Interface.selectedScreen.equals("HudEditor")) {
            featureTabs.stream().filter(featureTab -> !featureTab.ignore()).forEach(featureTab -> featureTab.mouseClicked(mouseX, mouseY, mouseButton));
        }
    }


    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if ((DefaultScreen.getActiveFeature() != null && DefaultScreen.getActiveFeature() == feature) || Interface.selectedScreen.equals("HudEditor")) {
            featureTabs.stream().filter(featureTab -> !featureTab.ignore()).forEach(featureTab -> featureTab.keyTyped(typedChar, keyCode));
        }
    }

    @ITheAnnotation(priority = -2)
    public void onScroll(ScrollEvent event) {
        if (event.getMouseX() > x && event.getMouseX() < x + width && event.getMouseY() > y && event.getMouseY() < y + height) {
            scrollTarget +=event.getAmount() / 10.0f;
        }
    }
}
