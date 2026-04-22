package me.lyric.skyfall.api.ui;

import lombok.Getter;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.ui.drawables.gui.screens.GUISize;
import me.lyric.skyfall.api.ui.drawables.gui.screens.impl.ConfigScreen;
import me.lyric.skyfall.api.ui.drawables.gui.screens.impl.DefaultScreen;
import me.lyric.skyfall.api.ui.drawables.gui.screens.impl.HUDEditorScreen;
import me.lyric.skyfall.api.ui.drawables.gui.sidebar.Sidebar;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import me.lyric.skyfall.api.utils.shader.ShadowShader;
import me.lyric.skyfall.impl.event.mc.ScrollEvent;
import me.lyric.skyfall.impl.event.ui.SkyfallUIClosedEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class Interface extends GuiScreen {
    private final ResourceLocation logo = new ResourceLocation("textures/icons/other/logo.png"), searchIcon = new ResourceLocation("textures/icons/other/search.png");
    @Getter
    private static Category activeCategory = Category.Internals;
    private final DefaultScreen defaultScreen = new DefaultScreen();
    private final ConfigScreen configScreen = new ConfigScreen();
    private final HUDEditorScreen hudEditorScreen = new HUDEditorScreen();

    private final Sidebar sidebar = new Sidebar();
    private static long delta, lastFrame;
    private float x, y, searchCol;
    public static String selectedScreen = "Default";
    public static String search = "";
    private boolean searching;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        cleanRendering();

        /* Setup x and y */
        x = width / 2.0f - GUISize.guiWidth / 2.0f;
        y = height / 2.0f - GUISize.guiHeight / 2.0f;

        /* Background */
        RenderUtils.rounded(x, y, x + GUISize.guiWidth, y + GUISize.guiHeight, 10.0f, background());

        /* Background Shadow */
        ShadowShader.shadow(40, 1, () -> RenderUtils.rounded(x, y, x + GUISize.guiWidth, y + GUISize.guiHeight, 10.0f, background()));

        /* Icon */
        float textureSize = 50.0f;
        RenderUtils.textureSmooth(x + GUISize.sidebarWidth + 15.0f, y - 25.0f, x + GUISize.sidebarWidth + 15.0f + textureSize, y + 5.0f + textureSize, Color.WHITE, logo);

        /* Icon Shadow */
        ShadowShader.shadow(1, 1, () -> RenderUtils.textureSmooth(x + GUISize.sidebarWidth + 15.0f, y + 5.0f, x + GUISize.sidebarWidth + 15.0f + textureSize, y + 5.0f + textureSize, Color.WHITE, logo));

        /* Watermark */
        // originally:         Managers.TEXT.guiString(Skyfall.NAME, (x + sidebarWidth + 20.0f + textureSize) / scale, (y + 17.5f - Managers.TEXT.stringHeight()) / scale, Color.WHITE);
        Managers.TEXT.guiStringLarge(Skyfall.NAME, (x + GUISize.sidebarWidth + 20.0f + textureSize), (y + 17.5f - Managers.TEXT.stringHeight()), Color.WHITE);
        Managers.TEXT.guiString(Skyfall.VERSION, x + GUISize.sidebarWidth + 21.5f + Managers.TEXT.stringWidth(Skyfall.NAME) * 1.5f + textureSize, y + 21.0f - Managers.TEXT.stringHeight(), Color.WHITE);

        /* Search bar */
        RenderUtils.rounded(x + GUISize.guiWidth - 150.0f, y + 5.0f, x + GUISize.guiWidth - 10.0f, y + 25.0f, 10.0f, shade(2));

        /* Search bar Shadow */
        ShadowShader.shadow(5, 1, () -> RenderUtils.rounded(x + GUISize.guiWidth - 150.0f, y + 5.0f, x + GUISize.guiWidth - 10.0f, y + 25.0f, 10.0f, Color.WHITE));

        /* Search icon */
        RenderUtils.textureSmooth(x + GUISize.guiWidth - 30.0f, y + 10.0f, x + GUISize.guiWidth - 20.0f, y + 20.0f, new Color(0, 0, 0, 50), searchIcon);

        /* Search Text */
        searchCol = MathsUtils.lerp(searchCol, search.isEmpty() ? 0.5f : 1.0f, getDelta());
        String text = ((search.isEmpty() && !searching) ? "Search" : search) + (searching ? typingIcon() : "");

        /* Search Scissor */
        RenderUtils.prepareScissor(x, y, x + GUISize.guiWidth - 30.0f, y + height);
        Managers.TEXT.string(text, x + GUISize.guiWidth - 140.0f, y + 17.5f - Managers.TEXT.stringHeight(), new Color(searchCol, searchCol, searchCol, searchCol));

        /* Release Search scissor */
        RenderUtils.releaseScissor();

        /* ConfigScreen */
        configScreen.x = x;
        configScreen.y = y;
        configScreen.drawScreen(mouseX, mouseY, partialTicks);

        /* Default Screen */
        defaultScreen.x = x;
        defaultScreen.y = y;
        defaultScreen.drawScreen(mouseX, mouseY, partialTicks);

        /* Hud Screen */
        hudEditorScreen.x = x;
        hudEditorScreen.y = y;
        hudEditorScreen.drawScreen(mouseX, mouseY, partialTicks);

        /* Sidebar */
        sidebar.x = x + 5.0f;
        sidebar.y = y + 5.0f;
        sidebar.height = GUISize.guiHeight - 10.0f;
        sidebar.drawScreen(mouseX, mouseY, partialTicks);

        /* Delta time */
        delta = System.currentTimeMillis() - lastFrame;
        lastFrame = System.currentTimeMillis();

        int wheel = Mouse.getDWheel();
        if (wheel != 0){
            ScrollEvent event = new ScrollEvent(mouseX, mouseY, wheel);
            EventBus.getInstance().post(event);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        sidebar.mouseClicked(mouseX, mouseY, mouseButton);
        defaultScreen.mouseClicked(mouseX, mouseY, mouseButton);
        configScreen.mouseClicked(mouseX, mouseY, mouseButton);
        hudEditorScreen.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            searching = insideSearch(mouseX, mouseY);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searching) {
            search = type(search, typedChar, keyCode);
        }

        defaultScreen.keyTyped(typedChar, keyCode);
        configScreen.keyTyped(typedChar, keyCode);

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        SkyfallUIClosedEvent event = new SkyfallUIClosedEvent();
        EventBus.getInstance().post(event);
    }

    public String type(String string, char typedChar, int keyCode) {
        String newString = string;
        switch (keyCode) {
            case Keyboard.KEY_BACK:
                if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    newString = "";
                }
                if (!newString.isEmpty()) {
                    newString = newString.substring(0, newString.length() - 1);
                }
                break;
            case Keyboard.KEY_RETURN:
                searching = false;
                break;
            default:
                if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                    newString = newString + typedChar;
                    break;
                }
        }
        return newString;
    }

    private boolean insideSearch(int mouseX, int mouseY) {
        return mouseX > x + GUISize.guiWidth - 150.0f && mouseX < x + GUISize.guiWidth - 10.0f && mouseY > y + 5.0f && mouseY < y + 25.0f;
    }

    private String typingIcon() {
        if (System.currentTimeMillis() / 500 % 2 == 0) {
            return "_";
        }
        return "";
    }

    public static Color primary() {
        return Managers.FEATURES.get(me.lyric.skyfall.impl.feature.internals.Interface.class).colourSetting.getValue();
    }

    public static Color background() {
        return Managers.FEATURES.get(me.lyric.skyfall.impl.feature.internals.Interface.class).background.getValue();
    }

    public static Color shade(int i) {
        Color get = background();
        boolean flag = get.getRed() + i > 255 || get.getBlue() + i > 255 || get.getGreen() + i > 255 || get.getRed() + i < 0 || get.getBlue() + i < 0 || get.getGreen() + i < 0;
        if (flag)
        {
            return background();
        }
        return new Color(get.getRed() + i, get.getGreen() + i, get.getBlue() + i);
    }

    public static Color shade(int i, int alpha) {
        Color get = background();
        boolean flag = get.getRed() + i > 255 || get.getBlue() + i > 255 || get.getGreen() + i > 255 || get.getRed() + i < 0 || get.getBlue() + i < 0 || get.getGreen() + i < 0;
        if (flag)
        {
            return new Color(get.getRed(), get.getGreen(), get.getBlue(), alpha);
        }
        return new Color(get.getRed() + i, get.getGreen() + i, get.getBlue() + i, alpha);
    }

    public static void setActiveCategory(Category activeCategory) {
        Interface.activeCategory = activeCategory;
    }

    public static float getDelta() {
        return delta * 0.01f;
    }

    /**
     * this method cleans the entire rendering stack for 2D rendering - FORCES everything reset
     */
    private void cleanRendering()
    {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(this.width, 0, this.zLevel).color(0.0f, 0.0f, 0.0f, 0.0f).endVertex();
        worldRenderer.pos(0, 0, this.zLevel).color(0.0f, 0.0f, 0.0f, 0.0f).endVertex();
        worldRenderer.pos(0, this.height, this.zLevel).color(0.0f, 0.0f, 0.0f, 0.0f).endVertex();
        worldRenderer.pos(this.width, this.height, this.zLevel).color(0.0f, 0.0f, 0.0f, 0.0f).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}
