package me.lyric.skyfall.asm.mixin;

import me.lyric.skyfall.Skyfall;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.utils.render.ColourUtils;
import me.lyric.skyfall.api.utils.render.image.NamedImage;
import me.lyric.skyfall.api.utils.shader.GradientShader;
import me.lyric.skyfall.impl.feature.internals.MainMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(GuiMainMenu.class)
public abstract class MixinMainMenu extends GuiScreen {
    @Unique
    private long skyfall$initTime;

    @Inject(method ="initGui", at = @At("RETURN"))
    private void initGuiHook(CallbackInfo info) {
        skyfall$initTime = System.currentTimeMillis();
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiMainMenu;renderSkybox(IIF)V", shift = At.Shift.AFTER))
    public void drawScreenAfterSkybox(int mouseX, int mouseY, float partialTicks, CallbackInfo ci)
    {
        if (Managers.FEATURES.get(MainMenu.class).isEnabled() && Managers.FEATURES.get(MainMenu.class).mainMenuMode.getValue().equals("Shader") && Managers.FEATURES.get(MainMenu.class).getSHADER() != null)
        {
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.disableCull();
            GlStateManager.disableDepth();
            GlStateManager.disableAlpha();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Managers.FEATURES.get(MainMenu.class).getSHADER().useShader(this.width, this.height, mouseX, mouseY, (System.currentTimeMillis() - skyfall$initTime) / 1000f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(-1.0f, -1.0f);
            GL11.glVertex2f(-1.0f, 1.0f);
            GL11.glVertex2f(1.0f, 1.0f);
            GL11.glVertex2f(1.0f, -1.0f);
            GL11.glEnd();
            GL20.glUseProgram(0);
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.popMatrix();
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }
        else if (Managers.FEATURES.get(MainMenu.class).isEnabled() && Managers.FEATURES.get(MainMenu.class).mainMenuMode.getValue().equals("Image"))
        {
            NamedImage image = Managers.IMAGES.getImageByName(Managers.FEATURES.get(MainMenu.class).imagePath.getValue());

            if (image != null && image.getTextureStore() != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.disableDepth();
                GlStateManager.disableAlpha();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                GlStateManager.bindTexture(image.getTextureStore().getGlTextureId());
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glTexCoord2f(0.0f, 0.0f);
                GL11.glVertex2f(0, 0);
                GL11.glTexCoord2f(0.0f, 1.0f);
                GL11.glVertex2f(0, this.height);
                GL11.glTexCoord2f(1.0f, 1.0f);
                GL11.glVertex2f(this.width, this.height);
                GL11.glTexCoord2f(1.0f, 0.0f);
                GL11.glVertex2f(this.width, 0);
                GL11.glEnd();
                GlStateManager.enableAlpha();
                GlStateManager.enableDepth();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }


    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiMainMenu;drawString(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;III)V"))
    public void drawScreenInvokeHook(int mouseX, int mouseY, float partialTicks, CallbackInfo ci)
    {
        GradientShader.setup(0.3f, 3f, ColourUtils.getGlobalColor()[0], ColourUtils.getGlobalColor()[1]);
        Managers.TEXT.stringNoShadow(Skyfall.NAME + " " + Skyfall.VERSION, 2, 2, Color.WHITE, 24);
        Managers.TEXT.stringNoShadow("from london with love - lyric", 2, 2 + Managers.TEXT.stringHeight() + 5, Color.WHITE);
        GradientShader.finish();
    }
}
