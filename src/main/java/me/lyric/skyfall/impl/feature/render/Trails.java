package me.lyric.skyfall.impl.feature.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.setting.types.*;
import me.lyric.skyfall.api.utils.interfaces.Globals;
import me.lyric.skyfall.api.utils.interfaces.ducks.IRenderManager;
import me.lyric.skyfall.api.utils.nulls.Null;
import me.lyric.skyfall.api.utils.render.MathAnimation;
import me.lyric.skyfall.impl.event.network.PacketEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author lyric
 * renders line tracers that fade out
 */
@SuppressWarnings("unused")
public final class Trails extends Feature {

    public IntegerSetting time = setting("Time", 2, 1, 10).invokeTab("Rendering");

    public ModeSetting colourMode = setting("Colour Mode", "Gradient", Arrays.asList("Static", "Gradient")).invokeTab("Rendering");

    public ColourSetting colour = setting("Colour", Color.BLUE).invokeTab("Rendering");

    public ColourSetting colour2 = setting("Colour 2", Color.RED).invokeTab("Rendering").invokeVisibility(s -> colourMode.getValue().equals("Gradient"));

    public FloatSetting width = setting("Width", 3.5f, 0.1f, 10.0f).invokeTab("Rendering");

    public IntegerSetting cutoff = setting("Cutoff", 5, 1, 256).invokeTab("Rendering");

    public IntegerSetting maxTracePoints = setting("Max Points", 500, 50, 2000).invokeTab("Rendering");

    public BooleanSetting pearls = setting("Pearls", true).invokeTab("Entities");

    public BooleanSetting arrows = setting("Arrows", false).invokeTab("Entities");

    private final Map<Integer, MathAnimation> ids = new ConcurrentHashMap<>();

    private final Map<Integer, List<Trace>> traceList = new ConcurrentHashMap<>();

    private final Map<Integer, Trace> traces = new ConcurrentHashMap<>();

    /**
     * origin of the localisation for Vec3
     */
    public static final Vec3 ORIGIN = new Vec3(8.0, 64.0, 8.0);


    public Trails() {
        super("Trails", Category.Render);
    }

    @Override
    public void onDisable() {
        ids.clear();
        traceList.clear();
        traces.clear();
    }

    /**
     * this is actually called too early but who cares about right click delay? Right? I think?
     * not that it has anything to do with what this does
     */
    @Override
    public void onTickPre()
    {
        if (Null.is()) return;
        if (ids.isEmpty()) return;

        ids.entrySet().removeIf(entry -> {
            Integer id = entry.getKey();
            MathAnimation animation = entry.getValue();

            if (id == null || animation == null) return true;

            Entity entity = mc.theWorld.getEntityByID(id);
            if (entity != null)
            {
                Vec3 vec = entity.getPositionVector();
                if (vec == null || vec.equals(ORIGIN)) return false;

                Trace idTrace = traces.get(id);
                if (idTrace == null)
                {
                    traces.put(id, new Trace(0, null, vec, new ArrayList<>()));
                    idTrace = traces.get(id);
                }

                List<Trace.TracePos> trace = idTrace.getTrace();
                Vec3 vec3d = trace.isEmpty() ? vec : trace.get(trace.size() - 1).getPos();

                double distSq = vec.squareDistanceTo(vec3d);
                if (!trace.isEmpty() && distSq > 10000.0)
                {
                    traceList.get(id).add(idTrace);
                    traces.put(id, new Trace(traceList.get(id).size(), null, vec, new ArrayList<>()));
                    idTrace = traces.get(id);
                    trace = idTrace.getTrace();
                }

                if (trace.isEmpty() || !vec.equals(vec3d))
                {
                    trace.add(new Trace.TracePos(vec));
                    int maxPoints = maxTracePoints.getValue();
                    while (trace.size() > maxPoints) {
                        trace.remove(0);
                    }
                }

                if (entity instanceof EntityArrow && (entity.onGround || entity.isCollided || !entity.isAirBorne)) {
                    animation.play();
                }
            }
            else
            {
                if (!animation.isPlaying()) {
                    animation.play();
                }
            }

            if (animation.isPlaying() && colour.getValue().getAlpha() - animation.getCurrent() <= 0) {
                animation.stop();
                traceList.remove(id);
                traces.remove(id);
                return true;
            }
            return false;
        });
    }


