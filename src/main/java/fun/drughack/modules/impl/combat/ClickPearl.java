package fun.drughack.modules.impl.combat;

import fun.drughack.api.events.impl.EventPlayerTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.impl.EnumSetting;
import fun.drughack.utils.network.NetworkUtils;
import fun.drughack.utils.world.InventoryUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class ClickPearl extends Module {

    private final EnumSetting<InventoryUtils.Swing> swing = new EnumSetting<>("settings.swing", InventoryUtils.Swing.MainHand);

    public ClickPearl() {
        super("ClickPearl", Category.Combat);
    }

    @EventHandler
    public void onPlayerTick(EventPlayerTick e) {
        if (fullNullCheck()) return;

        int slot = InventoryUtils.findHotbar(Items.ENDER_PEARL);
        int previousSlot = mc.player.getInventory().selectedSlot;

        if (slot == -1 || mc.player.getItemCooldownManager().isCoolingDown(mc.player.getInventory().getStack(slot))) {
            setToggled(false);
            return;
        }

        InventoryUtils.switchSlot(InventoryUtils.Switch.Silent, slot, previousSlot);
        NetworkUtils.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
        InventoryUtils.swing(swing.getValue());
        InventoryUtils.switchBack(InventoryUtils.Switch.Silent, slot, previousSlot);
        setToggled(false);
    }
}