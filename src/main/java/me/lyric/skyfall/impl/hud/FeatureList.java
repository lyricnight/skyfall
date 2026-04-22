package me.lyric.skyfall.impl.hud;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.utils.maths.MathsUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureList extends HUDBase {
    @Getter
    private final ArrayList<Feature> eligibleFeatures = new ArrayList<>();

    private final ArrayList<FeatureString> names = new ArrayList<>();
    private final ArrayList<FeatureString> appends = new ArrayList<>();
    private final ArrayList<Feature> toRemove = new ArrayList<>();

    public BooleanSetting lower = settingHUD("Lowercase", false);
    public BooleanSetting displayAppend = settingHUD("Appends", true);

    /**
     * Cached lowercase names/appends
     */
    private final Map<Feature, String> cachedLowerNames = new HashMap<>();
    private final Map<Feature, String> cachedLowerAppends = new HashMap<>();

    /**
     * Cached width values
     */
    private final Map<Feature, Float> cachedWidths = new HashMap<>();
    private int lastFontSize = -1;
    private boolean lastDisplayAppend = false;
    private boolean lastLowercase = false;

    /**
     * Track previous append values to detect changes
     */
    private final Map<Feature, String> lastAppendValues = new HashMap<>();

    /**
     * flag true when sorting needed
     */
    private boolean needsSort = true;
    private int lastFeatureCount = -1;

    /**
     * Cached runnable for shader rendering
     */
    private final Runnable shaderCallback = this::drawNames;
    private int currentNameCount = 0;

    public FeatureList() {
        super("FeatureList");
        x = 100;
        y = 70;
    }

    private void drawNames() {
        for (int i = 0; i < currentNameCount; i++) {
            names.get(i).draw(this);
        }
    }

    /**
     * Comparator that uses cached widths
     */
    private final Comparator<Feature> widthComparator = (a, b) -> {
        Float widthA = cachedWidths.get(a);
        Float widthB = cachedWidths.get(b);
        if (widthA == null) widthA = 0f;
        if (widthB == null) widthB = 0f;
        return Float.compare(widthA, widthB);
    };

    @AllArgsConstructor
    private static class FeatureString {
        String name;
        float x, y;
        Color color;

        void draw(FeatureList list) {
            list.drawHUDString(name, x, y, color);
        }

        void update(String name, float x, float y, Color color) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    private float getCachedWidth(Feature feature, int fontSize, boolean withAppend) {
        Float cached = cachedWidths.get(feature);
        if (cached != null) {
            return cached;
        }
        float width = withAppend ? feature.getStringWidthFull(fontSize) : -feature.getStringWidth(fontSize);
        cachedWidths.put(feature, width);
        return width;
    }

    private boolean checkAppendChanges(List<Feature> features) {
        boolean changed = false;
        for (Feature feature : features) {
            String currentAppend = feature.displayAppend();
            String lastAppend = lastAppendValues.get(feature);
            if (lastAppend == null || !lastAppend.equals(currentAppend)) {
                lastAppendValues.put(feature, currentAppend);
                changed = true;
            }
        }
        return changed;
    }

    private void checkCacheValidity() {
        int currentFontSize = fontSizeHUD.getValue();
        boolean currentDisplayAppend = displayAppend.getValue();
        boolean currentLowercase = lower.getValue();
        if (currentFontSize != lastFontSize || currentDisplayAppend != lastDisplayAppend) {
            cachedWidths.clear();
            cachedLowerNames.clear();
            cachedLowerAppends.clear();
            lastFontSize = currentFontSize;
            lastDisplayAppend = currentDisplayAppend;
            needsSort = true;
        }
        if (currentLowercase != lastLowercase) {
            cachedLowerNames.clear();
            cachedLowerAppends.clear();
            lastLowercase = currentLowercase;
        }
    }

    /**
     * Called when font changes to invalidate caches
     */
    @Override
    public void invalidateFontCache() {
        super.invalidateFontCache();
        cachedWidths.clear();
        cachedLowerNames.clear();
        cachedLowerAppends.clear();
    }

    @Override
    public void onRender2D() {
        checkCacheValidity();
        List<Feature> drawableFeatures = Managers.FEATURES.getDrawableFeatures();

        if (checkAppendChanges(drawableFeatures)) {
            cachedWidths.clear();
            cachedLowerAppends.clear();
            needsSort = true;
        }

        int currentFeatureCount = drawableFeatures.size();
        if (currentFeatureCount != lastFeatureCount) {
            needsSort = true;
            lastFeatureCount = currentFeatureCount;
        }
        if (needsSort || eligibleFeatures.isEmpty()) {
            eligibleFeatures.clear();
            eligibleFeatures.addAll(drawableFeatures);
            int fontSize = fontSizeHUD.getValue();
            boolean withAppend = displayAppend.getValue();
            for (Feature f : eligibleFeatures) {
                getCachedWidth(f, fontSize, withAppend);
            }
            eligibleFeatures.sort(widthComparator);
            needsSort = false;
        }

        int fontSize = fontSizeHUD.getValue();
        boolean withAppend = displayAppend.getValue();
        toRemove.clear();
        float deltaY = y;
        int nameIndex = 0;
        int appendIndex = 0;
        boolean useLowercase = lower.getValue();

        for (Feature feature : eligibleFeatures)
        {
            feature.anim = MathsUtils.lerp(feature.anim, feature.getEnabled().getValue() ? 1.0f : 0.0f, 0.005f * Managers.HUD.getDeltaTime());
            if (!feature.getEnabled().getValue() && feature.anim < 0.05f) {
                toRemove.add(feature);
                needsSort = true;
                continue;
            }
            float xPos = this.x + width;
            float featureWidth = getCachedWidth(feature, fontSize, withAppend);
            if (withAppend) {
                xPos += featureWidth * feature.anim;
            } else {
                xPos -= (-featureWidth) * feature.anim;
            }
            String featureName;
            String appendName;
            if (useLowercase) {
                featureName = cachedLowerNames.get(feature);
                if (featureName == null) {
                    featureName = feature.getName().toLowerCase();
                    cachedLowerNames.put(feature, featureName);
                }
                appendName = cachedLowerAppends.get(feature);
                if (appendName == null) {
                    appendName = feature.displayAppend().toLowerCase();
                    cachedLowerAppends.put(feature, appendName);
                }
            } else {
                featureName = feature.getName();
                appendName = feature.displayAppend();
            }

            if (nameIndex < names.size()) {
                names.get(nameIndex).update(featureName, xPos, deltaY, getRenderColor());
            } else {
                names.add(new FeatureString(featureName, xPos, deltaY, getRenderColor()));
            }
            nameIndex++;
            if (withAppend) {
                float appendX = xPos + feature.getStringWidth(fontSize);
                if (appendIndex < appends.size()) {
                    appends.get(appendIndex).update(appendName, appendX, deltaY, Color.GRAY);
                } else {
                    appends.add(new FeatureString(appendName, appendX, deltaY, Color.GRAY));
                }
                appendIndex++;
            }

            deltaY += (getHUDStringHeight() + 4.0f) * feature.anim;
        }

        if (!toRemove.isEmpty()) {
            eligibleFeatures.removeAll(toRemove);
        }
        currentNameCount = nameIndex;
        renderWithShader(shaderCallback);
        for (int i = 0; i < appendIndex; i++) {
            appends.get(i).draw(this);
        }
        width = 200.0f;
        height = deltaY - y + 1.0f;
    }
}
