package me.lyric.skyfall.impl.hud;

import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.setting.types.IntegerSetting;
import me.lyric.skyfall.api.setting.types.StringSetting;
import me.lyric.skyfall.api.utils.maths.StopWatch;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author turtleonfire2
 **/

public class PetChange extends HUDBase {

    public IntegerSetting time = settingHUD("Time", 2500, 100, 5000);

    public StringSetting sound = settingHUD("Sound", "note.pling");

    private final Pattern autopetPattern = Pattern.compile("§cAutopet §eequipped your §7\\[Lvl (.+)] (.+)§e!"), swapPattern = Pattern.compile("§aYou summoned your (.+)§a!");

    private String pet = "";

    private final StopWatch timer = new StopWatch.Single();

    public PetChange() {
        super("Pet Change");
        x = mc.displayWidth / 2f;
        y = mc.displayHeight / 2f - 50f;
        width = 100;
        height = 25;
        fontSizeHUD.invokeValue(50);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        Matcher autopet = autopetPattern.matcher(event.message.getFormattedText());
        Matcher swap = swapPattern.matcher(event.message.getFormattedText());
        if (autopet.find()) {
            timer.reset();
            pet = autopet.group(2);
            mc.thePlayer.playSound(sound.getValue(), 1f, 1f);
        } else if (swap.find()) {
            timer.reset();
            pet = swap.group(1);
            mc.thePlayer.playSound(sound.getValue(), 1f, 1f);
        }
    }

    @Override
    public void onRender2D() {
        if (timer.hasBeen(time.getValue())) {
            return;
        }
        float textWidth = getHUDStringWidth(pet);
        float centerX = x + (100f - textWidth) / 2;
        renderWithShader(() -> drawHUDStringNoShadow(pet, centerX, y, getRenderColor()));
    }
}
