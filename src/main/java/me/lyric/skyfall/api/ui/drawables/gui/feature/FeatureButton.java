package me.lyric.skyfall.api.ui.drawables.gui.feature;

import lombok.Getter;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.Drawable;
import me.lyric.skyfall.api.ui.drawables.gui.screens.impl.DefaultScreen;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class FeatureButton implements Drawable {
    private final Feature feature;
    private final ResourceLocation icon;
    public float deltaX, deltaXTarget, height, width, hover, guiY, alpha, c, guiX, guiWidth;
    @Getter
    public float x;
    @Getter
    public float y;
    private boolean binding = false;

    public FeatureButton(Feature feature, float deltaX) {
        this.feature = feature;
        this.icon = new ResourceLocation(
                "textures/icons/categories/" + feature.getCategory().toString().toLowerCase() + "/" + feature.getName().toLowerCase().replace(" ", "") + ".png");
        this.deltaX = deltaX;
        this.deltaXTarget = deltaX;
    }

    public void background() {
        /* Blur & Shadow */
        RenderUtils.rounded(x, y, x + width, y + height, 7.0f, Color.WHITE);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        deltaX = MathsUtils.lerp(deltaX, deltaXTarget, Interface.getDelta());
        float barY = y + height - 15.0f;

        /* Bottom bar */
        RenderUtils.setupDefault(Interface.shade(3));
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(x, barY);
        RenderUtils.corner(x, barY, x + width, y + height, 7.0f, 2);
        RenderUtils.corner(x, barY, x + width, y + height, 7.0f, 3);
        glVertex2f(x + width, barY);
        RenderUtils.releaseDefault();


        /* Name */
        String text = feature.getName();
        Managers.TEXT.guiString(text, x + width / 2.0f - Managers.TEXT.stringWidth(text) / 2.0f, y + 5.0f, Color.WHITE);

        /* Icon */
        RenderUtils.textureSmooth(x + width / 2.0f - 14.0f, y + 16.0f, x + width / 2.0f + 16.0f, y + 46.0f, new Color(0, 0, 0, 50), icon);
        RenderUtils.textureSmooth(x + width / 2.0f - 15.0f, y + 15.0f, x + width / 2.0f + 15.0f, y + 45.0f, Color.WHITE, icon);

        hover = MathsUtils.lerp(hover, insideKey(mouseX, mouseY) ? 0.2f : 0.0f, Interface.getDelta());

        /* Key background */
        RenderUtils.setupDefault(Interface.shade(5));
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(x, barY);
        RenderUtils.corner(x, barY, x + 40.0f, y + height, 7.0f, 2);
        glVertex2f(x + 40.0f, y + height);
        glVertex2f(x + 40.0f, barY);
        RenderUtils.releaseDefault();

        /* Hover key */
        RenderUtils.setupDefault(new Color(0.0f, 0.0f, 0.0f, hover));
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(x, barY);
        RenderUtils.corner(x, barY, x + 40.0f, y + height, 7.0f, 2);
        glVertex2f(x + 40.0f, y + height);
        glVertex2f(x + 40.0f, barY);
        RenderUtils.releaseDefault();

        /* Enabled background */
        RenderUtils.rounded(x + width - 30.0f, barY + 2.5f, x + width - 5.0f, y + height - 2.5f, 5.0f, Interface.shade(5));
        RenderUtils.roundedOutline(x + width - 30.0f, barY + 2.5f, x + width - 5.0f, y + height - 2.5f, 5.0f, Interface.shade(-2));

        /* Circle */
        alpha = MathsUtils.lerp(alpha, feature.getEnabled().getValue() ? insideEnabled(mouseX, mouseY) ? 0.7f : 1.0f : insideEnabled(mouseX, mouseY) ? 0.7f : 0.4f, Interface.getDelta());
        c = MathsUtils.lerp(c, feature.getEnabled().getValue() ? 1.0f : 0.0f, Interface.getDelta());
        Color p = Interface.primary();
        Color color = new Color(p.getRed() / 255.0f, p.getGreen() / 255.0f, p.getBlue() / 255.0f, alpha);
        RenderUtils.circle(x + width - 26.0f + (15.0f * c), barY + 5.5f, 4.0f, color);

        /* Prepare Scissor */
        RenderUtils.prepareScissor(Math.min(guiWidth, Math.max(x, guiX)), guiY, Math.min(guiWidth, Math.max(x + 40.0f, guiX)), Math.max(guiY, y + height));

        /* Key Text */
        String keyText = binding ? dots() : feature.getKeybind().getValue() == Keyboard.KEY_NONE ? "None" : Keyboard.getKeyName(feature.getKeybind().getValue());
        float textWidth = Managers.TEXT.stringWidth(keyText);
        float textHeight = Managers.TEXT.stringHeight();
        Managers.TEXT.guiStringSmall(keyText, (x + 20.0f - textWidth / 2.0f), (barY + textHeight), Color.WHITE);

        /* Release Scissor */
        RenderUtils.releaseScissor();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (insideKey(mouseX, mouseY)) {
                binding = !binding;
            }
            if (insideEnabled(mouseX, mouseY)) {
                feature.toggle();
            }
            if (outsideBar(mouseX, mouseY)) {
                DefaultScreen.setActiveFeature(feature);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (binding) {
            if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_ESCAPE) {
                feature.getKeybind().invokeValue(Keyboard.KEY_NONE);
                binding = false;
            } else if (keyCode == Keyboard.KEY_RETURN) {
                binding = false;
            } else {
                feature.getKeybind().invokeValue(keyCode);
                binding = false;
            }
        }
    }

    private boolean outsideBar(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width & mouseY > y && mouseY < y + height - 15.0f;

    }

    private boolean insideEnabled(int mouseX, int mouseY) {
        return mouseX > x + width - 30.0f && mouseX < x + width - 5.0f & mouseY > y + height - 12.5f && mouseY < y + height - 2.5f;
    }

    private boolean insideKey(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + 40.0f & mouseY > y + height - 15.0f && mouseY < y + height;
    }

    private long sys = 0L;

    private String dots() {
        float diff = System.currentTimeMillis() - sys;
        if (diff > 1333) {
            sys = System.currentTimeMillis();
            return "...";
        }
        if (diff > 999) {
            return "...";
        }
        if (diff > 666) {
            return "..";
        }
        if (diff > 333) {
            return ".";
        }
        return "";
    }

    public Feature getModule() {
        return feature;
    }
}
