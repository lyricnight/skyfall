package me.lyric.skyfall.api.ui;

import lombok.AllArgsConstructor;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class InventoryButtonInterface extends GuiScreen {

    public static Set<InventoryButton> buttons = new HashSet<>();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int guiLeft = (width - 176) / 2;
        int guiTop = (height - 166) / 2;

        for (InventoryButton button : buttons) {

            if (guiLeft + button.x < 0 || guiTop + button.y < 0 || guiLeft + button.x + 16 > width || guiTop + button.y + 16 > height) continue;

            if (button.command.isEmpty()) {
                RenderUtils.rect(guiLeft + button.x, guiTop + button.y, guiLeft + button.x + 16, guiTop + button.y + 16, new Color(1f, 1f, 1f, 0.1f));
                continue;
            }
            RenderUtils.rect(guiLeft + button.x, guiTop + button.y, guiLeft + button.x + 16f, guiTop + button.y + 16f, Color.GRAY);
            RenderUtils.rect(guiLeft + button.x + 0.5f, guiTop + button.y + 0.5f, guiLeft + button.x + 15.5f, guiTop + button.y + 15.5f, Color.DARK_GRAY);

            String[] parts = button.item.split(":");
            int meta = 0;
            Item item;
            item = Item.getByNameOrId(parts[0]);
            if (parts.length >= 2) {
                meta = Integer.parseInt(parts[1]);
            }
            if (item == null || button.command.isEmpty()) continue;
            mc.getRenderItem().renderItemIntoGUI(new ItemStack(item, 1, meta), guiLeft + button.x, guiTop + button.y);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int guiLeft = (width - 176) / 2;
        int guiTop = (height - 166) / 2;

        for (InventoryButton button : buttons) {
            if (isHovered(mouseX, mouseY, guiLeft +  button.x, guiTop + button.y)) {
                EditInventoryButtonInterface.button = new InventoryButton(button.x, button.y, button.command, button.item);
                mc.displayGuiScreen(new EditInventoryButtonInterface());
            }
        }
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y) {
        return (mouseX >= x && mouseX <= (x + 16) && mouseY >= y && mouseY <= (y + 16));
    }

    @AllArgsConstructor
    public static class InventoryButton {
        public int x, y;
        public String command;
        public String item;
    }

    public static void loadButtonConfig() {
        if (buttons != null && !buttons.isEmpty()) {
            return;
        }
        buttons = new HashSet<>();
        for (int i = -25; i <= 33; i++) { //-19 27
            for (int l = -14; l <= 22; l ++) { // -8 16
                if (i > -1 && i < 9 && l > -1 && l < 9) continue;
                buttons.add(new InventoryButton( 20 * i, -6 + l * 20, "", ""));
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}