package ru.drughack.modules.impl.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventAttackEntity;
import ru.drughack.api.event.impl.EventPacketSend;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;

public class NoFriendDamage extends Module {

    public NoFriendDamage() {
        super("NoFriendDamage", "cancel the damage for friend", Category.Combat);
    }

    @EventHandler
    public void onAttackEntity(EventAttackEntity e) {
        if (fullNullCheck()) return;
        for (String name : DrugHack.getInstance().getFriendManager().getFriends()) if (e.getTarget().getDisplayName().getString().contains(name)) e.cancel();
    }
}