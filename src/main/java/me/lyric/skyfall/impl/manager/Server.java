package me.lyric.skyfall.impl.manager;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.impl.event.network.PacketEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S37PacketStatistics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author lyric
 * manages everything related to the server:
 * TPS
 * Ping
 * and anything else.
 * also acts as a tick executor.
 */
public final class Server implements Globals {

    private final ArrayDeque<Float> queue = new ArrayDeque<>(20);
    /**
     * represent current tps.
     */
    @Getter
    private float currentTps;

    /**
     * update variable
     */
    private long time;

    /**
     * queue to store the last 10 ping measurements for averaging
     */
    private final ArrayDeque<Float> pingQueue = new ArrayDeque<>(10);

    /**
     * represents the tps for the last 20 ticks.
     */
    @Getter
    private float tps;
    /**
     * represents the list of tasks in queue
     */
    private final List<Task> tasks = new ArrayList<>();

    /**
     * represents the list of tasks to be executed this tick.
     */
    private final List<Task> toRun = new ArrayList<>();

    private boolean shouldPing = true;

    private long timeSincePing = 0L;

    @Getter
    private float lastPing;

    @Getter
    private float averagePing;

    /**
     * list of silent packets to check against
     * @see me.lyric.skyfall.asm.mixin.MixinNetworkManager
     */
    private final List<Packet<?>> silentList = new ArrayList<>();

    /**
     * initial method to allow scheduling of ping task
     */
    public void init() {
        Managers.THREADS.scheduleRepeating(
                () -> {
                    if (Null.is() || mc.isSingleplayer())
                    {
                        return;
                    }
                    if (!shouldPing)
                    {
                        return;
                    }
                    if (timeSincePing != 0 && System.nanoTime() - timeSincePing > 10e9)
                    {
                        lastPing = 0;
                        averagePing = 0;
                        timeSincePing = 0L;
                        pingQueue.clear();
                        shouldPing = true;
                    }
                    timeSincePing = System.nanoTime();
                    shouldPing = false;
                    silentPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS));
                },
                2000L,2000L, TimeUnit.MILLISECONDS
        );
    }

    /**
     * silently send a packet
     */
    public void silentPacket(Packet<?> packet)
    {
        silentList.add(packet);
        mc.getNetHandler().getNetworkManager().sendPacket(packet);
    }

    /**
     * check if a packet is silent
     */
    public boolean isSilent(Packet<?> packet)
    {
        return silentList.remove(packet);
    }

    /**
     * all 2 called by MinecraftClient mixin.
     */
    public void onTickPre()
    {
        for (Feature feature : Managers.FEATURES.getFeatures()) {
            if (feature.isEnabled()) {
                feature.onTickPre();
            }
        }
        for (HUDBase hudModule : Managers.HUD.getHudModules()) {
            if (hudModule.getEnabled()) {
                hudModule.onTickPre();
            }
        }
        runTasks();
    }

    public void onTickPost() {
        for (Feature feature : Managers.FEATURES.getFeatures()) {
            if (feature.isEnabled()) {
                feature.onTickPost();
            }
        }
        for (HUDBase hudModule : Managers.HUD.getHudModules()) {
            if (hudModule.getEnabled()) {
                hudModule.onTickPost();
            }
        }
        processTasks();
    }

    @ITheAnnotation(priority = Integer.MAX_VALUE - 1)
    public void onPacketReceive(PacketEvent.Receive event)
    {
        if (event.getPacket() instanceof S37PacketStatistics)
        {
            if (shouldPing)
            {
                return;
            }
            if (timeSincePing == 0)
            {
                return;
            }
            long currentTime = System.nanoTime();
            long elapsed = currentTime - timeSincePing;
            lastPing = elapsed / 1e6f;
            if (pingQueue.size() >= 10)
            {
                pingQueue.poll();
            }
            pingQueue.add(lastPing);
            float totalPing = 0.0f;
            for (Float ping : pingQueue)
            {
                totalPing += ping;
            }
            if (!pingQueue.isEmpty())
            {
                averagePing = totalPing / pingQueue.size();
            }
            shouldPing = true;
            timeSincePing = 0L;
        }
        else if (event.getPacket() instanceof S03PacketTimeUpdate)
        {
            if (time != 0)
            {
                if (queue.size() > 20)
                {
                    queue.poll();
                }

                currentTps = Math.max(0.0f, Math.min(20.0f, 20.0f * (1000.0f / (System.currentTimeMillis() - time))));
                queue.add(currentTps);
                float factor = 0.0f;
                for (Float qTime : queue)
                {
                    factor += Math.max(0.0f, Math.min(20.0f, qTime));
                }

                if (!queue.isEmpty())
                {
                    factor /= queue.size();
                }

                tps = factor;
            }
            time = System.currentTimeMillis();
        }
        else if (event.getPacket() instanceof S01PacketJoinGame)
        {
            averagePing = 0f;
            lastPing = 0f;
            pingQueue.clear();
            timeSincePing = 0L;
            shouldPing = true;
        }
    }

    /**
     * Process all scheduled tasks on each tick's end.
     */
    public void processTasks() {
        if (tasks.isEmpty()) return;

        tasks.removeIf(task -> {
            task.delay--;

            if (task.delay <= 0) {
                toRun.add(task);
                return true;
            }
            return false;
        });
    }

    /**
     * runs all tasks that are to be executed, on pre-tick.
     */
    private void runTasks()
    {
        if (toRun.isEmpty()) return;
        for (Task toRunTask : toRun)
        {
            try {
                mc.addScheduledTask(toRunTask.callback);
            } catch (Exception e) {
                ExceptionHandler.handleTaskFailure(e);
            }
        }
        toRun.clear();
    }

    /**
     * call this to add a task to the queue.
     * @param delay - delay in ticks.
     * @param callback - the callback to be executed.
     */
    public void schedule(int delay, Runnable callback) {
        tasks.add(new Task(delay, callback));
    }

    /**
     * simple internal class representing a task to be tick-executed.
     */

    @AllArgsConstructor
    @EqualsAndHashCode
    private static class Task {
        /**
         * the delay in ticks.
         */
        @Getter
        private int delay;
        /**
         * the callback
         */
        @Getter
        private final Runnable callback;
    }
}
