package me.lyric.skyfall.api.ui.drawables.gui.screens.impl;

import lombok.Getter;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.gui.category.CategoryBar;
import me.lyric.skyfall.api.ui.drawables.gui.feature.ActiveFeature;
import me.lyric.skyfall.api.ui.drawables.gui.screens.GUISize;
import me.lyric.skyfall.api.utils.render.RenderUtils;

import java.util.ArrayList;

public class DefaultScreen {
    private final ArrayList<CategoryBar> categoryBars = new ArrayList<>();
    private final ArrayList<ActiveFeature> activeFeatures = new ArrayList<>();
    private long sys;
    @Getter
    public static Feature activeFeature = null;

    public float x, y;

    public DefaultScreen() {
        for (Category category : Category.values()) {
            categoryBars.add(new CategoryBar(category));
        }
        for (Feature feature : Managers.FEATURES.getFeatures()) {
            activeFeatures.add(new ActiveFeature(feature));
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Interface.selectedScreen.equals("Default")) {
            sys = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - sys > 500) {
            return;
        }

        /* Category bars */
        for (CategoryBar categoryBar : categoryBars) {
            categoryBar.x = x + GUISize.sidebarWidth + 15.0f;
            categoryBar.y = y + GUISize.categoryBarY;
            categoryBar.width = GUISize.guiWidth - GUISize.sidebarWidth - 25.0f;
            categoryBar.height = 70.0f;

            /* Category bar scissor */
            RenderUtils.prepareScissor(x, y, x + GUISize.guiWidth, y + GUISize.guiHeight);

            categoryBar.drawScreen(mouseX, mouseY, partialTicks);

            /* Release scissor */
            RenderUtils.releaseScissor();
        }
        for (ActiveFeature activeFeature : activeFeatures) {
            activeFeature.x = x + GUISize.sidebarWidth + 15.0f;
            activeFeature.y = y + 105.0f;
            activeFeature.width = GUISize.guiWidth - GUISize.sidebarWidth - 25.0f;
            activeFeature.height = GUISize.guiHeight - y - 40.0f;
            activeFeature.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (System.currentTimeMillis() - sys > 500) {
            return;
        }
        for (CategoryBar categoryBar : categoryBars) {
            categoryBar.mouseClicked(mouseX, mouseY, mouseButton);
        }
        for (ActiveFeature activeFeature : activeFeatures) {
            activeFeature.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        for (CategoryBar categoryBar : categoryBars) {
            categoryBar.keyTyped(typedChar, keyCode);
        }
        for (ActiveFeature activeFeature : activeFeatures) {
            activeFeature.keyTyped(typedChar, keyCode);
        }
    }

    public static void setActiveFeature(Feature activeFeature) {
        DefaultScreen.activeFeature = activeFeature;
    }
}
