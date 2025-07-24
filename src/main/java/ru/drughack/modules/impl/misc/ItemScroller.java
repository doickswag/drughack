package ru.drughack.modules.impl.misc;

import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.api.Category;
import ru.drughack.api.event.impl.EventClickSlot;
import ru.drughack.modules.settings.Setting;

public class ItemScroller extends Module {

    public Setting<Integer> delay = new Setting<>("Delay", 0, 0, 500);
    private boolean pauseListening = false;

    public ItemScroller() {
        super("ItemScroller", "吸雞雞", Category.Misc);
    }

    @EventHandler
    public void onClick(EventClickSlot e) {
        if ((isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)
                || isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT))
                && (isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)
                || isKeyPressed(GLFW.GLFW_KEY_RIGHT_CONTROL))
                && e.getSlotActionType() == SlotActionType.THROW
                && !pauseListening) {

            if (mc.player == null || mc.interactionManager == null) return;
            Item copy = mc.player.currentScreenHandler.slots.get(e.getSlot()).getStack().getItem();
            pauseListening = true;
            for (int i2 = 0; i2 < mc.player.currentScreenHandler.slots.size(); ++i2) {
                if (mc.player.currentScreenHandler.slots.get(i2).getStack().getItem() == copy) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i2, 1, SlotActionType.THROW, mc.player);
                }
            }
            pauseListening = false;
        }
    }
}