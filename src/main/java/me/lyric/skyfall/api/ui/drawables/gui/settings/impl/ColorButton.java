package me.lyric.skyfall.api.ui.drawables.gui.settings.impl;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.ColourSetting;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.gui.feature.tab.FeatureTab;
import me.lyric.skyfall.api.ui.drawables.gui.screens.impl.DefaultScreen;
import me.lyric.skyfall.api.ui.drawables.gui.settings.SettingDrawable;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

public class ColorButton extends SettingDrawable {
    private final ColourSetting setting;
    private final ResourceLocation
            copy = new ResourceLocation("textures/icons/other/copy.png"),
            paste = new ResourceLocation("textures/icons/other/paste.png");

    private final Slider[] sliders;
    private float anim,
            sat, bright, hue, alpha,
            copyAnim, pasteAnim;
    public boolean open;
    private boolean hexTyping = false;
    private String hexText;
    private boolean isDraggingPicker = false;
    private float initialHue;

    public ColorButton(ColourSetting setting) {
        super(setting);
        this.setting = setting;
        this.sliders = new Slider[]{
                new Slider(0),
                new Slider(1),
                new Slider(2)
        };
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (visibleAnim < 0.01f) return;
        Managers.TEXT.guiString(setting.getName(), x, y + 7.5f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, 0.4f));

        RenderUtils.rounded(x + width - 12.5f, y + 2.5f, x + width - 2.5f, y + 12.5f, 5.0f, setting.getValue());
        RenderUtils.roundedOutline(x + width - 12.5f, y + 2.5f, x + width - 2.5f, y + 12.5f, 5.0f, FeatureTab.shade(-3));

        anim = MathsUtils.lerp(anim, open ? 1.0f : 0.0f, Interface.getDelta());

        RenderUtils.prepareScissor(Math.max(guiX, Math.min(x, guiX + guiWidth)), Math.max(guiY, Math.min(guiY + guiHeight, y)), Math.max(guiX, Math.min(x + width, guiX + guiWidth)), Math.max(guiY, Math.min(guiY + guiHeight, y + height - 2.5f)));

        renderPicker();

        renderHue();

        renderAlpha();

        renderSliders(mouseX, mouseY);

        renderCopyPasteHex(mouseX, mouseY);

        RenderUtils.releaseScissor();

        if (open && Mouse.isButtonDown(0) && visibleAnim > 0.9f) {
            if (insideAny(mouseY)) {
                if (insidePicker(mouseX)) {
                    if (!isDraggingPicker) {
                        initialHue = hue(setting.getValue());
                        isDraggingPicker = true;
                    }
                    updatePicker(mouseX, mouseY);
                }
                if (insideHue(mouseX)) {
                    updateHue(mouseY);
                }
                if (insideAlpha(mouseX)) {
                    updateAlpha(mouseY);
                }
            }
        } else {
            isDraggingPicker = false;
        }

