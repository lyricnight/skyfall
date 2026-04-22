package me.lyric.skyfall.api.utils.spotify;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.manager.Managers;

import javax.sound.sampled.*;
import java.util.Arrays;

/**
 * visualiser
 */
public class AudioVisualizer {
    private static final int NUM_BARS = 32;
    private static final float[] barHeights = new float[NUM_BARS];
    private static final float SMOOTHING = 0.75f;
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 2048;

    private static volatile boolean running = false;
    private static TargetDataLine audioLine;
    public static boolean audioInitialized = false;

    private static long simulationStartTime = 0;
    private static float bassIntensity = 0.8f;
    private static float midIntensity = 0.6f;
    private static float highIntensity = 0.5f;
    private static float overallEnergy = 0.7f;
    private static int beatPhase = 0;
    private static long lastBeatTime = 0;
    private static volatile float bpm = 110.0f;
    private static final float[] targetHeights = new float[NUM_BARS];
    private static final float[] velocities = new float[NUM_BARS];
    private static int musicalPhrase = 0;
    private static final java.util.Random simRandom = new java.util.Random();

    /**
     * Starts the audio capture thread
     */
    public static void start() {
        if (running) return;

        running = true;
        Managers.THREADS.runHeavy(() -> {
            try {
                audioInitialized = initializeAudioCapture();
                if (audioInitialized) {
                    captureAndProcessAudio();
                } else {
                    Skyfall.LOGGER.error("[AudioVisualizer] Failed to initialize audio capture - using fallback mode");
                    fallbackMode();
                }
            } catch (Exception e) {
                Skyfall.LOGGER.error("[AudioVisualizer] Audio visualizer error: {}", e.getMessage());
                try {
                    fallbackMode();
                } catch (Exception fallbackError) {
                    Skyfall.LOGGER.error("[AudioVisualizer] Fallback mode also failed: {}", fallbackError.getMessage());
                }
            } finally {
                try {
                    cleanup();
                } catch (Exception cleanupError) {
                    Skyfall.LOGGER.error("[AudioVisualizer] Cleanup error: {}", cleanupError.getMessage());
                }
            }
        });
    }

    /**
     * Stops the audio capture
     */
    public static void stop() {
        running = false;
    }

    /**
     * Gets the current bar heights for rendering
     * @return Array of bar heights (0.0 to 1.0)
     */
    public static float[] getBarHeights() {
        synchronized (barHeights) {
            return Arrays.copyOf(barHeights, barHeights.length);
        }
    }

    /**
     * Gets the number of bars in the visualizer
     */
    public static int getNumBars() {
        return NUM_BARS;
    }

    /**
     * Checks if the visualizer is running in simulation mode
     * @return true if in simulation mode (no real audio capture), false if using real audio
     */
    public static boolean isSimulationMode() {
        return !audioInitialized;
    }

    /**
     * Updates the BPM for simulation mode
     * @param newBPM The new BPM to use (must be between 40 and 240)
     */
    public static void updateBPM(float newBPM) {
        if (newBPM >= 40 && newBPM <= 240) {
            bpm = newBPM;
            Skyfall.LOGGER.info("[AudioVisualizer] BPM updated to: {}", newBPM);
        } else {
            Skyfall.LOGGER.warn("[AudioVisualizer] Invalid BPM value: {}, using fallback BPM: {}", newBPM, bpm);

        }
    }

