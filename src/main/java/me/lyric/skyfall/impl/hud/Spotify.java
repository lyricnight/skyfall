package me.lyric.skyfall.impl.hud;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.hud.HUDBase;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.setting.types.ActionSetting;
import me.lyric.skyfall.api.setting.types.BooleanSetting;
import me.lyric.skyfall.api.setting.types.IntegerSetting;
import me.lyric.skyfall.api.utils.exception.ExceptionHandler;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import me.lyric.skyfall.api.utils.spotify.AudioVisualizer;
import me.lyric.skyfall.api.utils.spotify.SpotifyAlbumArtFetcher;
import me.lyric.skyfall.api.utils.spotify.SpotifyBPMFetcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

public class Spotify extends HUDBase {

    public BooleanSetting compactMode = settingHUD("Compact Mode", false);
    public BooleanSetting lowercase = settingHUD("Lowercase", false);
    public ActionSetting resetAlbumArt = settingHUD("Reset", "Reset Album Covers", SpotifyAlbumArtFetcher::clearCache);
    public IntegerSetting bpm = settingHUD("BPM", 110, 40, 240);

    public Spotify() {
        super("Spotify");
        x = 10;
        y = 10;
        width = 300;
        height = 70;
    }

    public static String song;
    static String lastSong;
    static DynamicTexture albumArt;
    static CompletableFuture<BufferedImage> albumArtFuture;

    private static volatile boolean shouldPoll = true;

    private float scrollOffsetTrack = 0;
    private float scrollOffsetArtist = 0;
    private long lastScrollUpdate = 0;
    private boolean scrollDirectionTrack = true;
    private boolean scrollDirectionArtist = true;
    private long pauseStartTrack = 0;
    private long pauseStartArtist = 0;
    private static final float SCROLL_SPEED = 30.0f;
    private static final long PAUSE_DURATION = 2500;

    @Override
    public void onRender2D() {
        if (song == null || song.equals("Spotify") || song.equals("TIDAL")) return;

        String[] parts = song.split(" - ", 2);
        if (parts.length != 2) return;

        String artist = parts[0];
        String trackName = parts[1].startsWith(" - ") ? parts[1].substring(3) : parts[1];
        final String originalTrackName = trackName;
        final String originalArtist = artist;
        if (lowercase.getValue()) {
            artist = artist.toLowerCase();
            trackName = trackName.toLowerCase();
        }

        if (!song.equals(lastSong)) {
            lastSong = song;
            albumArt = null;
            albumArtFuture = SpotifyAlbumArtFetcher.fetchAlbumArt(originalTrackName, originalArtist);
            scrollOffsetTrack = 0;
            scrollOffsetArtist = 0;
            scrollDirectionTrack = true;
            scrollDirectionArtist = true;
            pauseStartTrack = 0;
            pauseStartArtist = 0;
            lastScrollUpdate = 0;

            if (AudioVisualizer.isSimulationMode()) {
                SpotifyBPMFetcher.fetchBPM(originalTrackName, originalArtist).thenAccept(bpm -> {
                    if (bpm != null) {
                        AudioVisualizer.updateBPM(bpm);
                        Skyfall.LOGGER.info("[Spotify] Updated AudioVisualizer BPM to {} for: {} (simulation mode)", bpm, originalTrackName);
                    } else {
                        throw new RuntimeException("BPM fetch returned null - even with fallback variable usage! This should be impossible!");
                    }
                });
            }
        }
        if (albumArtFuture != null && albumArtFuture.isDone() && albumArt == null) {
            try {
                BufferedImage image = albumArtFuture.getNow(null);
                if (image != null) {
                    Skyfall.LOGGER.info("Creating album art texture for: {}", trackName);
                    albumArt = new DynamicTexture(image);
                    Skyfall.LOGGER.info("Album art texture created successfully for: {}", trackName);
                } else {
                    Skyfall.LOGGER.warn("Album art image was null for: {}", trackName);
                }
                albumArtFuture = null;
            } catch (Exception e) {
                Skyfall.LOGGER.error("Failed to create album art texture for {}: {}", trackName, e.getMessage());
                ExceptionHandler.handle(e, this.getClass());
                albumArtFuture = null;
            }
        }
        boolean compact = compactMode.getValue();

        float albumSize = compact ? 30 : 60;
        float padding = compact ? 4 : 8;
        float visualizerHeight = compact ? 18 : 35;

        float textHeight = getHUDStringHeight();
        float textSpacing = textHeight + 4;

        float trackWidth = getHUDStringWidth(trackName);
        float artistWidth = getHUDStringWidth(artist);

        float visualizerWidth = compact ? 100 : 200;

        float totalWidth = visualizerWidth + albumSize + padding * 3;
        float availableTextWidth = totalWidth - padding * 2;

        float textSectionHeight = textSpacing * 2;
        float bottomSectionHeight = Math.max(albumSize, visualizerHeight);
        float totalHeight = textSectionHeight + bottomSectionHeight + padding;

        width = totalWidth;
        height = totalHeight;

        if (lastScrollUpdate == 0) {
            lastScrollUpdate = System.currentTimeMillis();
        }

        float textX = x + padding;
        float textY = y + padding;

        renderScrollingText(trackName, textX, textY, availableTextWidth, trackWidth, true);
        renderScrollingText(artist, textX, textY + textSpacing, availableTextWidth, artistWidth, false);

        float bottomY = y + textSectionHeight + 2;
        float visualizerX = x + padding;
        float visualizerY = bottomY + bottomSectionHeight - visualizerHeight;
        float albumX = x + totalWidth - albumSize - padding;
        float albumY = bottomY + bottomSectionHeight - albumSize;

        drawVisualizer(visualizerX, visualizerY, visualizerWidth, visualizerHeight, compact);

        if (albumArt != null) {
            drawAlbumArt(albumX, albumY, albumSize, albumSize);
        }
    }

