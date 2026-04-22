package me.lyric.skyfall.impl.manager;

import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import me.lyric.skyfall.api.utils.shader.BlurShader;
import me.lyric.skyfall.impl.feature.internals.Interface;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author lyric, based on an initial crappy system by savewatr
 */
public final class Notifications {

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        Notification.onRender();
    }

    public void notify(String title, String description, double duration, String icon) {
        Notification.send(title, description, duration, icon);
    }

    private static class Notification implements Globals {
        private static final List<Notification> notifications = new CopyOnWriteArrayList<>();

        private final String title;
        private final String description;
        private final double duration;
        private final String icon;
        private final long startTime;
        private final long endTime;

        private Notification(String title, String description, double duration, String icon) {
            this.title = title;
            this.description = description;
            this.duration = duration;
            this.icon = icon;
            this.startTime = System.currentTimeMillis();
            this.endTime = startTime + (long) duration;
        }

        private static void send(String title, String description, double duration, String icon) {
            notifications.add(new Notification(title, description, duration, icon));
        }

        private static void onRender() {
            if (notifications.isEmpty()) return;

            notifications.removeIf(n -> n.endTime <= System.currentTimeMillis());
            ScaledResolution sr = new ScaledResolution(mc);
            double y = sr.getScaledHeight() - 3;

            List<NotificationRenderData> renderData = new LinkedList<>();
            for (Notification notification : notifications) {
                double width = Math.max(150.0, Managers.TEXT.stringWidth(notification.title) + 10.0);
                double lineSpacing = Managers.TEXT.stringHeight() + 2;
                int iconOffset = (notification.icon != null) ? 15 : 0;
                List<String> lines = wrapText(notification.description, width - lineSpacing - iconOffset * 1.8);
                double height = 20.0 + lines.size() * lineSpacing;

                double x = sr.getScaledWidth() - width - 2;
                long elapsedTime = System.currentTimeMillis() - notification.startTime;
                long remainingTime = notification.endTime - System.currentTimeMillis();
                x += notification.calculateX(elapsedTime, width) + notification.calculateX(remainingTime, width);

                double adjustedX = x - iconOffset;
                double adjustedWidth = width + iconOffset;
                double adjustedHeight = height + iconOffset;
                if (notification.icon != null && (notification.title.isEmpty() || notification.description.isEmpty())) adjustedHeight += lineSpacing;

                renderData.add(new NotificationRenderData(notification, (float) adjustedX, (float) (y - adjustedHeight), (float) adjustedWidth, (float) adjustedHeight, lines, iconOffset));
                adjustedHeight *= notification.calculateH(elapsedTime) * notification.calculateH(remainingTime);
                y -= adjustedHeight + 2;
            }

            BlurShader.blur(10, () -> {
                for (NotificationRenderData data : renderData) {
                    RenderUtils.rounded(data.x, data.y, data.x + data.width + 0.5f, data.y + data.height + 0.5f, 4, new Color(32, 32, 32, 120));
                }
            });

            for (NotificationRenderData data : renderData) {
                data.notification.drawContent(data);
            }
        }

        private static class NotificationRenderData {
            final Notification notification;
            final float x, y, width, height;
            final List<String> lines;
            final int iconOffset;

            NotificationRenderData(Notification notification, float x, float y, float width, float height, List<String> lines, int iconOffset) {
                this.notification = notification;
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
                this.lines = lines;
                this.iconOffset = iconOffset;
            }
        }

        private void drawContent(NotificationRenderData data) {
            float x = data.x;
            float y = data.y;
            float width = data.width;
            float height = data.height;

            RenderUtils.rounded(x, y, x + width + 0.5f, y + height + 0.5f, 4, new Color(32, 32, 32, 120));

            long elapsed = System.currentTimeMillis() - startTime;
            double rawProgress = Math.min(elapsed / duration, 1.0);
            double easedProgress = Easing.easeOutQuad(rawProgress);
            float progressBarHeight = 3.0f;
            float progressWidth = (float) (width * easedProgress);

            if (progressWidth > 0.5f) {
                RenderUtils.rounded(x, y + height - progressBarHeight, x + progressWidth, y + height, 1.5f, Managers.FEATURES.get(Interface.class).notificationColour.getValue());
            }

            int iconOffset = 0;

            if (icon != null) {
                ResourceLocation texture = new ResourceLocation("textures/icons/" + icon);
                RenderUtils.textureSmooth((int) x + 10, (int) (y + (height - 26) / 2), 26 + x + 10, 26 + (y + (height - 26) / 2), Color.WHITE, texture);
                iconOffset = 43;
            }

            List<String> lines = data.lines;
            double textX = x + iconOffset + 3;
            double textY = y + (height - ((lines.size() + 1) * Managers.TEXT.stringHeight() + 2)) / 2;

            Managers.TEXT.guiString(this.title, (float) textX, (float) textY, Color.WHITE);
            for (int i = 0; i < lines.size(); i++) {
                Managers.TEXT.guiString(lines.get(i), (float) textX + 3, (float) textY + (Managers.TEXT.stringHeight() + 4) * (i + 1), Color.GRAY);
            }
        }

        private double calculateX(long time, double width) {
            if (time >= 100L && time <= 250L) {
                double progress = (250.0 - time) / 150.0;
                return Easing.easeInQuad(progress) * (width + 2);
            }
            return (time < 100L) ? Double.MAX_VALUE : 0.0;
        }

        private double calculateH(long time) {
            return (time < 100L) ? Easing.easeInQuad(time / 100.0) : 1.0;
        }

        private static List<String> wrapText(String text, double width) {
            List<String> wrappedLines = new LinkedList<>();
            for (String word : text.split(" ")) {
                if (wrappedLines.isEmpty() || Managers.TEXT.stringWidth(wrappedLines.get(wrappedLines.size() - 1) + word) > width) {
                    wrappedLines.add(word);
                } else {
                    wrappedLines.set(wrappedLines.size() - 1, wrappedLines.get(wrappedLines.size() - 1) + " " + word);
                }
            }
            return wrappedLines;
        }

        private static class Easing {
            public static double easeOutQuad(double t) {
                return t * (2 - t);
            }

            public static double easeInQuad(double t) {
                return t * t;
            }
        }
    }
}
