package ru.drughack.modules.impl.player;

import lombok.AllArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.world.InventoryUtils;

public class Replenish extends Module {

    public Setting<Switch> switchMode = new Setting<>("Switch", Switch.Swap);
    public Setting<Integer> threshold = new Setting<>("Threshold", 12, 1, 64);
    public Setting<Integer> minimumCount = new Setting<>("MinimumCount", 48, 1, 64);

    private int ticks;

    public Replenish() {
        super("Replenish", "refill your hotbar", Category.Player);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (mc.player == null || mc.world == null) return;
        if (InventoryUtils.inInventoryScreen()) return;

        if (ticks <= 0) {
            for (int i = 0; i <= 8; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!stack.isStackable()) continue;
                if (stack.getCount() > (int) ((threshold.getValue().floatValue() / 64.0f) * stack.getMaxCount())) continue;
                if (stack.isEmpty()) continue;
                int slot = InventoryUtils.findInventory(stack.getItem(), (int) ((minimumCount.getValue().intValue() / 64.0f) * stack.getMaxCount()));
                if (slot == -1) continue;
                if (switchMode.getValue() == Switch.Quick) mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, InventoryUtils.indexToSlot(slot), 0, SlotActionType.QUICK_MOVE, mc.player);
                else InventoryUtils.swap(switchMode.getValue().name(), slot, i);
                ticks = 2 + DrugHack.getInstance().getServerManager().getPingDelay();
            }
        }

        ticks--;
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(threshold.getValue().intValue());
    }

    @AllArgsConstructor
    private enum Switch implements Nameable {
        Pickup("Pickup"),
        Swap("Swap"),
        Quick("Quick");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}