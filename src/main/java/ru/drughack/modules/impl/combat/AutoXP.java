package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.world.InventoryUtils;
import ru.drughack.utils.world.NetworkUtils;

public class AutoXP extends Module {

    public Setting<InventoryUtils.Switch> autoSwitch = new Setting("Switch", InventoryUtils.Switch.Silent);
    public Setting<Integer> delay = new Setting<>("Delay", 1, 0, 20);
    public Setting<Integer> freuqency = new Setting<>("Freuqency", 1, 1, 15);
    public Setting<AntiWaste> antiWaste = new Setting<>("AntiWaste", AntiWaste.Avoid);

    private int ticks = 0;

    public AutoXP() {
        super("AutoXP", "xp but auto", Category.Combat);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate e) {
        if (autoSwitch.getValue() == InventoryUtils.Switch.None && !(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        if (ticks < delay.getValue()) {
            ticks++;
            return;
        }

        if (!needsExperience() && !(antiWaste.getValue() == AntiWaste.None)) return;
        int slot = InventoryUtils.find(Items.EXPERIENCE_BOTTLE, 0, autoSwitch.getValue() == InventoryUtils.Switch.AltSwap || autoSwitch.getValue() == InventoryUtils.Switch.AltPickup ? 35 : 8);
        int previousSlot = mc.player.getInventory().selectedSlot;
        if (slot == -1) return;
        InventoryUtils.switchSlot(autoSwitch.getValue().name(), slot, previousSlot);
        for (int i = 0; i < freuqency.getValue(); i++) NetworkUtils.sendSequencedPacket(sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, mc.player.getYaw(), mc.player.getPitch()));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        InventoryUtils.switchBack(autoSwitch.getValue().name(), slot, previousSlot);
        ticks = 0;
    }

    private boolean needsExperience() {
        for (ItemStack stack : mc.player.getArmorItems()) {
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem && (Math.round(((stack.getMaxDamage() - stack.getDamage()) * 100.0f) / stack.getMaxDamage()) < 100.0f)) {
                return true;
            }
        }

        return false;
    }

    @AllArgsConstructor
    private enum AntiWaste implements Nameable {
        None("None"),
        Avoid("Avoid");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}