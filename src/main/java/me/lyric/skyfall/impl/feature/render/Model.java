package me.lyric.skyfall.impl.feature.render;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.FloatSetting;

/**
 * @author lyric
 */
public final class Model extends Feature {
    public static final float[] DEFAULT_SCALE = new float[]{1.0f, 1.0f, 1.0f};

    private final FloatSetting positionX = setting("Position X", 0.0f, -1.5f, 3.0f).invokeTab("Position");
    private final FloatSetting positionY = setting("Position Y", 0.0f, -1.5f, 3.0f).invokeTab("Position");
    private final FloatSetting positionZ = setting("Position Z", 0.0f, -1.5f, 3.0f).invokeTab("Position");

    private final FloatSetting scaleX = setting("Scale X", 1.0f, 0.1f, 5.0f).invokeTab("Scale");
    private final FloatSetting scaleY = setting("Scale Y", 1.0f, 0.1f, 5.0f).invokeTab("Scale");
    private final FloatSetting scaleZ = setting("Scale Z", 1.0f, 0.1f, 5.0f).invokeTab("Scale");

    private final FloatSetting rotationX = setting("Rotation X", 0.0f, -180.0f, 180.0f).invokeTab("Rotation");
    private final FloatSetting rotationY = setting("Rotation Y", 0.0f, -180.0f, 180.0f).invokeTab("Rotation");
    private final FloatSetting rotationZ = setting("Rotation Z", 0.0f, -180.0f, 180.0f).invokeTab("Rotation");

    private float lastPosX, lastPosY, lastPosZ;
    private float lastScaleX, lastScaleY, lastScaleZ;
    private float lastRotX, lastRotY, lastRotZ;
    private boolean lastEnabled = false;

    public Model() {
        super("Model", Category.Render);
        updateLastValues();
    }

    private void updateLastValues() {
        lastPosX = positionX.getValue();
        lastPosY = positionY.getValue();
        lastPosZ = positionZ.getValue();
        lastScaleX = scaleX.getValue();
        lastScaleY = scaleY.getValue();
        lastScaleZ = scaleZ.getValue();
        lastRotX = rotationX.getValue();
        lastRotY = rotationY.getValue();
        lastRotZ = rotationZ.getValue();
        lastEnabled = this.isEnabled();
    }

    private void checkForChanges() {
        boolean changed = false;

        if (lastPosX != positionX.getValue() || lastPosY != positionY.getValue() || lastPosZ != positionZ.getValue()) {
            changed = true;
        }
        if (lastScaleX != scaleX.getValue() || lastScaleY != scaleY.getValue() || lastScaleZ != scaleZ.getValue()) {
            changed = true;
        }
        if (lastRotX != rotationX.getValue() || lastRotY != rotationY.getValue() || lastRotZ != rotationZ.getValue()) {
            changed = true;
        }
        if (lastEnabled != this.isEnabled()) {
            changed = true;
        }

        if (changed) {
            try {
                HandShader handShader = Managers.FEATURES.get(HandShader.class);
                handShader.clearBoundsCache();
            } catch (Exception e) {
            }
            updateLastValues();
        }
    }

    @Override
    public void onEnable() {
        checkForChanges();
    }

    @Override
    public void onDisable() {
        checkForChanges();
    }

    /**
     * TODO find some way to completely integrate this shit
     */
    @Override
    public void onRender3D() {
        checkForChanges();
    }

    public float getPositionX() {
        return positionX.getValue();
    }

    public float getPositionY() {
        return positionY.getValue();
    }

    public float getPositionZ() {
        return positionZ.getValue();
    }

    public float getRotationX() {
        return rotationX.getValue();
    }

    public float getRotationY() {
        return rotationY.getValue();
    }

    public float getRotationZ() {
        return rotationZ.getValue();
    }

    /**
     * Get scale values for the viewmodel.
     * @return float array [scaleX, scaleY, scaleZ]
     */
    public float[] getScale() {
        if (!this.isEnabled()) {
            return DEFAULT_SCALE;
        }
        return new float[]{scaleX.getValue(), scaleY.getValue(), scaleZ.getValue()};
    }
}
