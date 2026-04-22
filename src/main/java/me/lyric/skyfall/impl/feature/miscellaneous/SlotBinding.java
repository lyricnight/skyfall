package me.lyric.skyfall.impl.feature.miscellaneous;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.ActionSetting;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.impl.event.mc.GuiMouseClicked;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lyric
 * 'simple' feature for slotbinding.
 */
public final class SlotBinding extends Feature {

    public Map<Integer, Integer> slots = new HashMap<>();

    private boolean binding;

    public BooleanSetting verbose = setting("Verbose",  true).invokeTab("Messages");

    public ActionSetting prepare = setting("Prepare", "Prepare a bind", () -> {
        if (binding) {
            binding = false;
            Managers.MESSAGES.send(ChatFormatting.RED + "Slot binding disabled.");
        } else {
            binding = true;
            Managers.MESSAGES.send(ChatFormatting.GREEN + "Slot binding enabled. Click slots to bind them.");
        }
    }).invokeTab("Binds");

    public ActionSetting reset = setting("Reset", "Reset Binds", () -> {
        slots.clear();
        Managers.MESSAGES.send(ChatFormatting.GREEN + "Reset your slot bindings.");
    }).invokeTab("Binds");

    private int previous;

    public SlotBinding()
    {
        super("SlotBinding", Category.Miscellaneous);
    }

    @Override
    public void onEnable()
    {
        previous = 0;
    }

    /**
     * internal method to handle clicked slot.
     */
    private void handleClick(int slot)
    {
        int hotbarSlot = slots.get(slot) % 36;
        if (hotbarSlot == 0 || hotbarSlot >= 9)  {
            return;
        }
        if (verbose.getValue())
        {
            Managers.MESSAGES.send(ChatFormatting.GREEN + "Swapping slot " + slot + ".");
        }
        mc.playerController.windowClick(
                mc.thePlayer.openContainer.windowId,
                slot,
                hotbarSlot,
                2,
                mc.thePlayer
        );
    }

    @ITheAnnotation(priority = 100)
    public void onInventoryClicked(GuiMouseClicked event) {
        int key = event.getKey();
        GuiInventory screen = (GuiInventory) event.getScreen();

        try {
            if (key != 0) {
                int slot = screen.getSlotUnderMouse().slotNumber;

                if (slot < 5) {
                    Managers.MESSAGES.send(ChatFormatting.RED + "Registered return as slot is less than 5. How did you do that?");
                    return;
                }

                if (previous != 0 && (slot < 36 || slot > 44)) {
                    Managers.MESSAGES.send(ChatFormatting.RED + "Please select a valid hot bar slot you monkey. Slot selected: " + slot);
                    previous = 0;
                    return;
                }

                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && slots.containsKey(slot)) {
                    event.setCancelled(true);
                    handleClick(slot);
                    return;
                }
                if (!binding) {
                    return;
                }
                if (previous == 0) {
                    previous = slot;
                    return;
                }
                event.setCancelled(true);
                if (slot == previous) {
                    Managers.MESSAGES.send(ChatFormatting.RED + "Registered previous equality. " + slot);
                    return;
                }
                slots.put(previous, slot);
                Managers.MESSAGES.send(ChatFormatting.GREEN + "Saved slot binding, slot " + previous + " -> " + "slot " + slot);
                previous = 0;
                binding = false;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e, this.getClass());
        }
    }
}
