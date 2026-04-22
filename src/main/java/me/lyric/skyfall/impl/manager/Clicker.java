package me.lyric.skyfall.impl.manager;

import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.impl.event.mc.EmptyClickEvent;

import java.util.LinkedList;
import java.util.Queue;

import static me.lyric.skyfall.api.utils.interfaces.Globals.mc;

/**
 * @author mi0
 * this has its own runnable system outside of thread manager, since it requires special timing and runs on game thread.
 */
public final class Clicker {
    private final Queue<Runnable> tasks = new LinkedList<>();

    public void rightClick() {
        tasks.add(mc::rightClickMouse);
    }

    public void scheduleTask(Runnable runnable) {
        tasks.add(runnable);
    }

    public void onUseItemTick() {
        if (mc.thePlayer.isUsingItem()) {
            return;
        }

        Runnable runnable = tasks.peek();
        if (runnable == null) {
            EmptyClickEvent e = new EmptyClickEvent();
            EventBus.getInstance().post(new EmptyClickEvent());
            if (e.isHandled() || e.isCancelled()) {
                mc.gameSettings.keyBindAttack.unpressKey();
                mc.gameSettings.keyBindUseItem.unpressKey();
                mc.gameSettings.keyBindPickBlock.unpressKey();
            }

            return;
        }

        mc.gameSettings.keyBindAttack.unpressKey();
        mc.gameSettings.keyBindUseItem.unpressKey();
        mc.gameSettings.keyBindPickBlock.unpressKey();

        runnable.run();
        tasks.remove();
    }
}
