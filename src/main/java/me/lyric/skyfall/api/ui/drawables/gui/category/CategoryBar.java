package me.lyric.skyfall.api.ui.drawables.gui.category;

import me.lyric.skyfall.api.event.bus.EventBus;
import me.lyric.skyfall.api.event.bus.ITheAnnotation;
import me.lyric.skyfall.api.feature.Category;
import me.lyric.skyfall.api.feature.Feature;
import me.lyric.skyfall.api.manager.Managers;
import me.lyric.skyfall.api.ui.Interface;
import me.lyric.skyfall.api.ui.drawables.Drawable;
import me.lyric.skyfall.api.ui.drawables.gui.feature.FeatureButton;
import me.lyric.skyfall.api.utils.maths.MathsUtils;
import me.lyric.skyfall.api.utils.render.RenderUtils;
import me.lyric.skyfall.api.utils.shader.BlurShader;
import me.lyric.skyfall.api.utils.shader.ShadowShader;
import me.lyric.skyfall.impl.event.mc.ScrollEvent;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;

public class CategoryBar implements Drawable {
    private final ArrayList<FeatureButton> featureButtons = new ArrayList<>();
    private final ResourceLocation left = new ResourceLocation("textures/icons/other/left.png"), right = new ResourceLocation("textures/icons/other/right.png");
    private final Category category;
    public float x, y, width, height, lAnim, rAnim;
    private float anim, target, shift;

