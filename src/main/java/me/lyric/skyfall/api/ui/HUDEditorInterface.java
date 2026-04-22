package me.lyric.skyfall.api.ui;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;

public class HUDEditorInterface extends GuiScreen {
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.rect(width / 2.0f - 0.5f, 0.0f, width / 2.0f + 0.5f, height, new Color(255, 255, 255, 100));
        RenderUtils.rect(0.0f, height / 2.0f - 0.5f, width, height / 2.0f + 0.5f, new Color(255, 255, 255, 100));

        RenderUtils.rounded(width / 2.0f - 50.0f, height / 2.0f - 10.0f, width / 2.0f + 50.0f, height / 2.0f + 10.0f, 5.0f, shade(5));
        RenderUtils.roundedOutline(width / 2.0f - 50.0f, height / 2.0f - 10.0f, width / 2.0f + 50.0f, height / 2.0f + 10.0f, 5.0f, shade(-3));
        Managers.TEXT.string("Return", width / 2.0f - Managers.TEXT.stringWidth("Return") / 2.0f, height / 2.0f - Managers.TEXT.stringHeight() / 2.0f, Color.WHITE);

        Managers.HUD.getHudModules().stream().filter(HUDBase::getEnabled).forEach(hudModule -> {
            RenderUtils.roundedOutline(hudModule.x, hudModule.y, hudModule.x + hudModule.width, hudModule.y + hudModule.height, 0.0f, Interface.primary());
            if (hudModule.dragging) {
                hudModule.x = hudModule.dragX + mouseX;
                hudModule.y = hudModule.dragY + mouseY;
            }
        });
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            Managers.HUD.getHudModules().stream().filter(HUDBase::getEnabled)
                .filter(hudModule -> mouseX > hudModule.x && mouseX < hudModule.x + hudModule.width &&
                                    mouseY > hudModule.y && mouseY < hudModule.y + hudModule.height)
                .forEach(hudModule -> {
                    hudModule.dragX = hudModule.x - mouseX;
                    hudModule.dragY = hudModule.y - mouseY;
                    hudModule.dragging = true;
                });

            if (insideReturn(mouseX, mouseY)) {
                mc.displayGuiScreen(Skyfall.INTERFACE);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        Managers.HUD.getHudModules().stream().filter(HUDBase::getEnabled).forEach(hudModule -> hudModule.dragging = false);
    }

    private boolean insideReturn(int mouseX, int mouseY) {
        return mouseX > width / 2.0f - 50.0f && mouseX < width / 2.0f + 50.0f && mouseY > height / 2.0f - 10.0f && mouseY < height / 2.0f + 10.0f;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public Color color() {
        return new Color(43, 46, 66);
    }

    public static Color shade(int i) {
        return new Color(43 + i, 46 + i, 66 + i);
    }
}
