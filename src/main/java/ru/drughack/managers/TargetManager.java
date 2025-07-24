package ru.drughack.managers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventClientConnect;
import ru.drughack.api.event.impl.EventPlayerDeath;
import ru.drughack.api.event.impl.EventTargetDeath;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.utils.interfaces.Wrapper;

import java.util.ArrayList;

public class TargetManager implements Wrapper {
    private final ArrayList<Target> targets = new ArrayList<>();

    public TargetManager() {
        DrugHack.getInstance().getEventHandler().subscribe(this);
    }

    @EventHandler
    public void onClientConnect(EventClientConnect event) {
        targets.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        PlayerEntity caTarget = DrugHack.getInstance().getModuleManager().getAutoCrystal().getTarget();
        Entity kaTarget = DrugHack.getInstance().getModuleManager().getAura().getTarget();

        synchronized (targets) {
            targets.removeIf(t -> System.currentTimeMillis() - t.time > 15000);
            if (caTarget != null) targets.add(new Target(caTarget));
            if (kaTarget instanceof PlayerEntity) targets.add(new Target((PlayerEntity) kaTarget));
        }
    }

    @EventHandler
    public void onPlayerDeath(EventPlayerDeath event) {
        if (mc.player == null || mc.world == null || !isTarget(event.getPlayer())) return;

        synchronized (targets) {
            DrugHack.getInstance().getEventHandler().post(new EventTargetDeath(event.getPlayer()));
            targets.remove(getTarget(event.getPlayer()));
        }
    }

    private Target getTarget(PlayerEntity player) {
        for (Target target : targets) if (target.player == player) return target;
        return null;
    }

    public boolean isTarget(PlayerEntity player) {
        for (Target target : targets) {
            if (target.player == player) return true;
        }
        return false;
    }

    private static class Target {
        private final PlayerEntity player;
        private final long time;

        public Target(PlayerEntity player) {
            this.player = player;
            this.time = System.currentTimeMillis();
        }
    }
}