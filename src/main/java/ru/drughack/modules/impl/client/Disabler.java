package ru.drughack.modules.impl.client;

import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ru.drughack.api.event.impl.EventSync;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.world.InventoryUtils;

public class Disabler extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.GRIMV2);

    public Disabler() {
        super("Disabler", "old disabler for grim v2/v3", Category.Client);
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.GRIMV3) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                    mc.player.getX(),
                    mc.player.getY(),
                    mc.player.getZ(),
                    Float.MAX_VALUE,
                    mc.player.getPitch(),
                    mc.player.isOnGround(),
                    mc.player.horizontalCollision
            ));

            e.cancel();
        }
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        if (mode.getValue() == Mode.GRIMV2) {
            int tridentSlot = InventoryUtils.findHotbar(Items.TRIDENT);
            int oldSlot = mc.player.getInventory().selectedSlot;
            if (tridentSlot == -1 || mc.player.isUsingItem()) return;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(tridentSlot));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
        }
    }

    @AllArgsConstructor
    public enum Mode implements Nameable {
        GRIMV2("Grim V2"),
        GRIMV3("Grim V3");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}