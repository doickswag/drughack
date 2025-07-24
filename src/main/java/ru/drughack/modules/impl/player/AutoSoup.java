package ru.drughack.modules.impl.player;

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

public class AutoSoup extends Module {

    private final Setting<InventoryUtils.Switch> autoSwitch = new Setting<>("Switch", InventoryUtils.Switch.Silent);
    private final Setting<Float> health = new Setting<>("Health", 15f, 0f, 36f);
    private final Setting<InventoryUtils.Swing> swing = new Setting<>("Swing", InventoryUtils.Swing.Mainhand);

    public AutoSoup() {
        super("AutoSoup", "eat the all soup on your inventory", Category.Player);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate e) {
        int bowlSlot = InventoryUtils.findHotbar(Items.BOWL);
        int mushroomStewSlot = InventoryUtils.find(Items.MUSHROOM_STEW, 0, autoSwitch.getValue() == InventoryUtils.Switch.AltSwap || autoSwitch.getValue() == InventoryUtils.Switch.AltPickup ? 35 : 8);
        int previousSlot = mc.player.getInventory().selectedSlot;

        if (mushroomStewSlot != -1 && mc.player.getHealth() <= health.getValue()) {
            InventoryUtils.switchSlot(autoSwitch.getValue().name(), mushroomStewSlot, previousSlot);
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
            if (mc.player.getInventory().selectedSlot == bowlSlot && mc.player.dropSelectedItem(true)) {
                switch (swing.getValue()) {
                    case InventoryUtils.Swing.Mainhand -> mc.player.swingHand(Hand.MAIN_HAND);
                    case InventoryUtils.Swing.Offhand -> mc.player.swingHand(Hand.OFF_HAND);
                    case InventoryUtils.Swing.Both -> {
                        mc.player.swingHand(Hand.MAIN_HAND);
                        mc.player.swingHand(Hand.OFF_HAND);
                    }
                    case InventoryUtils.Swing.Packet -> mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }
            InventoryUtils.switchBack(autoSwitch.getValue().name(), mushroomStewSlot, previousSlot);
        }
    }
}