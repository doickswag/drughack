package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.math.TimerUtils;
import ru.drughack.utils.rotations.RotationChanger;
import ru.drughack.utils.rotations.RotationUtils;
import ru.drughack.utils.world.InventoryUtils;

public class AutoPotion extends Module {

    public Setting<Boolean> ground = new Setting<>("Only Ground", true);
    public Setting<Boolean> instantHealth = new Setting<>("Instant Health", true);
    public Setting<Float> health = new Setting<>("Health", 15f, 0f, 36f, v -> instantHealth.getValue());
    public Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.Normal);
    public Setting<InventoryUtils.Switch> autoSwitch = new Setting<>("Switch", InventoryUtils.Switch.Silent);
    public Setting<InventoryUtils.Swing> swing = new Setting<>("Swing", InventoryUtils.Swing.Mainhand);

    private final TimerUtils timer = new TimerUtils();

    private Vector2f rotations = new Vector2f(0, 0);
    private final RotationChanger changer = new RotationChanger(500, () -> new Float[]{rotations.getX(), rotations.getY()});

    public AutoPotion() {
        super("AutoPotion", "uses potion auto", Category.Combat);
    }

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate e) {
        if (!mc.player.hasStatusEffect(StatusEffects.STRENGTH)) throwPotion(getPotionSlot(Potions.STRENGTH));
        if (!mc.player.hasStatusEffect(StatusEffects.SPEED)) throwPotion(getPotionSlot(Potions.SPEED));
        if (!mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) throwPotion(getPotionSlot(Potions.FIRE_RESISTANCE));
        if (instantHealth.getValue() && mc.player.getHealth() <= health.getValue()) throwPotion(getPotionSlot(Potions.INSTANT_HEALTH));
    }

    private void throwPotion(int slot) {
        if (mc.player.age > 80 && slot != -1 && timer.hasTimeElapsed(250)) {
            int previousSlot = mc.player.getInventory().selectedSlot;
            if (ground.getValue() && !mc.player.isOnGround()) return;
            InventoryUtils.switchSlot(autoSwitch.getValue().name(), slot, previousSlot);
            rotations = new Vector2f(RotationUtils.getRotations(Direction.DOWN)[0], RotationUtils.getRotations(Direction.DOWN)[1]);
            if (rotate.getValue() == Rotate.Normal) {
                if (!DrugHack.getInstance().getRotationManager().containsRotation(changer)) DrugHack.getInstance().getRotationManager().addRotation(changer);
                else if (DrugHack.getInstance().getRotationManager().containsRotation(changer)) DrugHack.getInstance().getRotationManager().removeRotation(changer);
            }
            if (rotate.getValue() == Rotate.Packet) DrugHack.getInstance().getRotationManager().addPacketRotation(RotationUtils.getRotations(Direction.DOWN));
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, RotationUtils.getRotations(Direction.DOWN)[0], RotationUtils.getRotations(Direction.DOWN)[1]));
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
            timer.reset();
        }
    }

    private boolean isStackPotion(ItemStack stack, Potions potion) {
        if (stack == null) return false;

        if (stack.getItem() instanceof SplashPotionItem) {
            PotionContentsComponent potionContentsComponent = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            RegistryEntry<StatusEffect> id = null;

            switch (potion) {
                case STRENGTH -> id = StatusEffects.STRENGTH;
                case SPEED -> id = StatusEffects.SPEED;
                case FIRE_RESISTANCE -> id = StatusEffects.FIRE_RESISTANCE;
                case INSTANT_HEALTH -> id = StatusEffects.INSTANT_HEALTH;
            }

            for (StatusEffectInstance effect : potionContentsComponent.getEffects()) if (effect.getEffectType() == id) return true;
        }
        return false;
    }

    private int getPotionSlot(Potions potion) {
        for (int i = 0; i < 9; ++i) if (isStackPotion(mc.player.getInventory().getStack(i), potion)) return i;
        return -1;
    }

    private enum Potions {
        STRENGTH,
        SPEED,
        FIRE_RESISTANCE,
        INSTANT_HEALTH
    }

    @Override
    public void onDisable() {
        super.onDisable();
        DrugHack.getInstance().getRotationManager().removeRotation(changer);
    }

    @AllArgsConstructor
    private enum Rotate implements Nameable {
        Normal("Normal"),
        Packet("Packet"),
        None("None");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
}