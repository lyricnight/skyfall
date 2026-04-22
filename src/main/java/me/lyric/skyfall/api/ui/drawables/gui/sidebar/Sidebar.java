package me.lyric.skyfall.api.ui.drawables.gui.sidebar;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.Drawable;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import me.lyric.skyfall.api.utils.shader.ShadowShader;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;

public class Sidebar implements Drawable {
    private final ResourceLocation resourceLocation = new ResourceLocation("textures/icons/other/menu.png");
    private final ArrayList<SidebarCategory> sidebarCategories = new ArrayList<>();
    private final ArrayList<SidebarItem> sidebarItems = new ArrayList<>();
    public float sidebarWidth = 50.0f;
    public float x, y, height;
    private float hoverAnim;
    public boolean open;

    public Sidebar() {
        for (Category category : Category.values()) {
            sidebarCategories.add(new SidebarCategory(this, category));
        }
        sidebarItems.add(new SidebarItem(this, "Configs"));
        sidebarItems.add(new SidebarItem(this, "HudEditor"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        /* Sidebar */
        RenderUtils.rounded(x, y, x + sidebarWidth, y + height, 10.0f, sidebarColor());

        /* Sidebar Shadow */
        ShadowShader.shadow(5, 1, () -> RenderUtils.rounded(x, y, x + sidebarWidth, y + height, 10.0f, Color.WHITE));

        /* Menu icon */
        float textureSize = 21.0f, textureOffset = 5.0f;
        Color color = new Color(255 - ((int) ((255 - Interface.primary().getRed()) * hoverAnim)), 255 - ((int) ((255 - Interface.primary().getGreen()) * hoverAnim)), 255 - ((int) ((255 - Interface.primary().getBlue()) * hoverAnim)));
        RenderUtils.textureSmooth(x + 15.5f, y + textureOffset, x + 15.5f + textureSize, y + textureOffset + textureSize, color, resourceLocation);

        /* Icon shadow */
        ShadowShader.shadow(1, 1, () -> RenderUtils.textureSmooth(x + 15.5f, y + textureOffset, x + 15.5f + textureSize, y + textureOffset + textureSize, Color.WHITE, resourceLocation));

        /* Color for Menu icon */
        hoverAnim = MathsUtils.lerp(hoverAnim, insideMenuIcon(mouseX, mouseY) ? 1.0f : 0.0f, Interface.getDelta());

        /* Setup & render SidebarCategories */
        float deltaY = 45.0f;
        for (SidebarCategory sidebarCategory : sidebarCategories) {
            sidebarCategory.x = x + 15.0f;
            sidebarCategory.y = y + deltaY;
            sidebarCategory.sidebarWidth = sidebarWidth;

            /* Scissor SidebarCategories */
            RenderUtils.prepareScissor(x, y, x + sidebarWidth, y + height);

            sidebarCategory.drawScreen(mouseX, mouseY, partialTicks);

            /* Release Scissor */
            RenderUtils.releaseScissor();
            deltaY += 30.0f;
        }

        deltaY += 10.0f;
        for (SidebarItem sidebarItem : sidebarItems) {
            sidebarItem.x = x + 15.0f;
            sidebarItem.y = y + deltaY;
            sidebarItem.sidebarWidth = sidebarWidth;
            /* Scissor SidebarCategories */
            RenderUtils.prepareScissor(x, y, x + sidebarWidth, y + height);

            sidebarItem.drawScreen(mouseX, mouseY, partialTicks);

            /* Release Scissor */
            RenderUtils.releaseScissor();
            deltaY += 30.0f;
        }


        /* Set width */
        sidebarWidth = MathsUtils.lerp(sidebarWidth, open ? 100.0f : 50.0f, Interface.getDelta());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (insideMenuIcon(mouseX, mouseY)) {
                open = !open;
                return;
            } else if (open && !insideMenu(mouseX, mouseY)) {
                open = false;
                return;
            }
        }

        for (SidebarCategory sidebarCategory : sidebarCategories) {
            sidebarCategory.mouseClicked(mouseX, mouseY, mouseButton);
        }

        for (SidebarItem sidebarItem : sidebarItems) {
            sidebarItem.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    /* Inside Menu icon */
    private boolean insideMenuIcon(int mouseX, int mouseY) {
        float textureSize = 21.0f, textureOffset = 5.0f;
        return mouseX > x + 15.0f && mouseX < x + sidebarWidth - 15.0f && mouseY > y + textureOffset && mouseY < y + textureOffset + textureSize;
    }

    /* Inside Menu */
    private boolean insideMenu(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + sidebarWidth && mouseY > y && mouseY < y + height;
    }

    @SuppressWarnings("JavaExistingMethodCanBeUsed")
    public Color sidebarColor() {
        Color get = Interface.background();
        if (get.getRed() + 20 > 255 || get.getGreen() + 20 > 255 || get.getBlue() + 27 > 255) {
            return get;
        }
        return new Color(get.getRed() + 20, get.getGreen() + 20, get.getBlue() + 27);
    }
}
