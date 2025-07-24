package ru.drughack.modules.impl.render;

import net.minecraft.util.math.MathHelper;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.animations.Animation;
import ru.drughack.utils.animations.Easing;

public class Zoom extends Module {

    public Setting<Float> zoom = new Setting<>("Zoom", 0.5f, 0.01f, 1f);
    public Setting<Boolean> scroll = new Setting<>("Scroll", true);
    public Setting<Float> scrollFactor = new Setting<>("Scroll Factor", 0.3f, 0.1f, 0.65f, v -> scroll.getValue());

    public Zoom() {
        super("Zoom", "zoom like optifine", Category.Render);
    }

    private final Animation animation = new Animation(Easing.CUBIC_OUT, 750);
    private float needZoomValue;
    private float currentZoomValue;

    @Override
    public void onEnable() {
        currentZoomValue = 1;
        animation.reset();
        needZoomValue = 1 / zoom.getValue();
        super.onEnable();
    }

    public void mouseScroll(float step) {
        if (scroll.getValue()) {
            needZoomValue = Math.max(1, needZoomValue + (step * scrollFactor.getValue() * needZoomValue));
            animation.reset();
        }
    }

    public float getFov(float original) {
        currentZoomValue = (float) MathHelper.lerp(animation.getEase(), currentZoomValue, needZoomValue);
        return original / currentZoomValue;
    }
}