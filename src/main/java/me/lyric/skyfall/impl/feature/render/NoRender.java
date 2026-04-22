package me.lyric.skyfall.impl.feature.render;

import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.utils.skyblock.TextureUtils;
import me.lyric.skyfall.impl.event.mc.RenderCheckEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lyric and tortle
 * prevent a bunch of random stuff from rendering and killing fps.
 */
public final class NoRender extends Feature {
    public BooleanSetting archer = setting("Archer Passive", false).invokeTab("Misc");

    public BooleanSetting cheap = setting("Cheap Coins", false).invokeTab("Misc");

    public BooleanSetting fire = setting("Fire", false).invokeTab("Misc");

    public BooleanSetting backgroundNametags = setting("Nametag Backgrounds", true).invokeTab("Misc");

    public BooleanSetting hurtCam = setting("Hurt Camera", false).invokeTab("Misc");

    public BooleanSetting armorIcons = setting("Armor Icons", true).invokeTab("GUI");

    public BooleanSetting hungerIcons = setting("Hunger Icons", true).invokeTab("GUI");

    public BooleanSetting noBlind = setting("No Blindness", false).invokeTab("Debuff");
    public BooleanSetting noPortal = setting("No Portal Effect", false).invokeTab("Debuff");
    public BooleanSetting seeThroughBlocks = setting("Transparency blocks", false).invokeTab("Debuff");
    public BooleanSetting noNausea = setting("No Nausea", false).invokeTab("Debuff");

    private final Set<?> cheapCoins = new HashSet<>(Arrays.asList(
            "ewogICJ0aW1lc3RhbXAiIDogMTcxOTYwMDMwOTQ4MywKICAicHJvZmlsZUlkIiA6ICI1OTgyOWY1ZGY3MmM0ZmFlOTBmOGVhYmM0MjFjMzJkYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQZXBwZXJEcmlua2VyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE2YjkwZjRmYTNlYzEwNmJmZWYyMWYzYjc1ZjU0MWExOGU0NzU3Njc0ZjdkNTgyNTBmYTdlNzQ5NTJmMDg3ZGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
            "eyJ0aW1lc3RhbXAiOjE1NjAwMzYyODI5MTcsInByb2ZpbGVJZCI6ImU3NmYwZDlhZjc4MjQyYzM5NDY2ZDY3MjE3MzBmNDUzIiwicHJvZmlsZU5hbWUiOiJLbGxscmFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jZGVlNjIxZWI4MmIwZGFiNDE2NjMzMGQxZGEwMjdiYTJhYzEzMjQ2YTRjMWU3ZDUxNzRmNjA1ZmRkZjEwYTEwIn19fQ==",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg2ODk5MTUyNCwKICAicHJvZmlsZUlkIiA6ICIxMTM1Njg1ZTk3ZGE0ZjYyYTliNDQ3MzA0NGFiZjQ0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXJpb1dsZXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2NiY2NlMjJhZjU1OWVkNmJhNjAzODg0NWRiMzhjY2JjYTJlNjJiNzdiODdhMjZhMDY2NTcxMDljZTBlZmJhNiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
            "ewogICJ0aW1lc3RhbXAiIDogMTU5ODg0NzA4MjYxMywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQwZDZlMzYyYmM3ZWVlNGY5MTFkYmQwNDQ2MzA3ZTc0NThkMTA1MGQwOWFlZTUzOGViY2IwMjczY2Y3NTc0MiIKICAgIH0KICB9Cn0="
            )
    );

    public NoRender()
    {
        super("NoRender", Category.Render);
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.FOOD)
        {
            if (hungerIcons.getValue()) {
                event.setCanceled(true);
            }
        }
    }

    @ITheAnnotation
    public void onRenderCheck(RenderCheckEvent event)
    {
        if (archer.getValue() && event.getEntity() instanceof EntityItem && ((EntityItem) event.getEntity()).getEntityItem().getItemDamage() == 15 && ((EntityItem) event.getEntity()).getEntityItem().getItem() == Items.dye) {
            event.setCancelled(true);
        }
        else if (event.getEntity() instanceof EntityItem && cheap.getValue() && cheapCoins.contains(TextureUtils.getSkullTexture(((EntityItem) event.getEntity()).getEntityItem())))
        {
            event.setCancelled(true);
        }
        GuiIngameForge.renderArmor = !armorIcons.getValue();
    }

    @SubscribeEvent
    public void onRenderFog(EntityViewRenderEvent.FogDensity event) {
        if (noBlind.getValue()) {
            event.density = 0f;
            event.setCanceled(true);
            GlStateManager.setFogStart(998f);
            GlStateManager.setFogEnd(999f);
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.PORTAL && noPortal.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
        if (event.overlayType == RenderBlockOverlayEvent.OverlayType.BLOCK && seeThroughBlocks.getValue()) {
            event.setCanceled(true);
        }
    }
}