    /**
     * Draws the album art texture
     */
    private void drawAlbumArt(float x, float y, float width, float height) {
        if (albumArt == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        GlStateManager.bindTexture(albumArt.getGlTextureId());

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2f(x, y + height);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /**
     * Renders text with scrolling animation if it exceeds available width
     */
    private void renderScrollingText(String text, float x, float y, float availableWidth, float textWidth, boolean isTrack) {
        if (textWidth <= availableWidth) {
            if (isTrack) {
                scrollOffsetTrack = 0;
                scrollDirectionTrack = true;
                pauseStartTrack = 0;
            } else {
                scrollOffsetArtist = 0;
                scrollDirectionArtist = true;
                pauseStartArtist = 0;
            }

            renderWithShader(() -> drawHUDString(text, x, y, getRenderColor()));
        } else {
            float maxScroll = textWidth - availableWidth;
            float currentOffset = isTrack ? scrollOffsetTrack : scrollOffsetArtist;
            boolean scrollDirection = isTrack ? scrollDirectionTrack : scrollDirectionArtist;
            long pauseStart = isTrack ? pauseStartTrack : pauseStartArtist;

            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastScrollUpdate) / 1000.0f;
            lastScrollUpdate = currentTime;

            if (pauseStart > 0) {
                if (currentTime - pauseStart >= PAUSE_DURATION) {
                    if (isTrack) {
                        pauseStartTrack = 0;
                        scrollDirectionTrack = !scrollDirectionTrack;
                    } else {
                        pauseStartArtist = 0;
                        scrollDirectionArtist = !scrollDirectionArtist;
                    }
                }
            } else {
                if (scrollDirection) {
                    currentOffset += SCROLL_SPEED * deltaTime;
                    if (currentOffset >= maxScroll) {
                        currentOffset = maxScroll;
                        if (isTrack) {
                            pauseStartTrack = currentTime;
                        } else {
                            pauseStartArtist = currentTime;
                        }
                    }
                } else {
                    currentOffset -= SCROLL_SPEED * deltaTime;
                    if (currentOffset <= 0) {
                        currentOffset = 0;
                        if (isTrack) {
                            pauseStartTrack = currentTime;
                        } else {
                            pauseStartArtist = currentTime;
                        }
                    }
                }

                if (isTrack) {
                    scrollOffsetTrack = currentOffset;
                } else {
                    scrollOffsetArtist = currentOffset;
                }
            }

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            int scaleFactor = mc.gameSettings.guiScale;
            if (scaleFactor == 0) scaleFactor = 1000;

            float textHeightWithPadding = getHUDStringHeight() + 4;
            float yOffset = 2;
            GL11.glScissor(
                (int)(x * scaleFactor),
                (int)(mc.displayHeight - (y + textHeightWithPadding - yOffset) * scaleFactor),
                (int)(availableWidth * scaleFactor),
                (int)(textHeightWithPadding * scaleFactor)
            );

            final float finalX = x - currentOffset;
            final float finalY = y;
            final String finalText = text;

            renderWithShader(() -> drawHUDString(finalText, finalX, finalY, getRenderColor()));

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    /**
     * Draws the audio visualizer bars
     */
    private void drawVisualizer(float x, float y, float width, float height, boolean compact) {
        float[] barHeights = AudioVisualizer.getBarHeights();
        if (barHeights.length == 0) {
            return;
        }

        int totalBars = AudioVisualizer.getNumBars();

        int displayBars = compact ? 12 : totalBars;

        float barWidth = (width - (displayBars - 1)) / displayBars;
        float spacing = 1.0f;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (int i = 0; i < displayBars; i++) {
            int barIndex = compact ? (int) ((float) i / displayBars * totalBars) : i;

            float barX = x + i * (barWidth + spacing);
            float barHeight = barHeights[barIndex] * height;
            float barY = y + height - barHeight;

            Color color;
            if (isGradient()) {
                Color[] gradient = getGradient();
                float ratio = (float) i / displayBars;
                color = interpolateColor(gradient[0], gradient[1], ratio);
            } else {
                color = interpolateColor(new Color(34, 197, 94), new Color(239, 68, 68), barHeights[barIndex]);
            }

            RenderUtils.rect(barX, barY, barX + barWidth, y + height, color);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    /**
     * Interpolates between two colors
     */
    private Color interpolateColor(Color c1, Color c2, float ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * ratio);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * ratio);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * ratio);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * ratio);
        return new Color(r, g, b, a);
    }

    public static void getSpotify() throws InterruptedException {
        char[] buffer = new char[1024];
            User32.INSTANCE.EnumWindows((WinDef.HWND hWnd, Pointer data) -> {
                if (!User32.INSTANCE.IsWindowVisible(hWnd)) return true;

                User32.INSTANCE.GetWindowText(hWnd, buffer, 1024);
                String windowTitle = Native.toString(buffer);

                IntByReference pid = new IntByReference();
                User32.INSTANCE.GetWindowThreadProcessId(hWnd, pid);
                int processID = pid.getValue();

                String processName = getProcessName(processID);

                if ("Spotify.exe".equalsIgnoreCase(processName) && !windowTitle.isEmpty()) {
                    song = windowTitle;
                } else if ("TIDAL.exe".equalsIgnoreCase(processName) && !windowTitle.isEmpty()) {
                    song = windowTitle;
                }
                return true;
            }, Pointer.NULL);
    }

    private static String getProcessName(int pid) {
        Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
        try {
            if (Kernel32.INSTANCE.Process32First(snapshot, processEntry)) {
                do {
                    if (processEntry.th32ProcessID.intValue() == pid) {
                        return Native.toString(processEntry.szExeFile);
                    }
                } while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry));
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }
        return "unknown";
    }

    public static void stopPolling() {
        shouldPoll = false;
    }

    /**
     * Gets the current song playing on Spotify/TIDAL
     * @return the current song title, or null if no song is playing
     */
    public static String getCurrentSong() {
        return song;
    }
}
