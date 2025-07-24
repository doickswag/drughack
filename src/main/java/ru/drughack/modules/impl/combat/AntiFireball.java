package ru.drughack.modules.impl.combat;

import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.api.Nameable;
import ru.drughack.utils.rotations.RotationChanger;
import ru.drughack.utils.rotations.RotationUtils;
import ru.drughack.utils.world.InventoryUtils;
import ru.drughack.utils.world.MultipointUtils;
import ru.drughack.utils.world.WorldUtils;

public class AntiFireball extends Module {

    public Setting<Float> range = new Setting<>("Range", 3f, 0f, 6f);
    public Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.Normal);
    public Setting<InventoryUtils.Swing> swing = new Setting<>("Swing", InventoryUtils.Swing.Mainhand);

    public AntiFireball() {
        super("AntiFireball", "attack fireball auto", Category.Combat);
    }

    private Vector2f rotations = new Vector2f(0, 0);
    private final RotationChanger changer = new RotationChanger(500, () -> new Float[]{rotations.getX(), rotations.getY()});

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate e) {
        if (fullNullCheck()) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == null) continue;
            if (!(entity instanceof FireballEntity)) continue;
            Vec3d multipoint = MultipointUtils.getClosestPoint(entity, 10, 10, range.getValue(), false);
            if (multipoint == null) continue;
            if (WorldUtils.canSee(entity) && multipoint.squaredDistanceTo(mc.player.getEyePos()) < MathHelper.square(range.getValue())) {
                rotations = new Vector2f(RotationUtils.getRotations(entity)[0], RotationUtils.getRotations(entity)[1]);
                if (rotate.getValue() == Rotate.Normal) DrugHack.getInstance().getRotationManager().addRotation(changer);
                if (rotate.getValue() == Rotate.Packet) DrugHack.getInstance().getRotationManager().addPacketRotation(RotationUtils.getRotations(entity));
                mc.interactionManager.attackEntity(mc.player, entity);
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
        }
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