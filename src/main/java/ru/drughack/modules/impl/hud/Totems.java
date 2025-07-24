package ru.drughack.modules.impl.hud;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.GameMode;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.modules.api.HudModule;

public class Totems extends HudModule {

    public Totems() {
        super("Totems", 18, 18);
    }

    @Override
    public void onRender2D(EventRender2D e) {
        super.onRender2D(e);

        int totems = mc.player.getInventory().count(Items.TOTEM_OF_UNDYING);
        if (totems > 0) {
            ItemStack stack = new ItemStack(Items.TOTEM_OF_UNDYING);
            float waterOffset = (mc.player.isSubmergedInWater() && mc.interactionManager.getCurrentGameMode() != GameMode.CREATIVE) ? 10f : 0f;
            float itemY = getY() + waterOffset;
            e.getContext().drawItem(stack, (int) getX(), (int) itemY);
            e.getContext().drawStackOverlay(mc.textRenderer, stack, (int) getX(), (int) itemY, String.valueOf(totems));
        }

        setBounds(getX(), getY(), 18, 18);
    }
}