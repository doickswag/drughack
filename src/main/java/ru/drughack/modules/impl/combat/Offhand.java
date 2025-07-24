package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.MathHelper;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPlayerPop;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.impl.player.SpeedMine;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.world.InventoryUtils;

public class Offhand extends Module {

    public Setting<Mode> item = new Setting<>("Item", Mode.Totem);
    public Setting<Integer> health = new Setting<>("Health", 16, 0, 36, v -> item.getValue() == Mode.Crystal || item.getValue() == Mode.Gapple || item.getValue() == Mode.EnchantmentGapple);
    public Setting<Boolean> useGapple = new Setting<>("UseGapple", true);
    public Setting<Boolean> lethal = new Setting<>("Lethal", false, v -> useGapple.getValue());
    public Setting<Boolean> tickAbort = new Setting<>("TickAbort", true);
    public Setting<Boolean> smartMine = new Setting<>("SmartMine", false, v -> item.getValue() == Mode.Crystal);

    private int ticks = 0;

    public Offhand() {
        super("Offhand", "aka autototem", Category.Combat);
    }

    @EventHandler
    public void onPlayerPop(EventPlayerPop event) {
        if (event.getPlayer() == mc.player && !DrugHack.getInstance().getModuleManager().getSuicide().isToggled()) ticks = 0;
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (ticks > 0 && tickAbort.getValue()) {
            ticks--;
            return;
        }

        if (!(mc.currentScreen instanceof InventoryScreen) && mc.currentScreen instanceof HandledScreen<?>) return;

        Item item = getItem();
        if (item == null) return;

        int slot;

        if (item == Items.TOTEM_OF_UNDYING && DrugHack.getInstance().getModuleManager().getSuicide().isToggled()) {
            if (mc.player.getOffHandStack().isEmpty()) return;
            slot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
        } else {
            if (mc.player.getOffHandStack().getItem() == item) return;
            slot = InventoryUtils.findInventory(item);
            if (slot == -1) slot = InventoryUtils.find(item);

            if (slot == -1) {
                if (item == Items.TOTEM_OF_UNDYING) slot = InventoryUtils.findEmptySlot(InventoryUtils.HOTBAR_START, InventoryUtils.INVENTORY_END);
                else return;
            }
        }

        if (slot == -1) return;

        InventoryUtils.swap("Pickup", slot, 45);
        ticks = 2 + DrugHack.getInstance().getServerManager().getPingDelay();
    }

    private Item getItem() {
        if (useGapple.getValue() && mc.options.useKey.isPressed() && (lethal.getValue() || !needsTotem()) && (mc.player.getMainHandStack().getItem() instanceof SwordItem || mc.player.getMainHandStack().getItem() instanceof AxeItem) && !hasItem(Items.GOLDEN_APPLE) && hasItem(Items.ENCHANTED_GOLDEN_APPLE))
            return Items.ENCHANTED_GOLDEN_APPLE;

        if (useGapple.getValue() && mc.options.useKey.isPressed() && (lethal.getValue() || !needsTotem()) && (mc.player.getMainHandStack().getItem() instanceof SwordItem || mc.player.getMainHandStack().getItem() instanceof AxeItem) && !hasItem(Items.ENCHANTED_GOLDEN_APPLE) && hasItem(Items.GOLDEN_APPLE))
            return Items.GOLDEN_APPLE;

        if (hasItem(Items.TOTEM_OF_UNDYING)) {
            if (needsTotem()) return Items.TOTEM_OF_UNDYING;

            if (item.getValue() == Mode.Crystal && smartMine.getValue()) {
                SpeedMine module = DrugHack.getInstance().getModuleManager().getSpeedMine();
                if ((module.getPrimary() == null || !module.getPrimary().isMining()) && (module.getSecondary() == null || !module.getSecondary().isMining())) return Items.TOTEM_OF_UNDYING;
            }
        }

        switch (item.getValue()) {
            case Crystal -> {
                if (!hasItem(Items.END_CRYSTAL)) return Items.TOTEM_OF_UNDYING;
                return Items.END_CRYSTAL;
            }
            case Gapple -> {
                if (!hasItem(Items.GOLDEN_APPLE)) return Items.TOTEM_OF_UNDYING;
                return Items.GOLDEN_APPLE;
            }
            case EnchantmentGapple -> {
                if (!hasItem(Items.ENCHANTED_GOLDEN_APPLE)) return Items.TOTEM_OF_UNDYING;
                return Items.ENCHANTED_GOLDEN_APPLE;
            }
            default -> {
                return Items.TOTEM_OF_UNDYING;
            }
        }
    }

    private boolean needsTotem() {
        if (DrugHack.getInstance().getModuleManager().getSuicide().isToggled()) return false;

        return mc.player.getHealth() + mc.player.getAbsorptionAmount() <= health.getValue().floatValue();
    }

    private boolean hasItem(Item item) {
        return InventoryUtils.find(item) != -1 || mc.player.getOffHandStack().getItem() == item;
    }

    @AllArgsConstructor
    private enum Mode implements Nameable {
        Totem("Totem"),
        Crystal("Crystal"),
        EnchantmentGapple("EnchantmentGapple"),
        Gapple("Gapple");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}