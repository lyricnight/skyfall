package me.lyric.skyfall.impl.manager;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.impl.feature.miscellaneous.RichPresence;

import java.time.OffsetDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author lyric
 */
public final class Discord implements Globals, IPCListener {

    private static IPCClient ipcClient;
    private ScheduledFuture<?> updateTask;

    public boolean started = false;

    public void init()
    {
        if (started) return;
        try {
            ipcClient = new IPCClient(1342659701560709170L);
            started = true;
            ipcClient.setListener(this);
            ipcClient.connect();
            updateTime();
            updateTask = Managers.THREADS.scheduleRepeating(
                this::updateCall,
                7500,
                7500,
                TimeUnit.MILLISECONDS
            );
        }
        catch (Exception e)
        {
            ExceptionHandler.handle(e, this.getClass());
            started = false;
        }
    }

    private void restart()
    {
        try {
            ipcClient = new IPCClient(1342659701560709170L);
            ipcClient.setListener(this);
            ipcClient.connect();
            updateTime();
            started = true;

            if (updateTask != null && !updateTask.isCancelled()) {
                updateTask.cancel(false);
            }
            updateTask = Managers.THREADS.scheduleRepeating(
                this::updateCall,
                7500,
                7500,
                TimeUnit.MILLISECONDS
            );
        }
        catch (Exception e)
        {
            ExceptionHandler.handle(e, this.getClass());
            Managers.MESSAGES.send("Somehow failed to restart the IPC client.");
            Managers.FEATURES.get(RichPresence.class).setEnabled(false);
        }
    }

    /**
     * called when it's time to update.
     */

    public void updateCall()
    {
        if (ipcClient == null) return;
        update();
    }

    /**
     * update methods.
     * this is stupid, but it works...
     */

    private void update()
    {
        if (ipcClient == null) return;
        try
        {
            ipcClient.sendRichPresence(
                    new com.jagrosh.discordipc.entities.RichPresence.Builder()
                            .setDetails(Managers.FEATURES.get(RichPresence.class).getDetails())
                            .setState(Managers.FEATURES.get(RichPresence.class).getState())
                            .setLargeImage(Managers.FEATURES.get(RichPresence.class).getModeKey(), "Skyfall 2.0 for MC 1.8.9.")
                            .build()
            );
        }
        catch (Exception e) {
            ExceptionHandler.handle(e, this.getClass());
                Managers.MESSAGES.send("If you see this error, it's likely you either don't have discord open, or you swapped discord accounts.");
                Managers.MESSAGES.send("Attempting to restart the RPC client...");
                restart();
        }
    }

    private void updateTime()
    {
        if (ipcClient == null) return;
        try {
            ipcClient.sendRichPresence(
                    new com.jagrosh.discordipc.entities.RichPresence.Builder()
                            .setDetails(Managers.FEATURES.get(RichPresence.class).getDetails())
                            .setState(Managers.FEATURES.get(RichPresence.class).getState())
                            .setStartTimestamp(OffsetDateTime.now())
                            .setLargeImage(Managers.FEATURES.get(RichPresence.class).getModeKey(), "Skyfall 2.0 for MC 1.8.9.")
                            .build()
            );
        }
        catch (Exception e)
        {
            ExceptionHandler.handle(e, this.getClass());
                Managers.MESSAGES.send("If you see this error, it's likely you either don't have discord open, or you swapped discord accounts.");
                Managers.MESSAGES.send("Attempting to restart the RPC client...");
                restart();
        }
    }

    public void shutdown()
    {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel(false);
            updateTask = null;
        }
        ipcClient = null;
        started = false;
    }
}
