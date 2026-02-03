package fun.drughack.screen.clickgui.components.impl;

import fun.drughack.modules.settings.impl.NumberSetting;
import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.animations.infinity.InfinityAnimation;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.ColorUtils;
import fun.drughack.utils.render.fonts.Fonts;
import fun.drughack.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class SliderComponent extends Component {

    private final NumberSetting setting;
    private final InfinityAnimation animation = new InfinityAnimation(Easing.LINEAR);
    private boolean drag;

    public SliderComponent(NumberSetting setting) {
        super(setting.getName());
        this.setting = setting;
        this.addHeight = () -> 3f;
        this.visible = setting::isVisible;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (drag) {
            float value = MathHelper.clamp(
                    MathUtils.round((mouseX - x - 5f) / (width - 12f) * (setting.getMax() - setting.getMin()) + setting.getMin(), setting.getIncrement()),
                    setting.getMin(),
                    setting.getMax()
            );
            setting.setValue(value);
        }

        Render2D.drawFont(context.getMatrices(), Fonts.REGULAR.getFont(7.5f), I18n.translate(setting.getName()), x + 4f, y + 3f, Color.WHITE);
        Render2D.drawRoundedRect(context.getMatrices(), x + 4f, y + 13f,
                width - 8f, 4f, 0.5f, new Color(23, 23, 23, 100));
        Render2D.drawRoundedRect(context.getMatrices(), x + 4f, y + 13f,
                animation.animate((width - 8f) * ((setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin())), 100),
                4f, 0.5f, ColorUtils.getGlobalColor());
        Render2D.drawRoundedRect(context.getMatrices(),
                x + 1f + animation.animate((width - 8f) * ((setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin())), 100),
                y + 12f, 6f, 6f, 3f, Color.WHITE);
        Render2D.drawFont(context.getMatrices(), Fonts.REGULAR.getFont(6f), setting.getValue() + "",
                x + width - Fonts.REGULAR.getWidth(setting.getValue() + "", 6.5f) - 4.5f, y + 5f, Color.WHITE);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtils.isHovered(x + 4f, y + 12f, width - 8f, 6f, (float) mouseX, (float) mouseY) && button == 0) drag = !drag;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) drag = false;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void keyReleased(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char chr, int modifiers) {

    }
}