    /**
     * init audio capture
     */
    private static boolean initializeAudioCapture() {
        try {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            Skyfall.LOGGER.info("  Searching for STEREO MIX");
            Skyfall.LOGGER.info("\nAvailable Audio Devices:");
            for (Mixer.Info mixerInfo : mixers) {
                Skyfall.LOGGER.info("  - {}", mixerInfo.getName());
            }

            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
            DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, format);
            for (Mixer.Info mixerInfo : mixers) {
                String name = mixerInfo.getName().toLowerCase();
                String desc = mixerInfo.getDescription().toLowerCase();

                //support for as many loopback devices as possible
                boolean isStereoMix = name.contains("stereo mix") || desc.contains("stereo mix");
                boolean isLoopback = name.contains("loopback") || desc.contains("loopback");
                boolean isWaveOut = name.contains("wave out") || name.contains("what u hear");
                boolean isRecPlayback = name.contains("rec. playback") || name.contains("wave out mix");
                boolean isCableOutput = name.contains("cable output");

                if (isStereoMix || isLoopback || isWaveOut || isRecPlayback || isCableOutput) {
                    Skyfall.LOGGER.info("  [AudioVisualizer] Found potential device: {}", mixerInfo.getName());

                    Mixer mixer = AudioSystem.getMixer(mixerInfo);
                    if (mixer.isLineSupported(lineInfo)) {
                        try {
                            audioLine = (TargetDataLine) mixer.getLine(lineInfo);
                            audioLine.open(format, BUFFER_SIZE * 4);
                            audioLine.start();

                            Skyfall.LOGGER.info("\n[AudioVisualizer] SUCCESS - Audio capture initialized");
                            Skyfall.LOGGER.info("[AudioVisualizer] Device: {}", mixerInfo.getName());
                            return true;
                        } catch (Exception e) {
                            Skyfall.LOGGER.error("  [AudioVisualizer] Failed to open {}: {}", mixerInfo.getName(), e.getMessage());
                        }
                    } else {
                        Skyfall.LOGGER.info("  [AudioVisualizer] Device doesn't support audio capture: {}", mixerInfo.getName());
                    }
                }
            }

            Skyfall.LOGGER.error("\n[AudioVisualizer] STEREO MIX NOT FOUND");
            Skyfall.LOGGER.error("[AudioVisualizer] HOW TO ENABLE STEREO MIX:");
            Skyfall.LOGGER.error("  1. Right-click speaker icon -> 'Sound settings'");
            Skyfall.LOGGER.error("  2. Scroll down -> Click 'More sound settings'");
            Skyfall.LOGGER.error("  3. Go to 'Recording' tab");
            Skyfall.LOGGER.error("  4. Right-click empty space -> Check 'Show Disabled Devices'");
            Skyfall.LOGGER.error("  5. Right-click 'Stereo Mix' -> Click 'Enable'");
            Skyfall.LOGGER.error("  6. Right-click 'Stereo Mix' -> 'Set as Default Device'");
            Skyfall.LOGGER.error("  7. Click OK and restart this game");
            Skyfall.LOGGER.error("[AudioVisualizer] Visualizer will use SIMULATION mode\n");

            return false;
        } catch (Exception e) {
            Skyfall.LOGGER.error("[AudioVisualizer] Audio initialization error: {}", e.getMessage());
            return false;
        }
    }

    private static void captureAndProcessAudio() {
        byte[] buffer = new byte[BUFFER_SIZE * 2];
        float[] samples = new float[BUFFER_SIZE];

        Skyfall.LOGGER.info("[AudioVisualizer] Starting audio capture loop...");

        while (running && !Thread.currentThread().isInterrupted() && audioLine != null) {
            try {
                if (audioLine.available() > 0) {
                    int bytesRead = audioLine.read(buffer, 0, buffer.length);

                    if (bytesRead > 0) {
                        for (int i = 0; i < BUFFER_SIZE && i * 2 < bytesRead; i++) {
                            short sample = (short) ((buffer[i * 2 + 1] << 8) | (buffer[i * 2] & 0xFF));
                            samples[i] = sample / 32768.0f;
                        }

                        float[] frequencies = performFFT(samples);
                        updateBars(frequencies);
                    }
                }

                //determines the update rate of the visualizer
                //currently at 60FPS
                //noinspection BusyWait
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Skyfall.LOGGER.info("[AudioVisualizer] Audio capture thread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Skyfall.LOGGER.error("[AudioVisualizer] Error in capture loop: {}", e.getMessage());
            }
        }

        Skyfall.LOGGER.info("[AudioVisualizer] Audio capture loop ended");
    }

    /**
     * Fallback mode with simulated data when audio capture fails
     */
    private static void fallbackMode() {
        Skyfall.LOGGER.info("[AudioVisualizer] Audio visualizer running in simulation mode");
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                generateSimulatedData();
                //noinspection BusyWait
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Performs Fast Fourier Transform on audio samples
     * university maths finally useful!
     */
    private static float[] performFFT(float[] samples) {
        int n = samples.length;

        float[] real = new float[n];
        float[] imag = new float[n];

        for (int i = 0; i < n; i++) {
            float window = (float) (0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (n - 1)));
            real[i] = samples[i] * window;
            imag[i] = 0;
        }

        fft(real, imag);

        float[] magnitudes = new float[n / 2];
        for (int i = 0; i < n / 2; i++) {
            magnitudes[i] = (float) Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }

        return mapToBarHeights(magnitudes);
    }

    /**
     * In-place Fast Fourier Transform
     */
    private static void fft(float[] real, float[] imag) {
        int n = real.length;

        int j = 0;
        for (int i = 0; i < n - 1; i++) {
            if (i < j) {
                float tempR = real[i];
                float tempI = imag[i];
                real[i] = real[j];
                imag[i] = imag[j];
                real[j] = tempR;
                imag[j] = tempI;
            }
            int k = n / 2;
            while (k <= j) {
                j -= k;
                k /= 2;
            }
            j += k;
        }

        for (int len = 2; len <= n; len *= 2) {
            double angle = -2.0 * Math.PI / len;
            float wlenReal = (float) Math.cos(angle);
            float wlenImag = (float) Math.sin(angle);

            for (int i = 0; i < n; i += len) {
                float wReal = 1.0f;
                float wImag = 0.0f;

                for (int m = 0; m < len / 2; m++) {
                    float uReal = real[i + m];
                    float uImag = imag[i + m];
                    float tReal = wReal * real[i + m + len / 2] - wImag * imag[i + m + len / 2];
                    float tImag = wReal * imag[i + m + len / 2] + wImag * real[i + m + len / 2];

                    real[i + m] = uReal + tReal;
                    imag[i + m] = uImag + tImag;
                    real[i + m + len / 2] = uReal - tReal;
                    imag[i + m + len / 2] = uImag - tImag;

                    float tempW = wReal * wlenReal - wImag * wlenImag;
                    wImag = wReal * wlenImag + wImag * wlenReal;
                    wReal = tempW;
                }
            }
        }
    }

    private static float[] mapToBarHeights(float[] magnitudes) {
        float[] bars = new float[NUM_BARS];
        final float MIN_FREQ = 40.0f;
        final float MAX_FREQ = 10000.0f;
        final float NYQUIST = SAMPLE_RATE / 2.0f;

        for (int i = 0; i < NUM_BARS; i++) {
            double logMin = Math.log(MIN_FREQ);
            double logMax = Math.log(MAX_FREQ);
            double logRange = logMax - logMin;

            float freqStart = (float) Math.exp(logMin + (logRange * i / NUM_BARS));
            float freqEnd = (float) Math.exp(logMin + (logRange * (i + 1) / NUM_BARS));

            int binStart = (int) (freqStart / NYQUIST * magnitudes.length);
            int binEnd = (int) (freqEnd / NYQUIST * magnitudes.length);
            binStart = Math.max(0, Math.min(binStart, magnitudes.length - 1));
            binEnd = Math.max(binStart, Math.min(binEnd, magnitudes.length - 1));

            if (binEnd == binStart) {
                binEnd = Math.min(binStart + 2, magnitudes.length - 1);
            }

            float sum = 0;
            int count = 0;
            for (int j = binStart; j <= binEnd; j++) {
                sum += magnitudes[j];
                count++;
            }
            float averageMagnitude = count > 0 ? sum / count : 0;

            float frequencyScale;
            if (i < 8) {
                frequencyScale = 0.3f;
            } else if (i < 16) {
                float progress = (i - 8) / 8.0f;
                frequencyScale = 0.3f + (progress);
            } else if (i < 24) {
                frequencyScale = 1.5f;
            } else {
                frequencyScale = 1.8f;
            }

            bars[i] = averageMagnitude * frequencyScale;
        }

        float max = 0;
        for (float bar : bars) {
            if (bar > max) max = bar;
        }
        if (max > 0) {
            for (int i = 0; i < NUM_BARS; i++) {
                float normalized = bars[i] / max;
                bars[i] = Math.min(1.0f, (float) Math.pow(normalized, 0.5) * 1.5f);
            }
        }

        return bars;
    }

    /**
     * Updates bar heights with smoothing
     */
    private static void updateBars(float[] newHeights) {
        synchronized (barHeights) {
            for (int i = 0; i < NUM_BARS; i++) {
                if (i < newHeights.length) {
                    barHeights[i] = barHeights[i] * SMOOTHING + newHeights[i] * (1 - SMOOTHING);
                    if (newHeights[i] < barHeights[i]) {
                        //this basically gives it gravity
                        barHeights[i] = Math.max(newHeights[i], barHeights[i] - 0.05f);
                    }
                }
            }
        }
    }

    /**
     * Generates simulated visualizer data when real audio is unavailable
     * @author claude
     */
    private static void generateSimulatedData() {
        if (simulationStartTime == 0) {
            simulationStartTime = System.currentTimeMillis();
            bpm = 100 + simRandom.nextFloat() * 60;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - simulationStartTime;
        float timeInSeconds = elapsedTime / 1000.0f;
        float beatDuration = 60000.0f / bpm;
        boolean isBeat = (currentTime - lastBeatTime) >= beatDuration;

        if (isBeat) {
            lastBeatTime = currentTime;
            beatPhase = (beatPhase + 1) % 16;
            if (beatPhase % 16 == 0) {
                musicalPhrase = (musicalPhrase + 1) % 4;
                updateMusicalIntensity();
            }
            triggerBeatEvent();
        }
        evolveEnergyLevels(timeInSeconds);
        synchronized (barHeights) {
            for (int i = 0; i < NUM_BARS; i++) {
                float newTarget = calculateBarTarget(i, timeInSeconds, isBeat);
                float difference = newTarget - targetHeights[i];
                velocities[i] = velocities[i] * 0.75f + difference * 0.35f; // More responsive
                targetHeights[i] += velocities[i];
                barHeights[i] = barHeights[i] * SMOOTHING + targetHeights[i] * (1 - SMOOTHING);
                if (targetHeights[i] < barHeights[i]) {
                    barHeights[i] = Math.max(targetHeights[i], barHeights[i] - 0.12f); // Faster decay
                }
                barHeights[i] = Math.max(0.0f, Math.min(1.5f, barHeights[i])); // Allow over 1.0
            }
        }
    }

    private static void updateMusicalIntensity() {
        switch (musicalPhrase) {
            case 0: // Build-up
                overallEnergy = 0.85f + simRandom.nextFloat() * 0.15f;
                bassIntensity = 1.0f; // Bass always maxed
                midIntensity = 0.8f + simRandom.nextFloat() * 0.2f;
                highIntensity = 0.75f + simRandom.nextFloat() * 0.25f;
                break;
            case 1: // Peak energy
                overallEnergy = 0.95f + simRandom.nextFloat() * 0.05f;
                bassIntensity = 1.0f; // Bass always maxed
                midIntensity = 0.9f + simRandom.nextFloat() * 0.1f;
                highIntensity = 0.85f + simRandom.nextFloat() * 0.15f;
                break;
            case 2: // Full intensity
                overallEnergy = 1.0f;
                bassIntensity = 1.0f; // Bass always maxed
                midIntensity = 0.95f + simRandom.nextFloat() * 0.05f;
                highIntensity = 0.9f + simRandom.nextFloat() * 0.1f;
                break;
            case 3: // Drop but still energetic
                overallEnergy = 0.75f + simRandom.nextFloat() * 0.25f;
                bassIntensity = 0.95f + simRandom.nextFloat() * 0.05f; // Bass still very strong
                midIntensity = 0.75f + simRandom.nextFloat() * 0.25f;
                highIntensity = 0.8f + simRandom.nextFloat() * 0.2f;
                break;
        }
    }

    /**
     * Smoothly evolves energy levels over time to prevent abrupt changes
     */
    private static void evolveEnergyLevels(float timeInSeconds) {
        float slowWave = (float) Math.sin(timeInSeconds * 0.1) * 0.15f;
        float mediumWave = (float) Math.sin(timeInSeconds * 0.3) * 0.12f;
        float fastWave = (float) Math.sin(timeInSeconds * 0.8) * 0.08f;
        float energyModulation = slowWave + mediumWave + fastWave;
        overallEnergy = Math.max(0.6f, Math.min(1.0f, overallEnergy + energyModulation * 0.3f));
    }

    /**
     * Triggers beat-synchronized events (kick drum, snare, hi-hat simulation)
     */
    private static void triggerBeatEvent() {
        if (beatPhase % 4 == 0) {
            bassIntensity = 1.0f;
        } else if (beatPhase % 4 == 2) {
            bassIntensity = Math.min(1.0f, bassIntensity + 0.7f + simRandom.nextFloat() * 0.3f);
        } else {
            bassIntensity = Math.min(1.0f, bassIntensity + 0.5f + simRandom.nextFloat() * 0.3f);
        }

        if (beatPhase % 4 == 1 || beatPhase % 4 == 3) {
            midIntensity = Math.min(1.0f, midIntensity + 0.7f + simRandom.nextFloat() * 0.3f);
        }

        highIntensity = Math.min(1.0f, highIntensity + 0.5f + simRandom.nextFloat() * 0.3f);

        if (simRandom.nextFloat() < 0.5f) {
            int accentBar = simRandom.nextInt(NUM_BARS);
            targetHeights[accentBar] = Math.min(1.0f, targetHeights[accentBar] + 0.6f + simRandom.nextFloat() * 0.4f);
        }

        if (simRandom.nextFloat() < 0.35f) {
            int centerBar = simRandom.nextInt(NUM_BARS);
            for (int i = Math.max(0, centerBar - 2); i < Math.min(NUM_BARS, centerBar + 3); i++) {
                targetHeights[i] = Math.min(1.0f, targetHeights[i] + 0.5f + simRandom.nextFloat() * 0.3f);
            }
        }

        if (simRandom.nextFloat() < 0.4f) {
            for (int i = 0; i < 5; i++) {
                targetHeights[i] = Math.min(1.0f, targetHeights[i] + 0.6f);
            }
        }
    }

    /**
     * Calculates the target height for a specific frequency bar
     * Simulates realistic frequency distribution with beat-synchronized elements
     */
    private static float calculateBarTarget(int barIndex, float time, boolean isBeat) {
        float normalizedPosition = (float) barIndex / NUM_BARS;
        boolean isBass = barIndex < 8;
        boolean isMid = barIndex >= 8 && barIndex < 20;
        boolean isHigh = barIndex >= 20;
        float baseLevel;
        if (isBass) {
            baseLevel = bassIntensity * overallEnergy * 2.0f;
            bassIntensity *= 0.99f;
            if (isBeat && beatPhase % 2 == 0) {
                baseLevel += 0.7f * (1.0f - normalizedPosition);
            }
            float bassPulse = (float) Math.sin(time * 1.5 + barIndex * 0.3) * 0.5f;
            baseLevel += Math.abs(bassPulse);
        }
        else if (isMid) {
            baseLevel = midIntensity * overallEnergy * 1.5f;
            midIntensity *= 0.99f;
            float melodicWave = (float) Math.sin(time * 2.0 + barIndex * 0.5) * 0.6f;
            baseLevel += Math.abs(melodicWave);
        }
        else {
            baseLevel = highIntensity * overallEnergy * 1.3f;
            highIntensity *= 0.98f;
            float shimmer = (float) Math.sin(time * 8.0 + barIndex * 1.2) * 0.4f;
            baseLevel += Math.abs(shimmer);
        }
        float noise = (simRandom.nextFloat() - 0.5f) * 0.4f * overallEnergy;
        baseLevel += noise;

        float sweepPhase = (time * 0.5f) % 4.0f;
        if (sweepPhase > 2.0f && sweepPhase < 3.0f) {
            float sweepProgress = (sweepPhase - 2.0f);
            float sweepTarget = sweepProgress * NUM_BARS;
            float distance = Math.abs(barIndex - sweepTarget);
            if (distance < 3.0f) {
                baseLevel += (1.0f - distance / 3.0f) * 0.7f * overallEnergy;
            }
        }

        baseLevel *= (1.0f - normalizedPosition * 0.05f);

        return Math.max(0.2f, Math.min(1.5f, baseLevel));
    }

    /**
     * Cleans up audio resources
     */
    private static void cleanup() {
        if (audioLine != null) {
            audioLine.stop();
            audioLine.close();
            audioLine = null;
        }
    }
}