    public CategoryBar(Category category) {
        this.category = category;

        float delta = 1.0f;
        float deltaX = 1.0f;
        boolean neg = true;
        for (Feature feature : Managers.FEATURES.getFeatures()) {
            if (feature.getCategory() == category) {
                featureButtons.add(new FeatureButton(feature, deltaX * 120.0f));
                deltaX -= neg ? delta : -delta;
                neg = !neg;
                delta += 1.0f;
            }
        }
        EventBus.getInstance().register(this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float y = this.y - (110.0f * (1.0f - anim));
        /* Animation */
        anim = MathsUtils.lerp(anim, Interface.getActiveCategory() != null && Interface.getActiveCategory() == category ? 1.0f : 0.0f, Interface.getDelta());
        if (anim < 0.1f) {
            return;
        }

        /* Background */
        RenderUtils.rounded(x, y, x + width, y + height, 10.0f, barColor());

        /* Shadows */
        ShadowShader.shadow(5, 1, () -> {
            /* -> Background */
            RenderUtils.rounded(x, y, x + width, y + height, 10.0f, Color.WHITE);
            /* -> Left Circle */
            RenderUtils.circle(x + 15.0f, y + height / 2.0f - 5.0f, 10.0f, Color.WHITE);
            /* -> Right Circle */
            RenderUtils.circle(x + width - 25.0f, y + height / 2.0f - 5.0f, 10.0f, Color.WHITE);
        });

        /* Left Circle */
        RenderUtils.circle(x + 15.0f, y + height / 2.0f - 5.0f, 10.0f, Interface.shade(5));

        /* Right Circle */
        RenderUtils.circle(x + width - 25.0f, y + height / 2.0f - 5.0f, 10.0f, Interface.shade(5));

        /* Arrow Left */
        RenderUtils.textureSmooth(x + 15.0f, y + height / 2.0f - 5f, x + 25.0f, y + height / 2.0f + 5.0f, new Color(1.0f, 1.0f, 1.0f, lAnim), left);
        lAnim = MathsUtils.lerp(lAnim, insideLeft(mouseX, mouseY, y) ? 1.0f : 0.5f, Interface.getDelta());

        /* Arrow Right */
        RenderUtils.textureSmooth(x + width - 25.0f, y + height / 2.0f - 5f, x + width - 15.0f, y + height / 2.0f + 5.0f, new Color(1.0f, 1.0f, 1.0f, rAnim), right);
        rAnim = MathsUtils.lerp(rAnim, insideRight(mouseX, mouseY, y) ? 1.0f : 0.5f, Interface.getDelta());

        /* Scroll clamping */
        int excess = Math.max((featureButtons.size() - 3), 0);
        int left = (excess + 1) / 2;
        int right = excess / 2;

        target = Math.min(left * 120, target);
        target = Math.max(right * -120, target);

        /* Shift */
        shift = MathsUtils.lerp(shift, target, Interface.getDelta());

        if (Interface.getActiveCategory() == null || Interface.getActiveCategory() != category) {
            target = 0.0f;
        }

        shift = MathsUtils.lerp(shift, target, Interface.getDelta());

        float delta = 1.0f;
        float deltaX = 1.0f;
        boolean neg = true;
        for (FeatureButton featureButton : featureButtons) {
            if (!Interface.search.isEmpty() && !featureButton.getModule().getName().toLowerCase().contains(Interface.search.toLowerCase())) {
                continue;
            }
            featureButton.deltaXTarget = deltaX * 120.0f;
            deltaX -= neg ? delta : -delta;
            neg = !neg;
            delta += 1.0f;
        }

        /* Setup Feature Buttons */
        for (FeatureButton featureButton : featureButtons) {
            if (!Interface.search.isEmpty() && !featureButton.getModule().getName().toLowerCase().contains(Interface.search.toLowerCase())) {
                continue;
            }
            featureButton.x = x + 37.5f + featureButton.deltaX + shift;
            featureButton.y = y + 5.0f;
            featureButton.height = height - 10.0f;
            featureButton.width = 110.0f;
            featureButton.guiY = this.y - 30.0f;
            featureButton.guiX = x + 32.5f;
            featureButton.guiWidth = x + width - 32.5f;
        }

        /* Blur background */
        BlurShader.blur(10.0f, () -> {
            for (FeatureButton featureButton : featureButtons) {
                if (!Interface.search.isEmpty() && !featureButton.getModule().getName().toLowerCase().contains(Interface.search.toLowerCase())) {
                    continue;
                }
                /* Scissor Feature Buttons */
                RenderUtils.prepareScissor(x + 32.5f, this.y - 30.0f, x + width - 32.5f, this.y + height);
                featureButton.background();
                /* Release Scissor */
                RenderUtils.releaseScissor();
            }
        });

        /* Shadow around */
        ShadowShader.shadow(10, 1, () -> {
            for (FeatureButton featureButton : featureButtons) {
                if (!Interface.search.isEmpty() && !featureButton.getModule().getName().toLowerCase().contains(Interface.search.toLowerCase())) {
                    continue;
                }
                /* Scissor Feature Buttons */
                RenderUtils.prepareScissor(x + 32.5f, this.y - 30.0f, x + width - 32.5f, this.y + height);
                featureButton.background();
                /* Release Scissor */
                RenderUtils.releaseScissor();
            }
        });

        /* Render each after blur & setup */
        for (FeatureButton featureButton : featureButtons) {
            if (!Interface.search.isEmpty() && !featureButton.getModule().getName().toLowerCase().contains(Interface.search.toLowerCase())) {
                continue;
            }
            /* Scissor Feature Buttons */
            RenderUtils.prepareScissor(x + 32.5f, this.y - 30.0f, x + width - 32.5f, this.y + height);
            featureButton.drawScreen(mouseX, mouseY, partialTicks);
            /* Release Scissor */
            RenderUtils.releaseScissor();
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (Interface.getActiveCategory() == null || Interface.getActiveCategory() != category || anim < 0.1f) {
            return;
        }

        float y = this.y - (110.0f * (1.0f - anim));
        if (mouseButton == 0) {
            if (insideLeft(mouseX, mouseY, y)) {
                target += 120.0f;
                target = Math.round(target / 120f) * 120;
            } else if (insideRight(mouseX, mouseY, y)) {
                target -= 120.0f;
                target = Math.round(target / 120f) * 120;
            }
        }
        for (FeatureButton featureButton : featureButtons) {
            if (!Interface.search.isEmpty() && !featureButton.getModule().getName().toLowerCase().contains(Interface.search.toLowerCase())) {
                continue;
            }
            if (featureButton.x + featureButton.width > x + 32.5f && featureButton.x < x + width - 32.5f) {
                featureButton.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (Interface.getActiveCategory() == null || Interface.getActiveCategory() != category || anim < 0.1f) {
            return;
        }

        for (FeatureButton featureButton : featureButtons) {
            if (!Interface.search.isEmpty() && !featureButton.getModule().getName().toLowerCase().contains(Interface.search.toLowerCase())) {
                continue;
            }
            featureButton.keyTyped(typedChar, keyCode);
        }
    }

    @ITheAnnotation(priority = -1)
    public void onScroll(ScrollEvent event){
        if (event.getMouseX() > x && event.getMouseX() < x + width && event.getMouseY() > y && event.getMouseY() < y + height) {
            target += event.getAmount() / 5.0f;
        }
    }

    private boolean insideLeft(int mouseX, int mouseY, float y) {
        return mouseX > x + 15.0f && mouseX < x + 25.0f && mouseY > y + height / 2.0f - 5.0f && mouseY < y + height / 2.0f + 5.0f;
    }

    private boolean insideRight(int mouseX, int mouseY, float y) {
        return mouseX > x + width - 25.0f && mouseX < x + width - 15.0f && mouseY > y + height / 2.0f - 5.0f && mouseY < y + height / 2.0f + 5.0f;
    }

    @SuppressWarnings("JavaExistingMethodCanBeUsed")
    public Color barColor() {
        //more scaling stuff
        Color get = Interface.background();
        if (get.getRed() + 20 > 255 || get.getGreen() + 20 > 255 || get.getBlue() + 27 > 255) {
            return get;
        }
        return new Color(get.getRed() + 20, get.getGreen() + 20,get.getBlue() + 27);
    }
}
