package ru.drughack.modules.impl.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventKey;
import ru.drughack.api.event.impl.EventMouse;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.impl.Bind;
import ru.drughack.utils.world.InventoryUtils;

//later...
public class ElytraSwap extends Module {

    public Setting<Bind> elytraBind = new Setting<>("Elytra Bind", new Bind(GLFW.GLFW_KEY_Z, false, false));
    public Setting<Bind> fireworkBind = new Setting<>("Firework Bind", new Bind(GLFW.GLFW_KEY_X, false, false));
    private final Setting<InventoryUtils.Switch> autoSwitch = new Setting<>("Switch", InventoryUtils.Switch.Silent);
    public Setting<InventoryUtils.Swing> swing = new Setting<>("Swing", InventoryUtils.Swing.Mainhand);

    public ElytraSwap() {
        super("ElytraSwap", "swap your elytra to chestplate slot", Category.Player);
    }

    @EventHandler
    public void onKey(EventKey e) {
        if (fullNullCheck() || mc.currentScreen != null) return;
        if (e.getAction() == 1 && e.getKey() == fireworkBind.getValue().getKey()) throwFirework(false);
    }

    @EventHandler
    public void onMouse(EventMouse e) {
        if (fullNullCheck() || mc.currentScreen != null) return;
        if (e.getAction() == 1 && e.getButton() == fireworkBind.getValue().getKey()) throwFirework(true);
    }

    private void throwFirework(boolean mouse) {
        if (mouse && !fireworkBind.getValue().isMouse()) return;

        int slot = InventoryUtils.find(Items.FIREWORK_ROCKET, 0, autoSwitch.getValue() == InventoryUtils.Switch.AltSwap
                || autoSwitch.getValue() == InventoryUtils.Switch.AltPickup ? 35 : 8
        );

        if (slot == -1) {
            DrugHack.getInstance().getChatManager().message("slot with firework is empty!");
            return;
        }

        int previousSlot = mc.player.getInventory().selectedSlot;
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
    }
}