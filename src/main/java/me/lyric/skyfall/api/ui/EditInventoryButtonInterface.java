package me.lyric.skyfall.api.ui;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Objects;

public class EditInventoryButtonInterface extends GuiScreen {

    static InventoryButtonInterface.InventoryButton button = null;
    private String state = "NONE";

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int x = width / 2;
        int y = height / 2;

        RenderUtils.rounded(x - 100, y - 30, x + 100, y + 30, 5f, Color.GRAY);
        RenderUtils.rounded(x - 99, y - 29, x + 99, y + 29, 5f, Color.DARK_GRAY);

        RenderUtils.rounded(x - 90, y - 20, x + 40, y - 5, 5f, !Objects.equals(state, "COMMAND") ? Color.WHITE : new Color(0.8f, 0.8f, 0.8f));
        Managers.TEXT.stringNoShadow(button.command, x - 85, y - 15, Color.BLACK);

        RenderUtils.rounded(x - 90, y + 5, x + 40, y + 20, 5f, !Objects.equals(state, "ITEMSTACK") ? Color.WHITE : new Color(0.8f, 0.8f, 0.8f));
        Managers.TEXT.stringNoShadow(button.item, x - 85, y + 10, Color.BLACK);

        RenderUtils.rounded(x + 50, y - 20, x + 90, y + 20, 5f, Color.WHITE);

        String[] parts = button.item.split(":");
        int meta = 0;
        Item item;
        item = Item.getByNameOrId(parts[0]);
        if (parts.length >= 2) {
            meta = Integer.parseInt(parts[1]);
        }
        if (item == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 55, y - 15, 0);
        GlStateManager.scale(2f, 2f, 2f);
        mc.getRenderItem().renderItemIntoGUI(new ItemStack(item, 1, meta), 0, 0);
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseX > width / 2 - 90 && mouseX < width / 2 + 40 && mouseY > height / 2 - 20 && mouseY < height / 2 - 5) {
            state = "COMMAND";
        } else if (mouseX > width / 2 - 90 && mouseX < width / 2 + 40 && mouseY > height / 2 - 5 && mouseY < height / 2 + 20) {
            state = "ITEMSTACK";
        } else {
            state = "NONE";
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }

        if (keyCode == Keyboard.KEY_RETURN) {
            if (state.equals("NONE")) {
                mc.displayGuiScreen(new InventoryButtonInterface());
                for (InventoryButtonInterface.InventoryButton b : InventoryButtonInterface.buttons) {
                    if (b.x == button.x && b.y == button.y) {
                        b.command = button.command;
                        b.item = button.item;
                    }
                }
                //there was a save here, but it shouldn't be needed
                return;
            }
            state = "NONE";
            return;
        }

        if (state.equals("NONE")) return;

        String text = state.equals("COMMAND")
                ? button.command
                : button.item;

        if (keyCode == Keyboard.KEY_BACK && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
        } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            text += typedChar;
        }

        if (state.equals("COMMAND")) {
            button.command = text;
        } else if (state.equals("ITEMSTACK")) {
            button.item = text;
        }
    }
}