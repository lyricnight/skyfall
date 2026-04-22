package me.lyric.skyfall.impl.manager;

import lombok.Getter;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * thread management system
 * Usage Examples:
 * {@code
 * general async task
 * Managers.THREADS.runAsync(() -> {
 *     // Do network call or file I/O
 * });
 <p>
 * delayed task
 * Managers.THREADS.scheduleDelayed(() -> {
 *     // Execute after delay
 * }, 5, TimeUnit.SECONDS);
<p>
 * repeating task
 * Managers.THREADS.scheduleRepeating(() -> {
 *     // Execute periodically
 * }, 0, 1, TimeUnit.SECONDS);
 <p>
 * heavy task
 * Future<String> result = Managers.THREADS.submitHeavy(() -> {
 *     // CPU intensive work
 *     return "result";
 * });
 * }
 *
 * @author lyric
 */
public final class Threads {
    private static final AtomicInteger generalThreadCounter = new AtomicInteger(0);
    private static final AtomicInteger scheduledThreadCounter = new AtomicInteger(0);
    private static final AtomicInteger heavyThreadCounter = new AtomicInteger(0);
    private static final AtomicInteger spotifyThreadCounter = new AtomicInteger(0);

    private ExecutorService generalExecutor;
    private ScheduledExecutorService scheduledExecutor;
    private ExecutorService heavyExecutor;
    private ScheduledExecutorService spotifyPollingExecutor;

    @Getter
    private volatile boolean initialized = false;

    @Getter
    private volatile boolean shuttingDown = false;

