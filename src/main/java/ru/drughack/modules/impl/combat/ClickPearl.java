package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.world.InventoryUtils;

public class ClickPearl extends Module {

    private final Setting<InventoryUtils.Switch> autoSwitch = new Setting<>("Switch", InventoryUtils.Switch.Silent);
    private final Setting<InventoryUtils.Swing> swing = new Setting<>("Swing", InventoryUtils.Swing.Mainhand);

    public ClickPearl() {
        super("ClickPearl", "lol", Category.Combat);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate e) {
        int slot = InventoryUtils.find(Items.ENDER_PEARL, 0, autoSwitch.getValue() == InventoryUtils.Switch.AltSwap || autoSwitch.getValue() == InventoryUtils.Switch.AltPickup ? 35 : 8);
        int previousSlot = mc.player.getInventory().selectedSlot;

        if (slot == -1) {
            toggle();
            return;
        }

        InventoryUtils.switchSlot(autoSwitch.getValue().name(), slot, previousSlot);
        mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
        switch (swing.getValue()) {
            case InventoryUtils.Swing.Mainhand -> mc.player.swingHand(Hand.MAIN_HAND);
            case InventoryUtils.Swing.Offhand -> mc.player.swingHand(Hand.OFF_HAND);
            case InventoryUtils.Swing.Both -> {
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.swingHand(Hand.OFF_HAND);
            }
            case InventoryUtils.Swing.Packet -> mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        InventoryUtils.switchBack(autoSwitch.getValue().name(), slot, previousSlot);
        toggle();
    }
}