        height = 15 + (anim * 60.0f);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (insideColor(mouseX, mouseY)) {
                open = !open;
            }
            if (open) {
                if (insidePaste(mouseX, mouseY)) {
                    try {
                        String text = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
                        if (text.contains(":")) {
                            String hex = text.split(":")[0];
                            String alpha = text.split(":")[1];
                            Color prev = setting.getValue();
                            try {
                                setting.invokeValue(Color.decode(hex));
                                setting.invokeValue(new Color(setting.getValue().getRed(), setting.getValue().getGreen(), setting.getValue().getBlue(), Integer.parseInt(alpha)));
                            } catch (Exception e) {
                                setting.invokeValue(prev);
                                ExceptionHandler.handle(e);
                            }
                        } else {
                            Color prev = setting.getValue();
                            try {
                                setting.invokeValue(Color.decode(text));
                                setting.invokeValue(new Color(setting.getValue().getRed(), setting.getValue().getGreen(), setting.getValue().getBlue(), 255));
                            } catch (Exception e) {
                                setting.invokeValue(prev);
                                ExceptionHandler.handle(e);
                            }
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }
                if (insideCopy(mouseX, mouseY)) {
                    String hex = String.format("#%06x", setting.getValue().getRGB() & 0xFFFFFF) + ":" + setting.getValue().getAlpha();
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(hex), null);
                }
                if (insideHex(mouseX, mouseY)) {
                    hexText = String.format("#%06x", setting.getValue().getRGB() & 0xFFFFFF);
                    hexTyping = !hexTyping;
                    lastTime = System.currentTimeMillis();
                    cursorVisible = true;
                } else if (hexTyping) {
                    hexFinish();
                    hexTyping = false;
                }
            }
        }
        if (open) {
            for (Slider slider : sliders) {
                slider.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        for (Slider slider : sliders) {
            slider.keyTyped(typedChar, keyCode);
        }
        if (hexTyping) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (!hexText.isEmpty() && hexText.length() != 1) {
                    hexText = hexText.substring(0, hexText.length() - 1);
                }
                lastTime = System.currentTimeMillis();
                cursorVisible = true;
                return;
            }
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) {
                hexFinish();
                return;
            }
            if (hexText.length() < 7) {
                if (Character.digit(typedChar, 16) != -1) {
                    hexText += typedChar;
                    lastTime = System.currentTimeMillis();
                    cursorVisible = true;
                }
            }
        }
    }

    private void hexFinish() {
        hexTyping = false;
        if (hexText.length() != 7) return;
        hexText = hexText.replace("#", "");

        int r = Integer.parseInt(hexText.substring(0, 2), 16);
        int g = Integer.parseInt(hexText.substring(2, 4), 16);
        int b = Integer.parseInt(hexText.substring(4, 6), 16);

        setting.invokeValue(new Color(r, g, b));
    }

    private boolean insideAny(int mouseY) {
        return mouseY > y + 15.0f && mouseY < y + 75.0f;
    }

    private boolean insidePicker(int mouseX) {
        return mouseX > x && mouseX < x + width / 2.0f + 2.5f;
    }

    private boolean insideHue(int mouseX) {
        return mouseX > x + width / 2.0f + 5.0f && mouseX < x + width / 2.0f + 10.0f;
    }

    private boolean insideAlpha(int mouseX) {
        return mouseX > x + width / 2.0f + 15.0f && mouseX < x + width / 2.0f + 20.0f;
    }

    private void updatePicker(int mouseX, int mouseY) {
        float pickerWidth = (x + width / 2.0f) - (x + 2.5f);
        float pickerHeight = 55.0f;
        float xDiff = (x + width / 2.0f) - mouseX;
        float yDiff = (y + 72.5f) - mouseY;
        float xMultiplier = xDiff / pickerWidth;
        float yMultiplier = yDiff / pickerHeight;
        float saturation = Math.min(1.0f, Math.max(0.0f, 1.0f - xMultiplier));
        float brightness = Math.min(1.0f, Math.max(0.0f, yMultiplier));

        setting.invokeValue(Color.getHSBColor(initialHue, saturation, brightness));
    }

    private void updateHue(int mouseY) {
        float pickerHeight = 55.0f;
        float yDiff = (y + 72.5f) - mouseY;
        float yMultiplier = yDiff / pickerHeight;
        float hue = Math.min(1.0f, Math.max(0.0f, yMultiplier));
        setting.invokeValue(Color.getHSBColor(1.0f - hue, saturation(setting.getValue()), brightness(setting.getValue())));
    }

    private void updateAlpha(int mouseY) {
        float pickerHeight = 55.0f;
        float yDiff = (y + 72.5f) - mouseY;
        float yMultiplier = yDiff / pickerHeight;
        float alpha = 1.0f - Math.min(1.0f, Math.max(0.0f, yMultiplier));
        setting.invokeValue(new Color(setting.getValue().getRed() / 255.0f, setting.getValue().getGreen() / 255.0f, setting.getValue().getBlue() / 255.0f, alpha));
    }

    private void renderPicker() {
        if (!isDraggingPicker) {
            initialHue = hue(setting.getValue());
        }

        Color hueColor = Color.getHSBColor(initialHue, 1.0f, 1.0f);

        RenderUtils.picker(x + 2.5f, y + 17.5f, x + width / 2.0f, y + 72.5f, hueColor); // Pass hue color instead of current color
        RenderUtils.roundedOutline(x + 2.5f, y + 17.5f, x + width / 2.0f, y + 72.5f, 0.0f, FeatureTab.shade(-3));

        float pickerWidth = (x + width / 2.0f) - (x + 2.5f);
        float pickerHeight = 55.0f;

        sat = MathsUtils.lerp(sat, saturation(setting.getValue()), Interface.getDelta());
        bright = MathsUtils.lerp(bright, brightness(setting.getValue()), Interface.getDelta());

        RenderUtils.circle(x + 2.5f + pickerWidth * sat - 1.25f, y + 17.5f + pickerHeight * (1.0f - bright) - 1.25f, 2.5f, new Color(0, 0, 0, 50));
        RenderUtils.circle(x + 2.5f + pickerWidth * sat - 1.0f, y + 17.5f + pickerHeight * (1.0f - bright) - 1.0f, 2.0f, Interface.primary());
    }

    private void renderHue() {
        for (int i = 0; i < 6; i++) {
            Color previousStep = new Color(Color.HSBtoRGB(i / 6.0f, 1.0f, 1.0f));
            Color nextStep = new Color(Color.HSBtoRGB((i + 1.0f) / 6.0f, 1.0f, 1.0f));
            RenderUtils.gradient(
                    x + width / 2.0f + 5.0f,
                    y + 17.5f + i * (55.0f / 6.0f),
                    x + width / 2.0f + 10.0f,
                    y + 17.5f + (i + 1) * (55.0f / 6.0f),
                    previousStep,
                    previousStep,
                    nextStep,
                    nextStep
            );
        }
        RenderUtils.roundedOutline(x + width / 2.0f + 5.0f, y + 17.5f, x + width / 2.0f + 10.0f, y + 72.5f, 0.0f, FeatureTab.shade(-3));

        float pickerHeight = 55.0f;

        hue = MathsUtils.lerp(hue, hue(setting.getValue()), Interface.getDelta());

        RenderUtils.rect(x + width / 2.0f + 4.0f, y + 17.5f + pickerHeight * hue - 1.0f, x + width / 2.0f + 11.0f, y + 17.5f + pickerHeight * hue + 1.0f, Interface.primary());
        RenderUtils.roundedOutline(x + width / 2.0f + 4.0f, y + 17.5f + pickerHeight * hue - 1.0f, x + width / 2.0f + 11.0f, y + 17.5f + pickerHeight * hue + 1.0f, 0.0f, FeatureTab.shade(-3));
    }

    private void renderAlpha() {
        boolean white = false;
        for (int i = 0; i < 22; i++) {
            white = !white;
            RenderUtils.rect(x + width / 2.0f + 15.0f, y + 17.5f + i * 2.5f, x + width / 2.0f + 17.5f, y + 20.0f + i * 2.5f, white ? Color.WHITE : Color.GRAY);
        }
        white = true;
        for (int i = 0; i < 22; i++) {
            white = !white;
            RenderUtils.rect(x + width / 2.0f + 17.5f, y + 17.5f + i * 2.5f, x + width / 2.0f + 20.0f, y + 20.0f + i * 2.5f, white ? Color.WHITE : Color.GRAY);
        }
        RenderUtils.gradient(x + width / 2.0f + 15.0f, y + 17.5f, x + width / 2.0f + 20.0f, y + 72.5f, new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), Color.BLACK, Color.BLACK);
        RenderUtils.roundedOutline(x + width / 2.0f + 15.0f, y + 17.5f, x + width / 2.0f + 20.0f, y + 72.5f, 0.0f, FeatureTab.shade(-3));

        float pickerHeight = 55.0f;
        alpha = MathsUtils.lerp(alpha, setting.getValue().getAlpha() / 255.0f, Interface.getDelta());

        RenderUtils.rect(x + width / 2.0f + 14.0f, y + 17.5f + pickerHeight * alpha - 1.0f, x + width / 2.0f + 21.0f, y + 17.5f + pickerHeight * alpha + 1.0f, Interface.primary());
        RenderUtils.roundedOutline(x + width / 2.0f + 14.0f, y + 17.5f + pickerHeight * alpha - 1.0f, x + width / 2.0f + 21.0f, y + 17.5f + pickerHeight * alpha + 1.0f, 0.0f, FeatureTab.shade(-3));
    }

    private void renderSliders(int mouseX, int mouseY) {
        for (Slider slider : sliders) {
            slider.x = x + width / 2.0f + 25.0f;
            slider.y = y + 17.5f + slider.i * 10.0f;
            slider.height = 10.0f;
            slider.width = (x + width) - (x + width / 2.0f + 25.0f);
            slider.drawScreen(mouseX, mouseY);
        }
    }

    private void renderCopyPasteHex(int mouseX, int mouseY) {
        RenderUtils.rounded(x + width / 2.0f + 25.0f, y + 52.5f, x + width / 2.0f + 40.0f, y + 67.5f, 3.0f, FeatureTab.shade(5));
        RenderUtils.roundedOutline(x + width / 2.0f + 25.0f, y + 52.5f, x + width / 2.0f + 40.0f, y + 67.5f, 3.0f, FeatureTab.shade(-3));
        RenderUtils.textureSmooth(x + width / 2.0f + 27.5f, y + 55.0f, x + width / 2.0f + 37.5f, y + 65.0f, new Color(1.0f, 1.0f, 1.0f, copyAnim), copy);

        RenderUtils.rounded(x + width / 2.0f + 45.0f, y + 52.5f, x + width / 2.0f + 60.0f, y + 67.5f, 3.0f, FeatureTab.shade(5));
        RenderUtils.roundedOutline(x + width / 2.0f + 45.0f, y + 52.5f, x + width / 2.0f + 60.0f, y + 67.5f, 3.0f, FeatureTab.shade(-3));
        RenderUtils.textureSmooth(x + width / 2.0f + 47.5f, y + 55.0f, x + width / 2.0f + 57.5f, y + 65.0f, new Color(1.0f, 1.0f, 1.0f, pasteAnim), paste);

        copyAnim = MathsUtils.lerp(copyAnim, insideCopy(mouseX, mouseY) ? 1.0f : 0.4f, Interface.getDelta());
        pasteAnim = MathsUtils.lerp(pasteAnim, insidePaste(mouseX, mouseY) ? 1.0f : 0.4f, Interface.getDelta());

        String hex = hexTyping ? hexText + getCursor() : String.format("#%06x", setting.getValue().getRGB() & 0xFFFFFF);
        RenderUtils.rounded(x + width / 2.0f + 65.0f, y + 52.5f, x + width, y + 67.5f, 3.0f, FeatureTab.shade(5));
        RenderUtils.roundedOutline(x + width / 2.0f + 65.0f, y + 52.5f, x + width, y + 67.5f, 3.0f, FeatureTab.shade(-3));

        float scale = 0.8f;
        RenderUtils.invokeScale(scale);
        Managers.TEXT.guiString(hex, (x + width / 2.0f + 65.0f + ((width) - (width / 2.0f + 65.0f)) / 2.0f - Managers.TEXT.stringWidth(hex) * scale / 2.0f) / scale, (y + 60.0f - Managers.TEXT.stringHeight() * scale / 2.0f) / scale, Color.WHITE);
        RenderUtils.resetScale();
    }

    private boolean insidePaste(int mouseX, int mouseY) {
        return mouseX > x + width / 2.0f + 45.0f && mouseX < x + width / 2.0f + 60.0f && mouseY > y + 52.5f && mouseY < y + 67.5f;
    }

    private boolean insideCopy(int mouseX, int mouseY) {
        return mouseX > x + width / 2.0f + 25.0f && mouseX < x + width / 2.0f + 40.0f && mouseY > y + 52.5f && mouseY < y + 67.5f;
    }

    private boolean insideHex(int mouseX, int mouseY) {
        return mouseX > x + width / 2.0f + 65.0f && mouseX < x + width && mouseY > y + 52.5f && mouseY < y + 67.5f;
    }

    private float hue(Color color) {
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[0];
    }

    private float saturation(Color color) {
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[1];
    }

    private float brightness(Color color) {
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[2];
    }

    private boolean insideColor(int mouseX, int mouseY) {
        return mouseX > x + width - 12.5f && mouseX < x + width - 2.5f && mouseY > y + 2.5f && mouseY < y + 12.5f;
    }

    private long lastTime = System.currentTimeMillis();
    private boolean cursorVisible = true;

    private String getCursor() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 500) {
            cursorVisible = !cursorVisible;
            lastTime = currentTime;
        }
        return cursorVisible ? "|" : "";
    }

    private class Slider {
        public float x, y, width, height, value, textLerp, sliderX;
        public boolean typing;
        public int i;
        public String text;

        public Slider(int i) {
            this.i = i;
            this.text = String.valueOf(i == 0 ? setting.getValue().getRed() : i == 1 ? setting.getValue().getGreen() : setting.getValue().getBlue());
            this.textLerp = Integer.parseInt(text);
            this.sliderX = textLerp / 255.0f;
        }

        public void drawScreen(int mouseX, int mouseY) {
            RenderUtils.rounded(x, y + 1.0f, x + 20.0f, y + height - 1.0f, 3.0f, FeatureTab.shade(5));
            RenderUtils.roundedOutline(x, y + 1.0f, x + 20.0f, y + height - 1.0f, 3.0f, FeatureTab.shade(-3));
            Color col = setting.getValue();
            Color col2;
            switch (i) {
                case 0:
                    col = new Color(255, col.getGreen(), col.getBlue());
                    col2 = new Color(0, col.getGreen(), col.getBlue());
                    break;
                case 1:
                    col = new Color(col.getRed(), 255, col.getBlue());
                    col2 = new Color(col.getRed(), 0, col.getBlue());
                    break;
                default:
                    col = new Color(col.getRed(), col.getGreen(), 255);
                    col2 = new Color(col.getRed(), col.getGreen(), 0);
                    break;
            }
            RenderUtils.gradient(x + 22.5f, y + 3.5f, x + width, y + height - 3.5f, col2, col, col2, col);
            RenderUtils.roundedOutline(x + 22.5f, y + 3.5f, x + width, y + height - 3.5f, 0.0f, FeatureTab.shade(-3));

            float pickerWidth = (x + width) - (x + 22.5f);
            if (DefaultScreen.getActiveFeature() != null && open && Mouse.isButtonDown(0) && visibleAnim > 0.9f) {
                if (insideSlider(mouseX, mouseY)) {
                    float xDiff = (x + width) - mouseX;
                    float xM = xDiff / pickerWidth;
                    float xMultiplier = Math.min(1.0f, Math.max(1.0f - xM, 0.0f));
                    Color s = setting.getValue();
                    switch (i) {
                        case 0:
                            setting.invokeValue(new Color(xMultiplier, s.getGreen() / 255.0f, s.getBlue() / 255.0f));
                            break;
                        case 1:
                            setting.invokeValue(new Color(s.getRed() / 255.0f, xMultiplier, s.getBlue() / 255.0f));
                            break;
                        case 2:
                            setting.invokeValue(new Color(s.getRed() / 255.0f, s.getGreen() / 255.0f, xMultiplier));
                            break;
                    }
                }
            }

            int k = i == 0 ? setting.getValue().getRed() : i == 1 ? setting.getValue().getGreen() : setting.getValue().getBlue();

            float sliderMultiplier = k / 255.0f;
            sliderX = MathsUtils.lerp(sliderX, sliderMultiplier, Interface.getDelta());
            RenderUtils.rect(x + 22.5f + pickerWidth * sliderX - 1.0f, y + 2.5f, x + 22.5f + pickerWidth * sliderX + 1.0f, y + height - 2.5f, Interface.primary());
            RenderUtils.roundedOutline(x + 22.5f + pickerWidth * sliderX - 1.0f, y + 2.5f, x + 22.5f + pickerWidth * sliderX + 1.0f, y + height - 2.5f, 0.0f, FeatureTab.shade(-3));


            textLerp = MathsUtils.lerp(textLerp, k, Interface.getDelta());
            if (k - textLerp <= 1.0f && k - textLerp >= -1.0f) {
                textLerp = k;
            }
            if (!typing) {
                text = String.valueOf((int) textLerp);
            }

            float scale = 0.8f;
            RenderUtils.invokeScale(scale);
            Managers.TEXT.guiString(text + (typing ? getCursor() : ""), (x + 10.0f - Managers.TEXT.stringWidth(text) * scale / 2.0f) / scale, (y + height / 2.0f - Managers.TEXT.stringHeight() * scale / 2.0f) / scale, Color.WHITE);
            RenderUtils.resetScale();

        }

        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            if (mouseButton == 0) {
                if (insideBox(mouseX, mouseY)) {
                    typing = !typing;
                    lastTime = System.currentTimeMillis();
                    cursorVisible = true;
                } else if (typing) {
                    finish();
                    typing = false;
                }
            }
        }

        public void keyTyped(char typedChar, int keyCode) {
            if (typing) {
                if (keyCode == Keyboard.KEY_BACK) {
                    if (!text.isEmpty()) {
                        text = text.substring(0, text.length() - 1);
                    }
                    lastTime = System.currentTimeMillis();
                    cursorVisible = true;
                    return;
                }
                if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) {
                    finish();
                    typing = false;
                    return;
                }
                if (text.length() < 3) {
                    if (Character.isDigit(typedChar)) {
                        text += typedChar;
                    }
                    lastTime = System.currentTimeMillis();
                    cursorVisible = true;
                }
            }
        }

        private boolean insideSlider(int mouseX, int mouseY) {
            return mouseX > x + 20.0f && mouseX < x + width + 2.5f && mouseY > y + 2.5f && mouseY < y + height - 2.5f;
        }

        private void finish() {
            Color s = setting.getValue();
            if (text.isEmpty()) text = "0";
            int col = Math.max(0, Math.min(255, Integer.parseInt(text)));
            text = String.valueOf(col);
            switch (i) {
                case 0:
                    setting.invokeValue(new Color(col, s.getGreen(), s.getBlue()));
                    break;
                case 1:
                    setting.invokeValue(new Color(s.getRed(), col, s.getBlue()));
                    break;
                case 2:
                    setting.invokeValue(new Color(s.getRed(), s.getGreen(), col));
                    break;
            }
        }

        private boolean insideBox(int mouseX, int mouseY) {
            return mouseX > x && mouseX < x + 20.0f && mouseY > y + 1.0f && mouseY < y + height - 1.0f;
        }
    }
}
