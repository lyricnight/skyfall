package me.lyric.skyfall.impl.manager;

import lombok.Getter;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.impl.hud.*;
import me.lyric.skyfall.impl.hud.Spotify;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lyric
 * loads and stores HUD modules, acts as the Feature manager does for HUDs.
 */
public final class HUD {

    @Getter
    private long last, deltaTime;

    @Getter
    private final List<HUDBase> hudModules = new ArrayList<>();

    /**
     * hud module cache for get() lookups
     */
    private final Map<Class<? extends HUDBase>, HUDBase> hudCache = new HashMap<>();

    public void init()
    {
        hudModules.add(new Watermark());
        hudModules.add(new FeatureList());
        hudModules.add(new Splits());
        hudModules.add(new PetChange());
        hudModules.add(new Tickrate());
        hudModules.add(new Latency());
        hudModules.add(new Spotify());
        hudModules.add(new InvincibilityTimers());
        for (HUDBase hud : hudModules) {
            hudCache.put(hud.getClass(), hud);
        }
    }

    public void add(HUDBase hudModule)
    {
        if (hudModule == null || hudModules.contains(hudModule))
        {
            ExceptionHandler.handle(new NullPointerException("HUD module is null or already exists: " + hudModule));
            return;
        }
        Skyfall.LOGGER.info("Adding tied HUD module: {}", hudModule.getName());
        hudModules.add(hudModule);
        hudCache.put(hudModule.getClass(), hudModule);
    }

    @SuppressWarnings("unchecked")
    public <T extends HUDBase> T get(Class<T> clazz)
    {
        HUDBase cached = hudCache.get(clazz);
        if (cached != null) {
            return (T) cached;
        }
        throw new RuntimeException("No HUD module found for class: " + clazz.getSimpleName());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event)
    {
        for (HUDBase hudModule : hudModules) {
            if (hudModule.getEnabled()) {
                hudModule.onRender2D();
            }
        }
        for (Feature feature : Managers.FEATURES.getFeatures()) {
            if (feature.isEnabled()) {
                feature.onRender2D();
            }
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderWorldEvent(RenderWorldLastEvent ignored)
    {
        if (Null.is()) return;
        for (Feature feature : Managers.FEATURES.getFeatures()) {
            if (feature.isEnabled()) {
                feature.onRender3D();
            }
        }
        deltaTime = System.currentTimeMillis() - last;
        last = System.currentTimeMillis();
    }
}
