package ru.drughack.modules.impl.movement;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.*;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.world.InventoryUtils;
import ru.drughack.utils.world.NetworkUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NoSlow extends Module {

    public Setting<Mode> mode = new Setting<>("Mode", Mode.Plain);
    public Setting<InvMode> invMode = new Setting<>("Inv Mode", InvMode.Plain);
    public Setting<Integer> ticksPolar = new Setting<>("Ticks", 5, 1, 10, v -> invMode.getValue() == InvMode.Polar);
    public Setting<Boolean> soulSand = new Setting<>("SoulSand", false);
    public Setting<Boolean> slimeBlocks = new Setting<>("SlimeBlocks", false);
    public Setting<Boolean> honeyBlocks = new Setting<>("HoneyBlocks", false);
    public Setting<Boolean> airStrict = new Setting<>("AirStrict", false);

    private boolean sneaking;
    private int ticks = 0;
    private final Int2ObjectOpenHashMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();
    private final List<Packet<?>> packets = new CopyOnWriteArrayList<>();

    public NoSlow() {
        super("NoSlow", "Doesn't slow you down when using something", Category.Movement);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (fullNullCheck()) return;

        if (invMode.getValue() != InvMode.None
                && mc.currentScreen != null
                && !(mc.currentScreen instanceof ChatScreen
                || mc.currentScreen instanceof BookEditScreen
                || mc.currentScreen instanceof SignEditScreen
                || mc.currentScreen instanceof JigsawBlockScreen
                || mc.currentScreen instanceof StructureBlockScreen
                || mc.currentScreen instanceof AnvilScreen
                || invMode.getValue() == InvMode.Polar
                && mc.currentScreen instanceof GenericContainerScreen)
        ) {
            for (KeyBinding binding : new KeyBinding[]{
                    mc.options.forwardKey,
                    mc.options.backKey, mc.options.rightKey,
                    mc.options.leftKey, mc.options.sprintKey,
                    mc.options.jumpKey
            })
                binding.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(binding.getBoundKeyTranslationKey()).getCode()));
        }

        if (airStrict.getValue() && sneaking && !mc.player.isUsingItem()) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            sneaking = false;
        }
    }

    @EventHandler
    public void onKeyboardInput(EventKeyboardTick e) {
        if (invMode.getValue() == InvMode.Polar && ticks > 0) {
            e.setMovementForward(0);
            ticks--;
            e.cancel();
        }

        if (mode.getValue() == Mode.GRIMV3) {
            if (mc.player.isUsingItem()) {
                if (mc.player.getItemUseTime() <= 5) mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(mc.player.currentScreenHandler.syncId, 0, 1, 0, SlotActionType.PICKUP, ItemStack.EMPTY, int2ObjectMap));
                else {
                    mc.player.input.movementForward *= 5f;
                    mc.player.input.movementSideways *= 5f;
                }
            }
        }
    }

    @EventHandler
    public void onUpdateMovement(EventMoveUpdate event) {
        if (fullNullCheck()) return;
        if (!(mode.getValue() == Mode.GRIMOLD)) return;
        if (!mc.player.isUsingItem() || mc.player.isRiding() || mc.player.isGliding()) return;

        if (mc.player.getActiveHand() == Hand.OFF_HAND) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 8 + 1));
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot % 7 + 2));
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        } else NetworkUtils.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (fullNullCheck()) return;

        if (invMode.getValue() == InvMode.Polar && !(mc.currentScreen instanceof GenericContainerScreen)) {
            if (event.getPacket() instanceof ClickSlotC2SPacket && mc.currentScreen != null) {
                packets.add(event.getPacket());
                event.cancel();
            }

            if (event.getPacket() instanceof CloseHandledScreenC2SPacket && !packets.isEmpty()) {
                ticks = ticksPolar.getValue();
                resumePackets();
            }
        }

        if (mode.getValue() == Mode.NCP) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket.Full
                    || event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround
                    || event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround
                    || event.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly
            ) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }

            if (event.getPacket() instanceof ClickSlotC2SPacket) {
                if (mc.player.isUsingItem()) mc.player.stopUsingItem();
                if (mc.player.isSprinting()) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                if (mc.player.isSneaking()) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
        }
    }

    @EventHandler
    public void onChangeHand(EventChangeHand event) {
        if (fullNullCheck()) return;
        if (mode.getValue() == Mode.None) return;
        if (airStrict.getValue() && !sneaking && (!mc.player.isRiding() && !mc.player.isSneaking() && (mc.player.isUsingItem() && (!(mode.getValue() == Mode.GRIMOLD))))) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            sneaking = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        DrugHack.getInstance().getWorldManager().setTimerMultiplier(1.0f);
        if (mc.player == null) return;
        if (airStrict.getValue() && sneaking) mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        sneaking = false;
    }

    private void resumePackets() {
        if (!packets.isEmpty()) {
            for (Packet<?> packet : packets) NetworkUtils.sendWithoutEventPacket(packet);
            packets.clear();
        }
    }

    public boolean shouldSlow() {
        return mode.getValue() == Mode.None
                || mode.getValue() == Mode.GRIMV3
                || mode.getValue() == Mode.GRIMOLD
                && mc.player.getActiveHand() == Hand.MAIN_HAND
                && (mc.player.getOffHandStack().getComponents().contains(DataComponentTypes.FOOD)
                || mc.player.getOffHandStack().getItem() == Items.SHIELD);
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().getName() + "/" + invMode.getValue().getName();
    }

    @AllArgsConstructor
    public enum Mode implements Nameable {
        Plain("Plain"),
        NCP("NCP"),
        GRIMOLD("GrimOld"),
        GRIMV3("GrimV3"),
        None("None");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum InvMode implements Nameable {
        Plain("Plain"),
        Polar("Polar"),
        None("None");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}