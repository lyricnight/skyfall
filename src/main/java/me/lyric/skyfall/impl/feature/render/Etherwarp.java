package me.lyric.skyfall.impl.feature.render;

import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.setting.types.ColourSetting;
import me.lyric.skyfall.api.setting.types.FloatSetting;
import me.lyric.skyfall.api.setting.types.ModeSetting;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.api.utils.render.FeatureRenderUtils;
import me.lyric.skyfall.api.utils.skyblock.EtherwarpHelper;
import me.lyric.skyfall.api.utils.skyblock.ItemUtils;
import me.lyric.skyfall.api.utils.skyblock.Vec3Utils;
import me.lyric.skyfall.asm.mixin.accessors.IEntityPlayerSPAccessor;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.Arrays;

/**
 * @author lyric
 */
public final class Etherwarp extends Feature {

    public ColourSetting color = setting("Colour", new Color(255, 170, 0, 150)).invokeTab("Rendering");
    public BooleanSetting renderFail = setting("Show Failed", true).invokeTab("Rendering");
    public ColourSetting wrongColor = setting("Failed Colour", new Color(255, 85, 85, 150)).invokeTab("Rendering").invokeVisibility(v -> renderFail.getValue());
    public ModeSetting style = setting("Style", "Outline", Arrays.asList("Outline", "Filled", "Both")).invokeTab("Rendering");
    public FloatSetting lineWidth = setting("Line Width", 2.0f, 0.1f, 10.0f).invokeTab("Rendering");
    public BooleanSetting depthCheck = setting("Depth Check", false).invokeTab("Rendering");
    public BooleanSetting fullBlock = setting("Full Block", false).invokeTab("Rendering");
    public BooleanSetting useServerPosition = setting("Use Server Position", true).invokeTab("Modes");
    public BooleanSetting interactBlocks = setting("Fail on Interactable", true).invokeTab("Modes");
    public FloatSetting interpolationSpeed = setting("Interpolation Speed", 0.5f, 0.1f, 1.0f).invokeTab("Modes");
    public FloatSetting snapThreshold = setting("Snap Threshold", 10.0f, 1.0f, 50.0f).invokeTab("Modes");
    private Vec3 currentRenderPos = null;
    private Vec3 targetPos = null;

    public Etherwarp() {
        super("Etherwarp", Category.Render);
    }

    @Override
    public void onRender3D() {
        if (Null.is()) return;
        if (!ItemUtils.isUsingEtherwarp(mc.thePlayer)) {
            currentRenderPos = null;
            targetPos = null;
            return;
        }

        Vec3Utils.PositionLook positionLook;
        if (useServerPosition.getValue() && mc.thePlayer instanceof IEntityPlayerSPAccessor) {
            IEntityPlayerSPAccessor accessor = (IEntityPlayerSPAccessor) mc.thePlayer;
            Vec3 serverPos = new Vec3(
                accessor.getLastReportedPosX(),
                accessor.getLastReportedPosY(),
                accessor.getLastReportedPosZ()
            );
            positionLook = new Vec3Utils.PositionLook(serverPos, accessor.getLastReportedYaw(), accessor.getLastReportedPitch());
        } else {
            positionLook = new Vec3Utils.PositionLook(
                mc.thePlayer.getPositionVector(),
                mc.thePlayer.rotationYaw,
                mc.thePlayer.rotationPitch
            );
        }

        double distance = 60.0 + ItemUtils.getTunerBonus(mc.thePlayer.getHeldItem());

        Vec3Utils.EtherPos etherPos = EtherwarpHelper.getEtherPos(positionLook, distance, true);

        boolean succeeded = etherPos.succeeded;
        if (succeeded && interactBlocks.getValue() && mc.objectMouseOver != null) {
            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos hitPos = mc.objectMouseOver.getBlockPos();
                if (hitPos != null && mc.theWorld != null) {
                    Block hitBlock = mc.theWorld.getBlockState(hitPos).getBlock();
                    int blockId = Block.getIdFromBlock(hitBlock);
                    if (EtherwarpHelper.isInteractableBlock(blockId)) {
                        succeeded = false;
                    }
                }
            }
        }

        if (!succeeded && !renderFail.getValue()) {
            currentRenderPos = null;
            targetPos = null;
            return;
        }

        if (etherPos.pos == null) {
            currentRenderPos = null;
            targetPos = null;
            return;
        }

        Vec3 newTargetPos = new Vec3(etherPos.pos.getX(), etherPos.pos.getY(), etherPos.pos.getZ());

        if (currentRenderPos == null) {
            currentRenderPos = newTargetPos;
            targetPos = newTargetPos;
        } else {
            double distance2 = Vec3Utils.distanceTo(targetPos, newTargetPos);

            if (distance2 > snapThreshold.getValue()) {
                currentRenderPos = newTargetPos;
                targetPos = newTargetPos;
            } else {
                targetPos = newTargetPos;
                currentRenderPos = new Vec3(
                    MathsUtils.lerp(currentRenderPos.xCoord, targetPos.xCoord, interpolationSpeed.getValue()),
                    MathsUtils.lerp(currentRenderPos.yCoord, targetPos.yCoord, interpolationSpeed.getValue()),
                    MathsUtils.lerp(currentRenderPos.zCoord, targetPos.zCoord, interpolationSpeed.getValue())
                );
            }
        }

        Color renderColor = succeeded ? color.getValue() : wrongColor.getValue();

        renderEtherwarpBox(currentRenderPos, renderColor);
    }

    private void renderEtherwarpBox(Vec3 pos, Color color) {
        double x = pos.xCoord;
        double y = pos.yCoord;
        double z = pos.zCoord;

        boolean filled = style.getValue().equals("Filled") || style.getValue().equals("Both");
        boolean outline = style.getValue().equals("Outline") || style.getValue().equals("Both");
        double yOffset = depthCheck.getValue() ? 0.002 : 0.0;

        if (fullBlock.getValue()) {
            FeatureRenderUtils.renderBox(
                x,
                y + yOffset,
                z,
                1.0,
                1.0,
                1.0,
                outline ? color : new Color(0, 0, 0, 0),
                filled ? color : new Color(0, 0, 0, 0),
                lineWidth.getValue(),
                !depthCheck.getValue(),
                true,
                filled
            );
        } else {
            double blockHeight = 0.0625;
            FeatureRenderUtils.renderBox(
                x,
                y + 1.0 - blockHeight + yOffset,
                z,
                1.0,
                blockHeight,
                1.0,
                outline ? color : new Color(0, 0, 0, 0),
                filled ? color : new Color(0, 0, 0, 0),
                lineWidth.getValue(),
                !depthCheck.getValue(),
                true,
                filled
            );
        }
    }

    @Override
    public void onDisable() {
        currentRenderPos = null;
        targetPos = null;
    }
}

