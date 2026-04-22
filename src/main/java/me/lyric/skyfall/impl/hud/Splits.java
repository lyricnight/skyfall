package me.lyric.skyfall.impl.hud;

import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.setting.types.ModeSetting;
import me.lyric.skyfall.impl.event.network.PacketEvent;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author turtleonfire2
 **/

public class Splits extends HUDBase {

    public ModeSetting alignment = settingHUD("Alignment", "Left", Arrays.asList("Left", "Center", "Right"));

    public BooleanSetting lowercase = settingHUD("Lowercase", false);

    public Splits() {
        super("Splits");
        x = 70;
        y = 70;
    }

    private int client = 0;
    private int server = 0;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    private String activeSplit = "";
    LinkedHashMap<String, String> splitString = new LinkedHashMap<>();

    @Override
    public void onRender2D() {
        float deltaY = y;
        float maxWidth = 0f;

        for (Map.Entry<String, String> map : splitString.entrySet()) {
            String fullText = map.getKey() + map.getValue();
            float textWidth = getHUDStringWidth(fullText);
            if (textWidth > maxWidth) {
                maxWidth = textWidth;
            }
        }

        if (!activeSplit.isEmpty() && !splitString.containsKey(activeSplit)) {
            String fullText = activeSplit + ": " + df.format(client / 20.0) + EnumChatFormatting.GRAY + " (" + df.format(server / 20.0) + ")";
            float textWidth = getHUDStringWidth(fullText);
            if (textWidth > maxWidth) {
                maxWidth = textWidth;
            }
        }

        for (Map.Entry<String, String> map : splitString.entrySet()) {
            drawString(map.getKey(), map.getValue(), deltaY, maxWidth);
            deltaY += (getHUDStringHeight() + 4.0f);
        }
        if (!activeSplit.isEmpty() && !splitString.containsKey(activeSplit)) {
            drawString(activeSplit + ": ", df.format(client / 20.0) + EnumChatFormatting.GRAY + " (" + df.format(server / 20.0) + ")", deltaY, maxWidth);
        }
        width = Math.max(150.0f, maxWidth);
        height = 100.0f;
    }

    public void drawString(String string, String time, float yPos, float maxWidth) {
        if (isGradient()){ string = string.replaceAll("§[0-9A-FK-ORa-fk-or]", ""); }
        String finalString = string;
        float fullWidth = getHUDStringWidth(string + time);
        float textX = getAlignedX(maxWidth);
        if (alignment.getValue().equals("Right")) {
            textX += (maxWidth - fullWidth);
        } else if (alignment.getValue().equals("Center")) {
            textX += (maxWidth - fullWidth) / 2f;
        }
        final float finalTextX = textX;
        final float finalYPos = yPos;
        if (lowercase.getValue())
        {
            finalString = finalString.toLowerCase();
        }
        final String finalString1 = finalString;
        renderWithShader(() -> drawHUDStringNoShadow(finalString1, finalTextX, finalYPos, getRenderColor()));
        drawHUDString(time.replace(',', '.'), finalTextX + getHUDStringWidth(finalString1), finalYPos, Color.WHITE);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        String msg = event.message.getUnformattedText().replaceAll("§[0-9A-FK-ORa-fk-or]", "");
        for (Pattern pattern : triggers.keySet()) {
            if (pattern.matcher(msg).find()) {
                if (!activeSplit.isEmpty()) {
                    splitString.put(activeSplit + ": ", df.format(client / 20.0) + EnumChatFormatting.GRAY + " (" + df.format(server / 20.0) + ")");
                    Managers.MESSAGES.send(activeSplit + EnumChatFormatting.WHITE + " took " + client / 20.0 + EnumChatFormatting.GRAY + " (" + server / 20.0 + ")" + EnumChatFormatting.WHITE + " seconds.");
                }
                activeSplit = triggers.get(pattern);
                client = 0;
                server = 0;
                break;
            }
        }
    }


    @Override
    public void onTickPost() {client += 1;
    }

    @SubscribeEvent
    public void onWorldEvent(WorldEvent.Load event) {
        if (!Objects.equals(activeSplit, "Portal: ")) {
            splitString.clear();
            activeSplit = "";
        }
    }

    @ITheAnnotation
    public void onServerTick(PacketEvent.Receive event) {
        if (event.getPacket() instanceof S32PacketConfirmTransaction) {
            if (((S32PacketConfirmTransaction) event.getPacket()).getActionNumber() > 0) return;
            server += 1;
        }
    }

    private static final LinkedHashMap<Pattern, String> triggers = initializeTriggers();

    private static LinkedHashMap<Pattern, String> initializeTriggers() {
        LinkedHashMap<Pattern, String> map = new LinkedHashMap<>();
        // Clear
        map.put(Pattern.compile("\\[NPC] Mort: Here, I found this map when I first entered the dungeon"), "§2Blood Open");
        map.put(Pattern.compile("The BLOOD DOOR has been opened!"), "§bWatcher");
        map.put(Pattern.compile("\\[BOSS] The Watcher: You have proven yourself. You may pass"), "§dPortal");
        // F5/M5
        map.put(Pattern.compile("\\[BOSS] Livid: Welcome, you've arrived right on time"), "§4Livid");
        // F6/M6
        map.put(Pattern.compile("\\[BOSS] Sadan: So you made it all the way here"), "§cTerracottas");
        map.put(Pattern.compile("\\[BOSS] Sadan: ENOUGH!"), "§aGiants");
        map.put(Pattern.compile("\\[BOSS] Sadan: You did it"), "§4Sadan");
        // F7/M7 Boss Fight
        map.put(Pattern.compile("\\[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE"), "§5Maxor");
        map.put(Pattern.compile("\\[BOSS] Storm: Pathetic Maxor, just like expected"), "§3Storm");
        map.put(Pattern.compile("\\[BOSS] Goldor: Who dares trespass into my domain"), "§6Terminals");
        map.put(Pattern.compile("The Core entrance is opening!"), "§6Goldor");
        map.put(Pattern.compile("\\[BOSS] Necron: You went further than any human before, congratulations"), "§cNecron");
        map.put(Pattern.compile("\\[BOSS] Necron: All this, for nothing..."), "§4Wither King");
        // END
        map.put(Pattern.compile("> EXTRA STATS <"), "");
        return map;
    }

    public float getAlignedX(float textWidth) {
        String alignmenta = alignment.getValue();
        switch (alignmenta) {
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