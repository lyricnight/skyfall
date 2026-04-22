package me.lyric.skyfall.impl.manager;

import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.nulls.Null;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author eva
 * old, deprecated code.
 */
public final class Player implements Globals {
    /*
    private boolean awaitingMotion = false;
    private float yawToUse;

    public boolean awaitingLava = false;
    public Double lavaDistance = null;
    public boolean clickedThisTick = false;

    @ITheAnnotation
    public void onMotion(MotionUpdateEvent.Pre ignored) {
        if (awaitingMotion) {
            double speed = mc.thePlayer.capabilities.getWalkSpeed() * 2.806;
            double radians = yawToUse * Math.PI / 180;
            double x = -sin(radians) * speed;
            double z = cos(radians) * speed;

            mc.thePlayer.motionX = x;
            mc.thePlayer.motionZ = z;
            restartMovement();
            awaitingMotion = false;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            clickedThisTick = false;
        }
        else {
            if (awaitingLava && mc.thePlayer.isInLava() && lavaDistance != null) {
                Vec3 clipSpot = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                if (lavaDistance == 0.0) {
                    clipSpot = clipSpot.addVector(0, -autoClipDistance(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), 100.0), 0);
                } else {
                    clipSpot = clipSpot.addVector(0, -lavaDistance, 0);
                }
                mc.thePlayer.setPosition(clipSpot.xCoord, clipSpot.yCoord, clipSpot.zCoord);
                lavaClipped = true;
                awaitingLava = false;
                lavaDistance = null;
            }
        }
    }

    @ITheAnnotation
    public void onPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            clickedThisTick = true;
        } else if (event.getPacket() instanceof C07PacketPlayerDigging) {
            clickedThisTick = true;
        }
    }

    public boolean swapTo(String name) {
        if (clickedThisTick) {
            Managers.MESSAGES.send(ChatFormatting.DARK_RED + "[RETARD ALARM]" + ChatFormatting.RED + " Either ur retarded or something went wrong in which case report this");
            return false;
        }
        for (int i = 0; i<9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null) continue;
            String itemName = stack.getDisplayName();
            if (itemName != null) {
                if (itemName.toLowerCase().contains(name.toLowerCase())) {
                    if (mc.thePlayer.inventory.currentItem != i) {
                        mc.thePlayer.inventory.currentItem = i;
                    }
                    return true;
                }
            }
        }
        Managers.MESSAGES.send("Couldn't find " + ChatFormatting.BOLD + name);
        return false;
    }

    public boolean swapTo(int id) {
        if (clickedThisTick) {
            Managers.MESSAGES.send(ChatFormatting.DARK_RED + "[RETARD ALARM]" + ChatFormatting.RED + " Either ur retarded or something went wrong in which case report this");
            return false;
        }
        for (int i = 0; i<9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null) continue;
            int itemId = Item.getIdFromItem(stack.getItem());
            if (itemId == id) {
                if (mc.thePlayer.inventory.currentItem != i) {
                    mc.thePlayer.inventory.currentItem = i;
                }
                return true;
            }
        }
        Managers.MESSAGES.send("Couldn't find " + ChatFormatting.BOLD + Item.getItemById(id).getUnlocalizedName());
        return false;
    }

    public void swapToSlot(int slot) {
        if (clickedThisTick) {
            Managers.MESSAGES.send(ChatFormatting.DARK_RED + "[RETARD ALARM]" + ChatFormatting.RED + " Either ur retarded or something went wrong in which case report this");
            return;
        }
        if (slot<9 && slot>=0) mc.thePlayer.inventory.currentItem = slot;
    }

    private boolean pearlThrown = false;
    private Vec3 spotToClip = null;

    private boolean lavaClipped = false;

    public double autoClipDistance(BlockPos pos, double maxDist) {
        BlockPos prevBlock = pos.add(0, -1, 0);
        BlockPos currBlock = prevBlock.add(0, -1, 0);
        boolean gapFound = false;
        int i = 0;
        while (i < maxDist) {
            i += 1;
            if (mc.theWorld.getBlockState(currBlock).getBlock() == Blocks.air && mc.theWorld.getBlockState(prevBlock).getBlock() == Blocks.air) gapFound = true;
            prevBlock = currBlock;
            currBlock = prevBlock.add(0, -1, 0);
            if (gapFound && mc.theWorld.getBlockState(currBlock).getBlock() != Blocks.air) return pos.getY() - prevBlock.getY();
        }
        return 0.0;
    }

    public boolean motioning = false;
    public Float motionDir = null;

    private final int[] wasd = {30, 31, 32, 17};

    @ITheAnnotation
    public void onKey(KeyEvent event) {
        if (IntStream.of(wasd).anyMatch(i -> i == event.getKey())) {
            motioning = false;
            motionDir = null;
        }
    }

     */

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event)
    {
        if (event.entity == mc.thePlayer && !Null.is())
        {
            for (Feature feature : Managers.FEATURES.getFeatures()) {
                if (feature.isEnabled()) {
                    feature.onUpdate();
                }
            }
        }
    }
}
