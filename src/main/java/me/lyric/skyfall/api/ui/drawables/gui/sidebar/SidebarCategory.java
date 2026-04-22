package me.lyric.skyfall.api.ui.drawables.gui.sidebar;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.Drawable;
import me.lyric.skyfall.api.ui.drawables.gui.screens.impl.DefaultScreen;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import me.lyric.skyfall.api.utils.shader.ShadowShader;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class SidebarCategory implements Drawable {
    private final ResourceLocation resourceLocation;
    private final float textureSize = 20.0f;
    private final Category category;
    private final Sidebar sidebar;
    public float x, y, sidebarWidth;
    private float hoverAnim;

    public SidebarCategory(Sidebar bar, Category category) {
        resourceLocation = new ResourceLocation("textures/icons/categories/" + category.toString().toLowerCase() + ".png");
        this.category = category;
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
        hoverAnim = MathsUtils.lerp(hoverAnim, Interface.getActiveCategory() == category ? 1.0f : inside(mouseX, mouseY) ? 0.7f : 0.0f, Interface.getDelta());

        /* Text */
        Managers.TEXT.guiString(category.toString(), x + textureSize + 20.0f - (30.0f * (sidebarWidth / 100.0f - 0.5f)), y + textureSize / 2.0f - Managers.TEXT.stringHeight() / 2.0f, Color.WHITE);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && inside(mouseX, mouseY) && Interface.getActiveCategory() != category) {
            Interface.setActiveCategory(category);
            DefaultScreen.setActiveFeature(null);
            Interface.selectedScreen = "Default";
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    public boolean inside(int mouseX, int mouseY) {
        if (!sidebar.open) {
            return mouseX > x && mouseX < x + textureSize && mouseY > y && mouseY < y + textureSize;
        }

        return mouseX > x && mouseX < x + sidebar.sidebarWidth && mouseY > y && mouseY < y + textureSize;
    }
}
