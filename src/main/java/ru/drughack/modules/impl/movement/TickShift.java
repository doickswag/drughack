package ru.drughack.modules.impl.movement;

import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.world.EntityUtils;

public class TickShift extends Module {

    public Setting<Integer> maxTicks = new Setting<>("MaxTicks", 1, 1, 40);
    public Setting<Integer> delay = new Setting<>("Delay", 4, 1, 10);
    public Setting<Float> speed = new Setting<>("Speed", 2.0f, 1.0f, 10.0f);

    public TickShift() {
        super("TickShift", "Make your femboys feet for ticks faster", Category.Movement);
    }

    int ticks = 0;
    int wait = 0;

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;

        if (DrugHack.getInstance().getServerManager().getFallDistance() >= 5.0f) return;

        if ((mc.player.sidewaysSpeed == 0.0f && mc.player.forwardSpeed == 0.0f && DrugHack.getInstance().getServerManager().getFallDistance() == 0.0f) || EntityUtils.getSpeed(mc.player, EntityUtils.SpeedUnit.KILOMETERS) <= 5) {
            DrugHack.getInstance().getWorldManager().setTimerMultiplier(1.0f);
            if (wait >= delay.getValue()) {
                if (ticks < maxTicks.getValue()) ticks++;
                wait = 0;
            }
            wait++;
        } else {
            if (ticks > 0) {
                if (!DrugHack.getInstance().getModuleManager().getSpeed().isToggled() && !mc.options.jumpKey.isPressed()) DrugHack.getInstance().getWorldManager().setTimerMultiplier(speed.getValue().floatValue());
                ticks--;
            } else {
                reset$();
            }
        }
    }

    public void reset$() {
        DrugHack.getInstance().getWorldManager().setTimerMultiplier(1.0f);
        ticks = 0;
        wait = 0;
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(ticks);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset$();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset$();
    }
}