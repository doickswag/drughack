package ru.drughack.modules.impl.combat;

import net.minecraft.util.Hand;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventMouse;
import ru.drughack.api.event.impl.EventPlayerUpdate;
import ru.drughack.api.mixins.accesors.IMinecraftClient;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.math.MathUtils;
import ru.drughack.utils.math.TimerUtils;

public class AutoClicker extends Module {

    private final Setting<Integer> maxCps = new Setting<>("Max Cps", 16, 1, 25);
    private final Setting<Integer> minCps = new Setting<>("Min Cps", 13, 1, 25);

    public AutoClicker() {
        super("AutoClicker", "auto click (for 1.8)", Category.Combat);
    }

    private final TimerUtils attackTimer = new TimerUtils();
    private boolean isClick;

    @EventHandler
    public void onPlayerUpdate(EventPlayerUpdate e) {
        float maxDelay = 1000f / maxCps.getValue();
        float minDelay = 1000f / minCps.getValue();
        if (attackTimer.hasTimeElapsed(MathUtils.random(maxDelay, minDelay)) && isClick && mc.currentScreen == null) {
            if (!mc.player.isUsingItem()) {
                ((IMinecraftClient) mc).attack();
                mc.player.swingHand(Hand.MAIN_HAND);
                attackTimer.reset();
            }
        }
    }

    @Override
    public void onMouse(EventMouse e) {
        super.onMouse(e);
        if (e.getButton() == 0) {
            if (e.getAction() == 1) isClick = true;
            else if (e.getAction() == 0) isClick = false;
        }
    }
}