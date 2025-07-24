package ru.drughack.modules.impl.hud;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.modules.api.HudModule;

import java.awt.*;

public class Armor extends HudModule {

    public Armor() {
        super("Armor", 66, 17);
    }

    @Override
    public void onRender2D(EventRender2D e) {
        super.onRender2D(e);
        MatrixStack matrices = e.getContext().getMatrices();
        int offset = 0;

        for (ItemStack stack : mc.player.getArmorItems()) {
            if (stack.isEmpty()) continue;
            float itemX = getX() + (17f * offset);
            e.getContext().drawItem(stack, (int) itemX, (int) getY());
            e.getContext().drawStackOverlay(mc.textRenderer, stack, (int) itemX, (int) getY());
            int damage = stack.getDamage();
            int maxDamage = stack.getMaxDamage();
            matrices.push();
            matrices.scale(0.625f, 0.625f, 0.625f);
            float textX = (itemX + 2) * 1.6f;
            float textY = (getY() - 5) * 1.6f;
            DrugHack.getInstance().getFontManager().drawTextWithShadow(e.getContext(), (((maxDamage - damage) * 100) / maxDamage) + "%", (int) textX, (int) textY, new Color(1.0f - ((maxDamage - damage) / (float) maxDamage), (maxDamage - damage) / (float) maxDamage, 0));
            matrices.pop();
            offset++;
        }

        setBounds(getX(), getY(), 66, 17);
    }
}