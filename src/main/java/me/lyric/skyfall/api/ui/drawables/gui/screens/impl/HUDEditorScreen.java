package me.lyric.skyfall.api.ui.drawables.gui.screens.impl;

import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.ui.HUDEditorInterface;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.gui.hud.HUDButton;
import me.lyric.skyfall.api.ui.drawables.gui.screens.GUISize;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import me.lyric.skyfall.impl.event.mc.ScrollEvent;

import java.awt.*;
import java.util.ArrayList;

import static me.lyric.skyfall.api.utils.interfaces.Globals.mc;

public class HUDEditorScreen {
    private final ArrayList<HUDButton> HUDButtons = new ArrayList<>();
    private float anim, scroll, scrollTarget;
    private long sys;
    public float x, y;

    public HUDEditorScreen() {
        for (HUDBase hudBase : Managers.HUD.getHudModules()) {
            if (!hudBase.isTied()) {
                HUDButtons.add(new HUDButton(hudBase));
            }
        }
        EventBus.getInstance().register(this);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Interface.selectedScreen.equals("HudEditor")) {
            sys = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - sys > 500) {
            return;
        }
        anim = MathsUtils.lerp(anim, Interface.selectedScreen.equals("HudEditor") ? 0.0f : 1.0f, Interface.getDelta());

        float width = GUISize.guiWidth - GUISize.sidebarWidth - 25.0f;
        float x = this.x + GUISize.sidebarWidth + 15.0f;
        float exit = width / 1.5f * anim;

        RenderUtils.prepareScissor(x, y, this.x + GUISize.guiWidth, y + GUISize.guiHeight);

        float leftX = x - exit,
                rightX = x + width / 2.0f + 5.0f + exit,
                leftWidth = x + width / 2.0f - 5.0f - exit,
                rightWidth = this.x + GUISize.guiWidth - 10.0f + exit,
                boxY = y + GUISize.categoryBarY,
                boxHeight = y + GUISize.guiHeight - 100.0f;

        RenderUtils.rounded(leftX, boxY, leftWidth, boxHeight, 5.0f, shade(5));
        RenderUtils.rounded(rightX, boxY, rightWidth, boxHeight, 5.0f, shade(5));
        RenderUtils.roundedOutline(leftX, boxY, leftWidth, boxHeight, 5.0f, shade(-3));
        RenderUtils.roundedOutline(rightX, boxY, rightWidth, boxHeight, 5.0f, shade(-3));

        RenderUtils.releaseScissor();

        scroll = MathsUtils.lerp(scroll, scrollTarget, Interface.getDelta());
        if (!Interface.selectedScreen.equals("HudEditor")) {
            scrollTarget = 0.0f;
        }

        float leftY = boxY + 10.0f + scroll, rightY = boxY + 10.0f + scroll;
        for (HUDButton HUDButton : HUDButtons) {
            HUDButton.guiX = x;
            HUDButton.guiY = boxY;
            HUDButton.guiWidth = GUISize.guiWidth - GUISize.sidebarWidth - 25.0f;
            HUDButton.guiHeight = boxHeight - boxY;
            if (rightY < leftY) {
                HUDButton.targetX = rightX + 5.0f;
                HUDButton.targetY = rightY;
                HUDButton.width = rightWidth - rightX - 10.0f;
                rightY += HUDButton.height + 5.0f;
            } else {
                HUDButton.targetX = leftX + 5.0f;
                HUDButton.targetY = leftY;
                HUDButton.width = leftWidth - leftX - 10.0f;
                leftY += HUDButton.height + 5.0f;
            }
            HUDButton.drawScreen(mouseX, mouseY, partialTicks);
        }

        float bigger = Math.max(leftY - scroll, rightY - scroll);
        scrollTarget = Math.max(boxHeight - bigger, scrollTarget);
        scrollTarget = Math.min(scrollTarget, 0);

        //RenderUtils.prepareScissor(x, y, this.x + GUISize.guiWidth, y + GUISize.guiHeight);
        //RenderUtils.rounded(x + width / 2.0f - 5.0f, boxY, x + width / 2.0f + 5.0f, Math.max(boxY, boxHeight * (1.0f - anim)), 0.0f, Interface.shade(0));


        RenderUtils.rounded(x + width / 2.0f - 75.0f, y + GUISize.guiHeight - 50.0f + 60.0f * anim, x + width / 2.0f + 75.0f, y + GUISize.guiHeight - 30.0f + 60.0f * anim, 5.0f, shade(5));
        RenderUtils.roundedOutline(x + width / 2.0f - 75.0f, y + GUISize.guiHeight - 50.0f + 60.0f * anim, x + width / 2.0f + 75.0f, y + GUISize.guiHeight - 30.0f + 60.0f * anim, 5.0f, shade(-3));
        String text = "Open HUD Editor";

        Managers.TEXT.guiString(text, x + width / 2.0f - Managers.TEXT.stringWidth(text) / 2.0f, y + GUISize.guiHeight - 40.0f + 60.0f * anim - Managers.TEXT.stringHeight() / 2.0f, Color.WHITE);
        RenderUtils.releaseScissor();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (System.currentTimeMillis() - sys > 500) {
            return;
        }
        for (HUDButton HUDButton : HUDButtons) {
            HUDButton.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (mouseButton == 0) {
            float width = GUISize.guiWidth - GUISize.sidebarWidth - 25.0f;
            float x = this.x + GUISize.sidebarWidth + 15.0f;
            if (mouseX > x + width / 2.0f - 75.0f && mouseX < x + width / 2.0f + 75.0f && mouseY > y + GUISize.guiHeight - 50.0f + 60.0f * anim && mouseY < y + GUISize.guiHeight - 30.0f + 60.0f * anim) {
                mc.displayGuiScreen(new HUDEditorInterface());
            }
        }
    }

    @ITheAnnotation(priority = -4)
    public void onScroll(ScrollEvent event) {
        if (event.getMouseX() > x && event.getMouseX() < this.x + GUISize.guiWidth && event.getMouseY() > y && event.getMouseY() < y + GUISize.guiHeight) {
            if (Interface.selectedScreen.equals("HudEditor")) {
                scrollTarget += event.getAmount() / 10.0f;
            }
        }
    }
    
    public static Color color() {
        Color tochange = Interface.background();
        if (tochange.getRed() + 15 > 255 || tochange.getGreen() + 15 > 255 || tochange.getBlue() + 22 > 255) {
            return tochange;
        }
        return new Color(tochange.getRed() + 15, tochange.getGreen() + 15, tochange.getBlue() + 22);
    }

    public static Color shade(int i) {
        if (color().getRed() + i > 255 || color().getGreen() + i > 255 || color().getBlue() + i > 255) {
            return color();
        }
        return new Color(color().getRed() + i, color().getGreen() + i, color().getBlue() + i);
    }
}