    @Override
    public void onRender3D() {
        if (traceList.isEmpty()) return;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        GlStateManager.disableLighting();
        GL11.glLineWidth(width.getValue());

        boolean isGradient = colourMode.getValue().equals("Gradient");
        int baseAlpha = colour.getValue().getAlpha();
        double renderPosX = ((IRenderManager) mc.getRenderManager()).getRenderPosX();
        double renderPosY = ((IRenderManager) mc.getRenderManager()).getRenderPosY();
        double renderPosZ = ((IRenderManager) mc.getRenderManager()).getRenderPosZ();

        int r1 = colour.getValue().getRed();
        int g1 = colour.getValue().getGreen();
        int b1 = colour.getValue().getBlue();
        int r2 = 0, g2 = 0, b2 = 0;

        if (isGradient) {
            r2 = colour2.getValue().getRed();
            g2 = colour2.getValue().getGreen();
            b2 = colour2.getValue().getBlue();
        }
        worldRenderer.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (Map.Entry<Integer, List<Trace>> entry : traceList.entrySet()) {
            MathAnimation animation = ids.get(entry.getKey());
            if (animation == null) continue;

            animation.add();
            float alpha = MathHelper.clamp_float((baseAlpha - (float) animation.getCurrent()) / 255.0f, 0.0f, 1.0f);
            int alphaInt = (int)(alpha * 255);

            for (Trace trace : entry.getValue()) {
                addTraceToBuffer(worldRenderer, trace.getTrace(), renderPosX, renderPosY, renderPosZ, isGradient, r1, g1, b1, r2, g2, b2, alphaInt);
            }

            Trace currentTrace = traces.get(entry.getKey());
            if (currentTrace != null) {
                addTraceToBuffer(worldRenderer, currentTrace.getTrace(), renderPosX, renderPosY, renderPosZ, isGradient, r1, g1, b1, r2, g2, b2, alphaInt);
            }
        }
        tessellator.draw();
        glDisable(GL_LINE_SMOOTH);
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    /**
     * Helper method to add trace line segments to a shared WorldRenderer buffer
     * uses GL_LINES (each segment needs 2 vertices) for batched rendering.
     */
    private void addTraceToBuffer(WorldRenderer worldRenderer, List<Trace.TracePos> tracePositions, double renderPosX, double renderPosY, double renderPosZ, boolean isGradient, int r1, int g1, int b1, int r2, int g2, int b2, int alpha) {
        int size = tracePositions.size();
        if (size < 2) return;

        for (int i = 0; i < size - 1; i++) {
            Trace.TracePos tracePos1 = tracePositions.get(i);
            Trace.TracePos tracePos2 = tracePositions.get(i + 1);
            Vec3 pos1 = tracePos1.getPos();
            Vec3 pos2 = tracePos2.getPos();

            double x1 = pos1.xCoord - renderPosX;
            double y1 = pos1.yCoord - renderPosY;
            double z1 = pos1.zCoord - renderPosZ;
            double x2 = pos2.xCoord - renderPosX;
            double y2 = pos2.yCoord - renderPosY;
            double z2 = pos2.zCoord - renderPosZ;

            if (isGradient) {
                float progress1 = (float) i / (size - 1);
                float progress2 = (float) (i + 1) / (size - 1);
                int cr1 = (int) (r1 + (r2 - r1) * progress1);
                int cg1 = (int) (g1 + (g2 - g1) * progress1);
                int cb1 = (int) (b1 + (b2 - b1) * progress1);
                int cr2 = (int) (r1 + (r2 - r1) * progress2);
                int cg2 = (int) (g1 + (g2 - g1) * progress2);
                int cb2 = (int) (b1 + (b2 - b1) * progress2);
                worldRenderer.pos(x1, y1, z1).color(cr1, cg1, cb1, alpha).endVertex();
                worldRenderer.pos(x2, y2, z2).color(cr2, cg2, cb2, alpha).endVertex();
            } else {
                worldRenderer.pos(x1, y1, z1).color(r1, g1, b1, alpha).endVertex();
                worldRenderer.pos(x2, y2, z2).color(r1, g1, b1, alpha).endVertex();
            }
        }
    }

    @ITheAnnotation
    @SuppressWarnings("unused")
    public void onPacketReceive(PacketEvent.Receive event) {
        if (Null.is()) return;
        if (event.getPacket() instanceof S0EPacketSpawnObject) {
            S0EPacketSpawnObject packet = (S0EPacketSpawnObject) event.getPacket();
            if ((packet.getType() == 65 && pearls.getValue()) || (packet.getType() == 60 && arrows.getValue())) {
                Vec3 entityPos = new Vec3(packet.getX() / 32.0, packet.getY() / 32.0, packet.getZ() / 32.0);
                Vec3 playerPos = mc.thePlayer.getPositionVector();
                int cutoffValue = cutoff.getValue();
                double distanceSq = entityPos.squareDistanceTo(playerPos);
                if (distanceSq > cutoffValue * cutoffValue) {
                    return;
                }
                MathAnimation animation = new MathAnimation(time.getValue() * 1000, 0, colour.getValue().getAlpha(), false, MathAnimation.AnimationMode.LINEAR);
                animation.stop();
                ids.put(packet.getEntityID(), animation);
                traceList.put(packet.getEntityID(), new ArrayList<>());
                traces.put(packet.getEntityID(), new Trace(0, null, new Vec3(packet.getX(), packet.getY(), packet.getZ()), new ArrayList<>()));
            }
        }
        else if (event.getPacket() instanceof S13PacketDestroyEntities)
        {
            for (int id : ((S13PacketDestroyEntities) event.getPacket()).getEntityIDs())
            {
                if (ids.containsKey(id))
                {
                    ids.get(id).play();
                }
            }
        }

    }

    @Override
    public String displayAppend()
    {
        if (Null.is()) return "";
        return " " + ids.size();
    }

    /**
     * a class that represents all traces of things. This keeps a track of everything.
     */
    @Setter
    @Getter
    @AllArgsConstructor
    private static class Trace implements Globals
    {
        private int index;
        private String name;
        private Vec3 pos;
        private List<TracePos> trace;

        @Getter
        private static class TracePos {
            private final Vec3 pos;

            public TracePos(Vec3 pos) {
                this.pos = pos;
            }
        }
    }
}
