package fun.drughack.screen.clickgui.components.impl;

import fun.drughack.modules.settings.impl.EnumSetting;
import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.modules.settings.api.Nameable;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.fonts.Fonts;
import fun.drughack.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;

import java.awt.*;
import java.util.*;

public class EnumComponent extends Component {

    private final EnumSetting<?> setting;
    private final Animation openAnimation = new Animation(300, 1f, false, Easing.BOTH_SINE);
    private final Map<Enum<?>, Animation> pickAnimations = new HashMap<>();
    private boolean open;

    public EnumComponent(EnumSetting<?> setting) {
        super(setting.getName());
        this.setting = setting;
        for (Enum<?> enums : setting.getValue().getClass().getEnumConstants()) pickAnimations.put(enums, new Animation(300, 1f, false, Easing.BOTH_SINE));
        this.addHeight = () -> openAnimation.getValue() > 0 ? ((setting.getValue().getClass().getEnumConstants().length * 14f)) * openAnimation.getValue() : 0;
        this.visible = setting::isVisible;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        openAnimation.update(open);

        Render2D.drawFont(context.getMatrices(),
                Fonts.REGULAR.getFont(7.5f),
                I18n.translate(setting.getName()) + ": " + I18n.translate(setting.currentEnumName()),
                x + 4f,
                y + 3f,
                Color.WHITE
        );

        if (openAnimation.getValue() > 0) {
            float yOffset = height;
            for (Enum<?> enums : setting.getValue().getClass().getEnumConstants()) {
                Render2D.startScissor(context, x, y + yOffset, width, 14f);
                Animation anim = pickAnimations.get(enums);
                anim.update(enums == setting.getValue());
                Render2D.drawFont(context.getMatrices(), Fonts.ICONS.getFont(10f), "D", x + width - 14f, y + yOffset + 2f, new Color(255, 255, 255, (int) (255 * anim.getValue())));
                Render2D.drawFont(context.getMatrices(),
                        Fonts.REGULAR.getFont(7.5f),
                        I18n.translate(((Nameable) enums).getName()),
                        x + 6f,
                        y + yOffset + 2f,
                        Color.WHITE
                );
                yOffset += 14f;
                Render2D.stopScissor(context);
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtils.isHovered(x, y, width, height, (float) mouseX, (float) mouseY)) {
            if (button == 0) setting.increaseEnum();
            else if (button == 1) open = !open;
        }

        if (open && button == 0) {
            float yOffset = height;
            for (Enum<?> enums : setting.getValue().getClass().getEnumConstants()) {
                if (MathUtils.isHovered(x, y + yOffset, width, 14f, (float) mouseX, (float) mouseY)) {
                    setting.setEnumValue(((Nameable) enums).getName());
                    break;
                }

                yOffset += 14f;
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {

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