package me.lyric.skyfall.api.ui.drawables.gui.screens.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.Drawable;
import me.lyric.skyfall.api.ui.drawables.gui.feature.tab.FeatureTab;
import me.lyric.skyfall.api.ui.drawables.gui.screens.GUISize;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ConfigScreen {
    private final ResourceLocation expand = new ResourceLocation("textures/icons/other/expand.png"), reload = new ResourceLocation("textures/icons/other/reload.png");
    private final ArrayList<CategoryBoolean> left = new ArrayList<>(), right = new ArrayList<>();
    private final CopyOnWriteArrayList<LoadableConfig> loadableConfigs = new CopyOnWriteArrayList<>();
    private String selectedLoad = "AutoSave", saveString = "";
    private File selectedFile = new File(Managers.CONFIG.getFolder() + "/AutoSave");
    private boolean open, typing;
    private float anim, expandAnim, reloadAnim, scroll, scrollTarget, textAnim = 0.4f;
    private long sys;
    public float x, y;

    public ConfigScreen() {
        for (Category category : Category.values()) {
            left.add(new CategoryBoolean(category));
            right.add(new CategoryBoolean(category));
        }
        loadLoadableConfigs();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Interface.selectedScreen.equals("Configs")) {
            sys = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - sys > 500) {
            return;
        }
        anim = MathsUtils.lerp(anim, Interface.selectedScreen.equals("Configs") ? 0.0f : 1.0f, Interface.getDelta());


        float width = GUISize.guiWidth - GUISize.sidebarWidth - 25.0f;
        float exit = GUISize.guiWidth / 2.0f * anim;
        float leftX = x + GUISize.sidebarWidth + 15.0f - exit,
                rightX = x + GUISize.sidebarWidth + 15.0f + width / 2.0f + 5.0f + exit,
                leftWidth = x + GUISize.sidebarWidth + 15.0f + width / 2.0f - 5.0f - exit,
                rightWidth = x + GUISize.guiWidth - 10.0f + exit,
                boxY = y + GUISize.categoryBarY,
                boxHeight = y + GUISize.guiHeight / 2.0f;
        RenderUtils.prepareScissor(x + GUISize.sidebarWidth + 14.0f, y, x + GUISize.guiWidth, boxHeight);

        RenderUtils.rounded(leftX, boxY, leftWidth, boxHeight, 5.0f, color());
        RenderUtils.rounded(rightX, boxY, rightWidth, boxHeight, 5.0f, color());

        RenderUtils.roundedOutline(leftX, boxY, leftWidth, boxHeight, 5.0f, shade(-3));
        RenderUtils.roundedOutline(rightX, boxY, rightWidth, boxHeight, 5.0f, shade(-3));

        RenderUtils.rounded(leftX + 5.0f, boxY + 5.0f, leftWidth - 5.0f, boxY + 25.0f, 3.0f, shade(5));
        RenderUtils.roundedOutline(leftX + 5.0f, boxY + 5.0f, leftWidth - 5.0f, boxY + 25.0f, 3.0f, shade(-3));

        float halfLeftWidth = leftX + (leftWidth - leftX) / 2.0f;
        RenderUtils.rounded(halfLeftWidth - 50.0f, boxHeight - 30.0f, halfLeftWidth + 50.0f, boxHeight - 10.0f, 3.0f, shade(5));
        RenderUtils.roundedOutline(halfLeftWidth - 50.0f, boxHeight - 30.0f, halfLeftWidth + 50.0f, boxHeight - 10.0f, 3.0f, shade(-3));

        Managers.TEXT.guiString("Save", halfLeftWidth - Managers.TEXT.stringWidth("Save") / 2.0f, boxHeight - 20.0f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, textAnim));

        float expansion = (((Math.max(0, Math.min(7, loadableConfigs.size() - 1))) * 20.0f + 2.5f) * expandAnim);

        float halfRightWidth = rightX + (rightWidth - rightX) / 2.0f;
        RenderUtils.rounded(halfRightWidth - 50.0f, boxHeight - 30.0f + expansion, halfRightWidth + 50.0f, boxHeight - 10.0f + expansion, 3.0f, shade(5));
        RenderUtils.roundedOutline(halfRightWidth - 50.0f, boxHeight - 30.0f + expansion, halfRightWidth + 50.0f, boxHeight - 10.0f + expansion, 3.0f, shade(-3));
        Managers.TEXT.guiString("Load", halfRightWidth - Managers.TEXT.stringWidth("Load") / 2.0f, boxHeight - 20.0f + expansion - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, textAnim));

        RenderUtils.rounded(rightX + 5.0f, boxY + 5.0f, rightWidth - 25.0f, boxY + 25.0f + expansion, 3.0f, shade(5));
        RenderUtils.roundedOutline(rightX + 5.0f, boxY + 5.0f, rightWidth - 25.0f, boxY + 25.0f + expansion, 3.0f, shade(-3));

        RenderUtils.textureSmooth(rightWidth - 42.5f, boxY + 7.5f, rightWidth - 27.5f, boxY + 22.5f, new Color(1.0f, 1.0f, 1.0f, 1.0f), expand);

        Managers.TEXT.guiString(selectedLoad, rightX + 10.0f, boxY + 15.0f - Managers.TEXT.stringHeight() / 2.0f, Color.WHITE);

        if (selectedFile.exists()) {
            try {
                String string = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(Files.getLastModifiedTime(Paths.get(selectedFile.toURI())).toMillis());
                Managers.TEXT.guiString(string, rightX + 10.0f + (rightWidth - rightX) / 2.0f - Managers.TEXT.stringWidth(string) / 2.0f, boxY + 15.0f - Managers.TEXT.stringHeight() / 2.0f, Color.white);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        RenderUtils.rounded(rightWidth - 22.5f, boxY + 5.0f, rightWidth - 2.5f, boxY + 25.0f, 3.0f, shade(5));
        RenderUtils.roundedOutline(rightWidth - 22.5f, boxY + 5.0f, rightWidth - 2.5f, boxY + 25.0f, 3.0f, shade(-3));

        RenderUtils.textureSmooth(rightWidth - 20.0f, boxY + 7.5f, rightWidth - 5.0f, boxY + 22.5f, new Color(1.0f, 1.0f, 1.0f, reloadAnim), reload);

        expandAnim = MathsUtils.lerp(expandAnim, open ? 1.0f : 0.0f, Interface.getDelta());
        reloadAnim = MathsUtils.lerp(reloadAnim, insideReload(mouseX, mouseY, rightWidth, boxY) ? 1.0f : 0.4f, Interface.getDelta());

        scroll = MathsUtils.lerp(scroll, scrollTarget, Interface.getDelta());
        if (!open) {
            scrollTarget = 0.0f;
        }

        float deltaY = boxY + 30.0f;
        for (CategoryBoolean categoryBoolean : left) {
            categoryBoolean.x = leftX + 5.0f;
            categoryBoolean.y = deltaY;
            categoryBoolean.width = leftWidth - leftX - 15.0f;
            categoryBoolean.drawScreen(mouseX, mouseY, partialTicks);
            deltaY += 15.0f;
        }

        deltaY = boxY + 30.0f + expansion;
        for (CategoryBoolean categoryBoolean : right) {
            categoryBoolean.x = rightX + 5.0f;
            categoryBoolean.y = deltaY;
            categoryBoolean.width = rightWidth - rightX - 15.0f;
            categoryBoolean.drawScreen(mouseX, mouseY, partialTicks);
            deltaY += 15.0f;
        }

        RenderUtils.releaseScissor();

        RenderUtils.prepareScissor(x + GUISize.sidebarWidth + 14.0f, boxY + 25.0f, x + GUISize.guiWidth, boxY + 25.0f + expansion);
        deltaY = boxY + 25.0f + scroll;
        for (LoadableConfig loadableConfig : loadableConfigs) {
            if (loadableConfig.config.equals(selectedLoad)) {
                continue;
            }
            loadableConfig.x = rightX + 10.0f;
            loadableConfig.y = deltaY;
            loadableConfig.width = rightWidth - rightX;
            loadableConfig.height = 20.0f;
            loadableConfig.drawScreen(mouseX, mouseY, partialTicks);
            deltaY += 20.0f;
        }
        RenderUtils.releaseScissor();

        RenderUtils.prepareScissor(x + GUISize.sidebarWidth + 14.0f, boxY, Math.max(x + GUISize.sidebarWidth + 15.0f, leftWidth - 10.0f), boxY + 25.0f + expansion);
        String text = (saveString.isEmpty() ? "Name" : saveString) + (typing ? typingIcon() : "");
        textAnim = MathsUtils.lerp(textAnim, saveString.isEmpty() ? 0.4f : 1.0f, Interface.getDelta());
        Managers.TEXT.guiString(text, leftX + 10.0f, boxY + 15.0f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, textAnim));
        RenderUtils.releaseScissor();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (System.currentTimeMillis() - sys > 500) {
            return;
        }
        float width = GUISize.guiWidth - GUISize.sidebarWidth - 25.0f;
        float exit = GUISize.guiWidth / 2.0f * anim;
        float leftX = x + GUISize.sidebarWidth + 15.0f - exit,
                rightX = x + GUISize.sidebarWidth + 15.0f + width / 2.0f + 5.0f + exit,
                leftWidth = x + GUISize.sidebarWidth + 15.0f + width / 2.0f - 5.0f - exit,
                rightWidth = x + GUISize.guiWidth - 10.0f + exit,
                boxY = y + GUISize.categoryBarY,
                boxHeight = y + GUISize.guiHeight / 2.0f;
        float halfLeftWidth = leftX + (leftWidth - leftX) / 2.0f;
        if (mouseButton == 0) {
            float halfRightWidth = rightX + (rightWidth - rightX) / 2.0f;
            float expansion = (((Math.max(0, Math.min(7, loadableConfigs.size() - 1))) * 20.0f + 2.5f) * expandAnim);
            if (insideLoadButton(mouseX, mouseY, halfRightWidth, boxHeight, expansion)) {
                if (selectedLoad != null && !selectedLoad.isEmpty()) {
                    ArrayList<Category> categories = right.stream().filter(CategoryBoolean::getValue).map(categoryBoolean -> categoryBoolean.category).collect(Collectors.toCollection(ArrayList::new));
                    Managers.CONFIG.load(selectedLoad, categories);
                    Managers.MESSAGES.send(ChatFormatting.GREEN + "Attempting to load configuration " + selectedLoad + ".");
                    loadLoadableConfigs();
                }
            }
            if (insideSaveButton(mouseX, mouseY, halfLeftWidth, boxHeight)) {
                ArrayList<Category> categories = left.stream().filter(CategoryBoolean::getValue).map(categoryBoolean -> categoryBoolean.category).collect(Collectors.toCollection(ArrayList::new));
                Managers.MESSAGES.send(ChatFormatting.GREEN + "Attempting to save configuration " + saveString + ".");
                Managers.CONFIG.save(saveString, true, categories);
                saveString = "";
            }
            if (insideSave(mouseX, mouseY, leftX, boxY, leftWidth)) {
                typing = !typing;
            }
            if (insideReload(mouseX, mouseY, rightWidth, boxY)) {
                loadLoadableConfigs();
            }
            if (insideOpen(mouseX, mouseY, rightX, rightWidth, boxY)) {
                boolean insideDelete = false;
                float deltaY = boxY + 25.0f + scroll;
                for (LoadableConfig loadableConfig : loadableConfigs) {
                    if (loadableConfig.config.equals(selectedLoad)) {
                        continue;
                    }
                    loadableConfig.x = rightX + 10.0f;
                    loadableConfig.y = deltaY;
                    loadableConfig.width = rightWidth - rightX;
                    loadableConfig.height = 20.0f;
                    if (open && loadableConfig.insideDelete(mouseX, mouseY)) {
                        insideDelete = true;
                    }
                    deltaY += 20.0f;
                }
                if (!insideDelete) {
                    open = !open;
                }
            }
        }
        float deltaY = boxY + 25.0f + scroll;
        for (LoadableConfig loadableConfig : loadableConfigs) {
            if (loadableConfig.config.equals(selectedLoad)) {
                continue;
            }
            loadableConfig.x = rightX + 10.0f;
            loadableConfig.y = deltaY;
            loadableConfig.width = rightWidth - rightX;
            loadableConfig.height = 20.0f;
            if (loadableConfig.y < GUISize.guiHeight && expandAnim > 0.1f) {
                loadableConfig.mouseClicked(mouseX, mouseY, mouseButton);
            }
            deltaY += 20.0f;
        }
        left.forEach(categoryBoolean -> categoryBoolean.mouseClicked(mouseX, mouseY, mouseButton));
        right.forEach(categoryBoolean -> categoryBoolean.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (typing) {
            saveString = type(saveString, typedChar, keyCode);
        }
    }

    private boolean insideLoadButton(int mouseX, int mouseY, float halfRightWidth, float boxHeight, float expansion) {
        return mouseX > halfRightWidth - 50.0f && mouseX < halfRightWidth + 50.0f && mouseY > boxHeight - 30.0f + expansion && mouseY < boxHeight - 10.0f + expansion;
    }

    private boolean insideSaveButton(int mouseX, int mouseY, float halfLeftWidth, float boxHeight) {
        return mouseX > halfLeftWidth - 50.0f && mouseX < halfLeftWidth + 50.0f && mouseY > boxHeight - 30.0f && mouseY < boxHeight - 10.0f;
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
                typing = false;
                break;
            default:
                if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                    newString = newString + typedChar;
                    break;
                }
        }
        return newString;
    }

    private void loadLoadableConfigs() {
        loadableConfigs.clear();
        try {
            for (File file : Objects.requireNonNull(Managers.CONFIG.getConfigsFolder().listFiles())) {
                if (file.getName().equals("active.txt") || file.getName().equals("friends.txt") || file.isDirectory() && file.getName().equals("AutoRoutes") ||file.getName().equals("prefix.txt") || file.getName().equals("slot_bindings.txt")) {
                    continue;
                }
                loadableConfigs.add(new LoadableConfig(file.getName()));
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    private long s = System.currentTimeMillis();

    private String typingIcon() {
        if (System.currentTimeMillis() - s < 500) {
            return "";
        }
        if (System.currentTimeMillis() - s < 1000) {
            return "_";
        }
        s = System.currentTimeMillis();
        return "";
    }

    private boolean insideSave(int mouseX, int mouseY, float leftX, float boxY, float leftWidth) {
        return mouseX > leftX && mouseX < leftWidth - 5.0f && mouseY > boxY + 5.0f && mouseY < boxY + 25.0f;
    }

    private boolean insideReload(int mouseX, int mouseY, float rightWidth, float boxY) {
        return mouseX > rightWidth - 25.0f && mouseX < rightWidth - 2.5f && mouseY > boxY + 5.0f && mouseY < boxY + 25.0f;
    }

    private boolean insideOpen(int mouseX, int mouseY, float rightX, float rightWidth, float boxY) {
        float expansion = (((Math.max(0, loadableConfigs.size() - 1)) * 20.0f + 5.0f) * expandAnim);
        return mouseX > rightX + 5.0f && mouseX < rightWidth - 25.0f && mouseY > boxY + 5.0f && mouseY < boxY + 25.0f + expansion;
    }

    public static Color color() {
        Color get = Interface.background();
        if (get.getRed() + 20 > 255 || get.getGreen() + 20 > 255 || get.getBlue() + 27 > 255) {
            return get;
        }
        return new Color(get.getRed() + 20, get.getGreen() + 20,get.getBlue() + 27);
    }

    public static Color shade(int i) {
        Color get = color();
        if (get.getRed() + i > 255 || get.getGreen() + i > 255 || get.getBlue() + i > 255 || get.getRed() + i < 0 || get.getGreen() + i < 0 || get.getBlue() + i < 0) {
            return get;
        }
        return new Color(get.getRed() + i, get.getGreen() + i, get.getBlue() + i);
    }


    private class LoadableConfig implements Drawable {
        private final String config;
        private final File file;
        private float
                x, y,
                width, height,
                alpha,
                deleteHover = 0.4f;

        public LoadableConfig(String config) {
            this.config = config;
            this.file = new File(Managers.CONFIG.getFolder() + "/" + config);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            Managers.TEXT.guiString(config, x, y + height / 2.0f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, alpha));
            if (file.exists()) {
                try {
                    final String string = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(Files.getLastModifiedTime(Paths.get(file.toURI())).toMillis());
                    Managers.TEXT.guiString(string, x + width / 2.0f - Managers.TEXT.stringWidth(string) / 2.0f, y + height / 2.0f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, alpha));
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
            RenderUtils.rounded(x + width - 55.0f, y + 2.5f, x + width - 40.0f, y + height - 2.5f, 5.0f, shade(5));
            RenderUtils.roundedOutline(x + width - 55.0f, y + 2.5f, x + width - 40.0f, y + height - 2.5f, 5.0f, shade(-3));

            RenderUtils.rect(x + width - 52.0f, y + height / 2.0f - 0.5f, x + width - 42.0f, y + height / 2.0f + 0.5f, new Color(1.0f, 0.0f, 0.0f, deleteHover));

            deleteHover = MathsUtils.lerp(deleteHover, insideDelete(mouseX, mouseY) && !inside(mouseX, mouseY) ? 1.0f : 0.4f, Interface.getDelta());
            alpha = MathsUtils.lerp(alpha, inside(mouseX, mouseY) ? 1.0f : 0.4f, Interface.getDelta());
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            if (mouseButton == 0) {
                if (insideDelete(mouseX, mouseY) && !inside(mouseX, mouseY)) {
                    for (File file1 : Objects.requireNonNull(file.listFiles())) {
                        for (File file2 : Objects.requireNonNull(file1.listFiles())) {
                            file2.delete();
                        }
                        file1.delete();
                    }
                    file.delete();
                    loadLoadableConfigs();
                }
                if (inside(mouseX, mouseY)) {
                    if (!config.equals(selectedLoad)) {
                        selectedLoad = config;
                        selectedFile = file;
                    }
                }
            }
        }

        @Override
        public void keyTyped(char typedChar, int keyCode) {}

        private boolean inside(int mouseX, int mouseY) {
            return mouseX > x && mouseX < x + width - 55.0f && mouseY > y && mouseY < y + height;
        }

        public boolean insideDelete(int mouseX, int mouseY) {
            return mouseX > x - width - 52.5f && mouseX < x + width - 42.5f && mouseY > y + 2.5f && mouseY < y + height - 2.5f;
        }
    }

    private static class CategoryBoolean implements Drawable {
        private final Category category;
        public float x, y, width, height;
        private boolean value;
        private float alpha, c;

        public CategoryBoolean(Category category) {
            this.category = category;
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            Managers.TEXT.guiString(category.toString(), x, y + height / 2.0f - Managers.TEXT.stringHeight() / 2.0f, new Color(1.0f, 1.0f, 1.0f, alpha));

            RenderUtils.rounded(x + width - 25.0f, y + 2.5f, x + width, y + height - 2.5f, 5.0f, FeatureTab.shade(5));
            RenderUtils.roundedOutline(x + width - 25.0f, y + 2.5f, x + width, y + height - 2.5f, 5.0f, FeatureTab.shade(-2));

            alpha = MathsUtils.lerp(alpha, value ? insideEnabled(mouseX, mouseY) ? 0.7f : 1.0f : insideEnabled(mouseX, mouseY) ? 0.7f : 0.4f, Interface.getDelta());
            c = MathsUtils.lerp(c, value ? 1.0f : 0.0f, Interface.getDelta());
            Color p = Interface.primary();
            Color color = new Color(p.getRed() / 255.0f, p.getGreen() / 255.0f, p.getBlue() / 255.0f, alpha);
            RenderUtils.circle(x + width - 21.0f + (13.0f * c), y + height / 2.0f - 2.0f, 4.0f, color);
            height = 15.0f;
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            if (mouseButton == 0 && insideEnabled(mouseX, mouseY)) {
                value = !value;
            }
        }

        @Override
        public void keyTyped(char typedChar, int keyCode) {
        }

        private boolean insideEnabled(int mouseX, int mouseY) {
            return mouseX > x + width - 25.0f && mouseX < x + width && mouseY > y + 2.5f && mouseY < y + height - 2.5f;
        }

        public boolean getValue() {
            return value;
        }
    }
}
