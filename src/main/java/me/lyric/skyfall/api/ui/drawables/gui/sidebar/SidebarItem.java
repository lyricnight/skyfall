package me.lyric.skyfall.api.ui.drawables.gui.sidebar;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.Drawable;
import me.lyric.skyfall.api.ui.drawables.gui.screens.impl.DefaultScreen;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import me.lyric.skyfall.api.utils.shader.ShadowShader;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class SidebarItem implements Drawable {
    private final ResourceLocation resourceLocation;
    private final float textureSize = 20.0f;
    public float x, y, sidebarWidth;
    private final Sidebar sidebar;
    private final String alias;
    private float hoverAnim;

    public SidebarItem(Sidebar bar, String alias) {
        resourceLocation = new ResourceLocation("textures/icons/other/" + alias.toLowerCase() + ".png");
        this.alias = alias;
        this.sidebar = bar;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        /* Render icon */
        Color color = new Color(255 - ((int) ((255 - Interface.primary().getRed()) * hoverAnim)), 255 - ((int) ((255 - Interface.primary().getGreen()) * hoverAnim)), 255 - ((int) ((255 - Interface.primary().getBlue()) * hoverAnim)));
        RenderUtils.textureSmooth(x, y, x + textureSize, y + textureSize, color, resourceLocation);

        /* Icon shadow */
        ShadowShader.shadow(1, 1, () -> RenderUtils.textureSmooth(x, y, x + textureSize, y + textureSize, Color.WHITE, resourceLocation));

        /* Color for Menu icon */
        hoverAnim = MathsUtils.lerp(hoverAnim, Interface.selectedScreen.equals(alias) ? 1.0f : inside(mouseX, mouseY) ? 0.7f : 0.0f, Interface.getDelta());

        /* Text */
        Managers.TEXT.guiString(alias, x + textureSize + 20.0f - (30.0f * (sidebarWidth / 100.0f - 0.5f)), y + textureSize / 2.0f - Managers.TEXT.stringHeight() / 2.0f, Color.WHITE);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && inside(mouseX, mouseY) && !Interface.selectedScreen.equals(alias)) {
            Interface.setActiveCategory(null);
            DefaultScreen.setActiveFeature(null);
            Interface.selectedScreen = alias;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    public boolean inside(int mouseX, int mouseY) {
        if (!sidebar.open) {
            return mouseX > x && mouseX < x + textureSize && mouseY > y && mouseY < y + textureSize;
        }

        return mouseX > x && mouseX < x + sidebar.sidebarWidth && mouseY > y && mouseY < y + textureSize;
    }
}
