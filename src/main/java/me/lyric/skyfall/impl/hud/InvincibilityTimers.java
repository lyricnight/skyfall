package me.lyric.skyfall.impl.hud;

import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.ModeSetting;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.impl.manager.Location;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.DecimalFormat;
import java.util.Arrays;

public class InvincibilityTimers extends HUDBase {

    public ModeSetting alignmentHUD = settingHUD("Alignment", "Left", Arrays.asList("Left", "Center", "Right"));

    public InvincibilityTimers() {
        super("Invincibility Timer"); //I hate the word invincibility icl
        x = 10;
        y = 10;
    }

    int phoenixTime, bonzoTime, spiritTime;
    boolean phoenix, bonzo, spirit;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public void onRender2D() {
        if (!(Managers.LOCATION.getCurrentIsland() == Location.Island.DUNGEON))
        {
            return;
        }
        float h = getHUDStringHeight() + 4f;

        checkMasks();

        float maxWidth = getHUDStringWidth("| Bonzo's Mask: Ready");

        String bonzoStr = getString(bonzo, "Bonzo's Mask: ", bonzoTime);
        String phoenixStr = getString(phoenix, "Phoenix Pet: ", phoenixTime);
        String spiritStr = getString(spirit, "Spirit Mask: ", spiritTime);

        float bonzoX = getAlignedX(maxWidth) + (alignmentHUD.getValue().equals("Right") ? (maxWidth - getHUDStringWidth(bonzoStr)) : alignmentHUD.getValue().equals("Center") ? (maxWidth - getHUDStringWidth(bonzoStr)) / 2f : 0);
        float phoenixX = getAlignedX(maxWidth) + (alignmentHUD.getValue().equals("Right") ? (maxWidth - getHUDStringWidth(phoenixStr)) : alignmentHUD.getValue().equals("Center") ? (maxWidth - getHUDStringWidth(phoenixStr)) / 2f : 0);
        float spiritX = getAlignedX(maxWidth) + (alignmentHUD.getValue().equals("Right") ? (maxWidth - getHUDStringWidth(spiritStr)) : alignmentHUD.getValue().equals("Center") ? (maxWidth - getHUDStringWidth(spiritStr)) / 2f : 0);

        renderWithShader(() -> {
            drawHUDString(bonzoStr, bonzoX, y, getRenderColor());
            drawHUDString(phoenixStr, phoenixX, y + h, getRenderColor());
            drawHUDString(spiritStr, spiritX, y + 2 * h, getRenderColor());
        });
        width = maxWidth;
        height = (getHUDStringHeight() + 4) * 3;
    }

    public String getString(boolean equipped, String string, int num) {
        String time = num > 0 ? "§c" + df.format(num / 20.0) : "§aReady";
        return (equipped ? "| " : "  ") + string + time;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String msg = event.message.getUnformattedText();

        if (msg.contains("Second Wind Activated! Your Spirit Mask saved your life!")) {
            spiritTime = 600;
        } else if (msg.contains("Bonzo's Mask saved your life!")) {
            bonzoTime = 3600;
        } else if (msg.contains("Your Phoenix Pet saved you from certain death!")) {
            phoenixTime = 1200;
        } else if (msg.contains("You summoned your") || msg.contains("Autopet equipped your")) {
            phoenix = msg.contains("Phoenix");
        } else if (msg.contains("You despawned your Phoenix!")) {
            phoenix = false;
        }
    }

    @Override
    public void onTickPost() {
        phoenixTime -= 1;
        spiritTime -= 1;
        bonzoTime -= 1;
    }

    public void checkMasks() {
        if (Null.is()) return;
        ItemStack head = mc.thePlayer.getCurrentArmor(3);
        if (head == null || !head.hasDisplayName()) {
            bonzo = false;
            spirit = false;
            return;
        }
        bonzo = head.getDisplayName().contains("Bonzo");
        spirit = head.getDisplayName().contains("Spirit");
    }

    public float getAlignedX(float textWidth) {
        String alignment = alignmentHUD.getValue();
        switch (alignment) {
            case "Center":
                return x + (width - textWidth) / 2f;
            case "Right":
                return x + width - textWidth;
            case "Left":
            default:
                return x;
        }
    }
}