package me.lyric.skyfall.impl.manager;

import lombok.Getter;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.impl.event.mc.KeyEvent;
import org.lwjgl.input.Keyboard;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lyric
 * Manages all features
 */
@Getter
public final class Features implements Globals {
    /**
     * hash set of features
     */
    private Set<Feature> features = new HashSet<>();

    /**
     * cached feature classes used in get() lookup
     */
    private final Map<Class<? extends Feature>, Feature> featureCache = new HashMap<>();

    /**
     * list for drawable features to avoid allocation every frame
     */
    private final List<Feature> drawableFeaturesCache = new ArrayList<>();

    /**
     * flag for drawable features cache
     */
    private boolean drawableFeaturesValid = false;

    /**
     * initialise features
     */
    public void init()
    {
        try {
            Reflections reflections = new Reflections("me.lyric.skyfall.impl.feature");
            Set<Class<? extends Feature>> classes = reflections.getSubTypesOf(Feature.class);
            final Set<Feature> toSort = new HashSet<>();
            classes.stream().filter(clazz -> !Modifier.isAbstract(clazz.getModifiers())).forEach(clazz -> {
                try {
                    Feature instance = clazz.getDeclaredConstructor().newInstance();
                    toSort.add(instance);
                }
                catch (Throwable t)
                {
                    //we don't use exceptionHandler here, since the client will not have loaded past nulls at this point.
                    Skyfall.errored = true;
                    Skyfall.LOGGER.catching(t);
                    Skyfall.LOGGER.error("Failed to load feature: {}", clazz.getSimpleName());
                }
            });
            features = toSort.stream().sorted(Comparator.comparingInt(Feature::hashCode)).collect(Collectors.toCollection(LinkedHashSet::new));
            for (Feature feature : features) {
                featureCache.put(feature.getClass(), feature);
            }
        }
        catch (Exception e) {
            //we can use it here anyway since this should never fail
            ExceptionHandler.handle(e, this.getClass());
        }
    }

    @ITheAnnotation(priority = 1)
    public void onKey(KeyEvent event)
    {
        if (mc.currentScreen != null) return;
        for (Feature feature : features) {
            if (feature.getKeybind().getValue() == event.getKey() && feature.getKeybind().getValue() != Keyboard.KEY_NONE) {
                feature.toggle();
            }
        }
    }

    /**
     * called when the client is completely loaded.
     */
    public void onInit()
    {
        features.forEach(Feature::onInit);
    }

    /**
     * get the instance of a feature.
     * @param clazz - class in
     * @return the instance of that feature
     * @param <T> - generic type
     */
    @SuppressWarnings("unchecked")
    public <T extends Feature> T get(Class<T> clazz) {
        Feature cached = featureCache.get(clazz);
        if (cached != null) {
            return (T) cached;
        }
        throw new RuntimeException("Class does not match any known feature! Report this!");
    }

    /**
     * get a list of features that are drawn on the screen
     * @return - list of drawable features
     */
    public List<Feature> getDrawableFeatures()
    {
        if (!drawableFeaturesValid) {
            drawableFeaturesCache.clear();
            for (Feature feature : features) {
                if (feature.getDrawn().getValue()) {
                    drawableFeaturesCache.add(feature);
                }
            }
            drawableFeaturesValid = true;
        }
        return drawableFeaturesCache;
    }

    /**
     * forces it to rebuild on next frame
     */
    public void invalidateDrawableCache() {
        drawableFeaturesValid = false;
    }
}