    /**
     * inits all service executors
     */
    public void init() {
        if (initialized) {
            throw new RuntimeException("Thread manager is already initialized!");
        }

        generalExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Skyfall-General-" + generalThreadCounter.incrementAndGet());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        });

        scheduledExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "Skyfall-Scheduled-" + scheduledThreadCounter.incrementAndGet());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        });

        int heavyThreads = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        heavyExecutor = Executors.newFixedThreadPool(heavyThreads, r -> {
            Thread t = new Thread(r, "Skyfall-Heavy-" + heavyThreadCounter.incrementAndGet());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        });

        spotifyPollingExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "Skyfall-Spotify-Polling-" + spotifyThreadCounter.incrementAndGet());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        });

        initialized = true;
        Skyfall.LOGGER.info("Threads manager initialized with {} heavy threads", heavyThreads);
    }

    /**
     * null check for this
     */
    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    init();
                }
            }
        }
    }

    /**
     * Runs a task asynchronously on the general purpose executor.
     * Use this for general async operations like network calls, file I/O, etc.
     *
     * @param task The task to run
     */
    public void runAsync(Runnable task) {
        ensureInitialized();
        if (shuttingDown) return;
        generalExecutor.execute(wrapTask(task, "General"));
    }


    /**
     * Runs a task asynchronously and returns a CompletableFuture.
     *
     * @param task The task to run
     * @param <T> The return type
     * @return A CompletableFuture for the result
     */
    public <T> CompletableFuture<T> supplyAsync(Callable<T> task) {
        ensureInitialized();
        if (shuttingDown) return CompletableFuture.completedFuture(null);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                Skyfall.LOGGER.error("Error in async task: {}", e.getMessage());
                throw new CompletionException(e);
            }
        }, generalExecutor);
    }

    /**
     * Schedules a task to run after a delay.
     *
     * @param task The task to run
     * @param delay The delay before execution
     * @param unit The time unit of the delay
     * @return A ScheduledFuture representing the scheduled task
     */
    public ScheduledFuture<?> scheduleDelayed(Runnable task, long delay, TimeUnit unit) {
        ensureInitialized();
        if (shuttingDown) return null;
        return scheduledExecutor.schedule(wrapTask(task, "Scheduled"), delay, unit);
    }

    /**
     * Schedules a task to run periodically with a fixed rate.
     * The task will run at initialDelay, then initialDelay + period, initialDelay + 2*period, etc.
     *
     * @param task The task to run
     * @param initialDelay The initial delay before first execution
     * @param period The period between successive executions
     * @param unit The time unit
     * @return A ScheduledFuture representing the scheduled task
     */
    public ScheduledFuture<?> scheduleRepeating(Runnable task, long initialDelay, long period, TimeUnit unit) {
        ensureInitialized();
        if (shuttingDown) return null;
        return scheduledExecutor.scheduleAtFixedRate(wrapTask(task, "Scheduled"), initialDelay, period, unit);
    }

    /**
     * this runs a parallel processing task - used in @link AudioVisualiser
     * @param task The task to run
     */
    public void runHeavy(Runnable task) {
        ensureInitialized();
        if (shuttingDown) return;
        heavyExecutor.execute(wrapTask(task, "Heavy"));
    }

    /**
     * a heavy task that returns some value
     * @param task The task to submit
     * @param <T> The return type
     * @return A Future representing the pending result
     */
    public <T> Future<T> submitHeavy(Callable<T> task) {
        ensureInitialized();
        if (shuttingDown) return null;
        return heavyExecutor.submit(wrapCallable(task, "Heavy"));
    }

    /**
     * dedicated spotify polling executor
     * @param task The polling task to run
     */
    public void runSpotifyPolling(Runnable task) {
        ensureInitialized();
        if (shuttingDown) return;
        spotifyPollingExecutor.scheduleAtFixedRate(wrapTask(task, "Spotify-Polling"), 2000, 2000, TimeUnit.MILLISECONDS);
    }

    /**
     * util methods that wraps stuff for error logging
     * not really that important but whatever
     */
    private Runnable wrapTask(Runnable task, String executorName) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                Skyfall.LOGGER.error("Error in {} executor wrapTask task: {}", executorName, e.getMessage());
                ExceptionHandler.handle(e, this.getClass());
            }
        };
    }


    private <T> Callable<T> wrapCallable(Callable<T> task, String executorName) {
        return () -> {
            try {
                return task.call();
            } catch (Exception e) {
                Skyfall.LOGGER.error("Error in {} executor wrapCallable task: {}", executorName, e.getMessage());
                ExceptionHandler.handle(e, this.getClass());
                throw e;
            }
        };
    }

    public String getDebugInfo() {
        if (!initialized) {
            return "Thread manager not initialized";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Thread Manager Status ===\n");
        sb.append("Initialized: ").append(initialized).append("\n");
        sb.append("Shutting Down: ").append(shuttingDown).append("\n\n");
        sb.append("General Executor: ");
        if (generalExecutor != null) {
            sb.append(getExecutorStatus(generalExecutor));
            sb.append(" (Thread Count: ").append(generalThreadCounter.get()).append(")");
        } else {
            sb.append("null");
        }
        sb.append("\n");
        sb.append("Scheduled Executor: ");
        if (scheduledExecutor != null) {
            sb.append(getExecutorStatus(scheduledExecutor));
            sb.append(" (Thread Count: ").append(scheduledThreadCounter.get()).append(")");
            if (scheduledExecutor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) scheduledExecutor;
                sb.append(" [Active: ").append(tpe.getActiveCount())
                  .append(", Pool Size: ").append(tpe.getPoolSize())
                  .append(", Queue: ").append(tpe.getQueue().size()).append("]");
            }
        } else {
            sb.append("null");
        }
        sb.append("\n");
        sb.append("Heavy Executor: ");
        if (heavyExecutor != null) {
            sb.append(getExecutorStatus(heavyExecutor));
            sb.append(" (Thread Count: ").append(heavyThreadCounter.get()).append(")");
            if (heavyExecutor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) heavyExecutor;
                sb.append(" [Active: ").append(tpe.getActiveCount())
                  .append(", Pool Size: ").append(tpe.getPoolSize())
                  .append(", Queue: ").append(tpe.getQueue().size()).append("]");
            }
        } else {
            sb.append("null");
        }
        sb.append("\n");
        sb.append("Spotify Polling Executor: ");
        if (spotifyPollingExecutor != null) {
            sb.append(getExecutorStatus(spotifyPollingExecutor));
            sb.append(" (Thread Count: ").append(spotifyThreadCounter.get()).append(")");
            if (spotifyPollingExecutor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) spotifyPollingExecutor;
                sb.append(" [Active: ").append(tpe.getActiveCount())
                  .append(", Pool Size: ").append(tpe.getPoolSize())
                  .append(", Queue: ").append(tpe.getQueue().size()).append("]");
            }
        } else {
            sb.append("null");
        }
        return sb.toString();
    }

    /**
     * Helper method to get executor status
     */
    private String getExecutorStatus(ExecutorService executor) {
        if (executor.isShutdown()) {
            return "Shutdown";
        } else if (executor.isTerminated()) {
            return "Terminated";
        } else {
            return "Running";
        }
    }

    /**
     * called when unloading
     */
    public void shutdown() {
        if (!initialized || shuttingDown) return;
        shuttingDown = true;
        shutdownExecutor(generalExecutor, "General");
        shutdownExecutor(scheduledExecutor, "Scheduled");
        shutdownExecutor(heavyExecutor, "Heavy");
        shutdownExecutor(spotifyPollingExecutor, "Spotify-Polling");
    }

    /**
     * Shuts down a single executor
     */
    private void shutdownExecutor(ExecutorService executor, String name) {
        if (executor == null || executor.isShutdown()) return;

        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                Skyfall.LOGGER.warn("{} executor did not terminate in time, forcing shutdown...", name);
                executor.shutdownNow();
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    Skyfall.LOGGER.error("{} executor did not terminate!", name);
                }
            } else {
                Skyfall.LOGGER.info("{} executor shutdown successfully", name);
            }
        } catch (InterruptedException e) {
            Skyfall.LOGGER.error("{} executor shutdown interrupted", name);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
