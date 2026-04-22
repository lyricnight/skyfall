package me.lyric.skyfall.impl.feature.player;

import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.ActionSetting;
import me.lyric.skyfall.api.setting.types.FloatSetting;
import me.lyric.skyfall.api.ui.InventoryButtonInterface;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import me.lyric.skyfall.impl.event.mc.GuiMouseClicked;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

//TODO change rendering methods
public final class InventoryButtons extends Feature {

    public ActionSetting actionSetting = setting("Action", "Open Gui", this::openGui).invokeTab("Actions");
    public FloatSetting floatSetting = setting("Cooldown", 0.5f, 0f, 2f).invokeTab("Misc");

    public InventoryButtons() {
        super("InventoryButtons", Category.Player);
    }


    public void openGui() {
        mc.displayGuiScreen(new InventoryButtonInterface());
    }

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (mc.currentScreen instanceof GuiInventory) {
            int width = new ScaledResolution(mc).getScaledWidth();
            int height = new ScaledResolution(mc).getScaledHeight();
            int guiLeft = (width - 176) / 2;
            int guiTop = (height - 166) / 2;
            for (InventoryButtonInterface.InventoryButton button : InventoryButtonInterface.buttons) {
                if (button.command.isEmpty()) continue;
                if (guiLeft + button.x < 0 || guiTop + button.y < 0 || guiLeft + button.x + 16 > width || guiTop + button.y + 16 > height) continue;
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
    }

    @Override
    public void onWorldChange() {
        InventoryButtonInterface.loadButtonConfig();
    }

    private Long lastExecute = System.currentTimeMillis();

    @ITheAnnotation
    public void onMouseClick(GuiMouseClicked event) {
        if (!(mc.currentScreen instanceof GuiInventory)) return;
        int width = new ScaledResolution(mc).getScaledWidth();
        int height = new ScaledResolution(mc).getScaledHeight();
        int guiLeft = (width - 176) / 2;
        int guiTop = (height - 166) / 2;
        for (InventoryButtonInterface.InventoryButton button : InventoryButtonInterface.buttons) {
            if (guiLeft + button.x < 0 || guiTop + button.y < 0 || guiLeft + button.x + 16 > width || guiTop + button.y + 16 > height) continue;
            if (isHovered(event.getX(), event.getY(), guiLeft + button.x, guiTop + button.y)) {
                if (System.currentTimeMillis() - lastExecute < floatSetting.getValue() * 1000) return;
                if (button.command.isEmpty()) return;

                if (ClientCommandHandler.instance.executeCommand(mc.thePlayer, button.command) == 0) {
                    mc.thePlayer.sendChatMessage("/" + button.command);
                }

                lastExecute = System.currentTimeMillis();
            }
        }
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y) {
        return (mouseX >= x && mouseX <= (x + 16) && mouseY >= y && mouseY <= (y + 16));
    }
}