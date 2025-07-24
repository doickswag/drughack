package ru.drughack.modules.impl.client;

import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventChatSend;
import ru.drughack.api.event.impl.EventClientConnect;
import ru.drughack.api.event.impl.EventServerConnect;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.impl.CategoryBooleanSetting;
import ru.drughack.utils.formatting.CustomFormatting;
import ru.drughack.utils.math.TimerUtils;

public class IRC extends Module {

    public Setting<CategoryBooleanSetting> chat = new Setting<>("Chat", new CategoryBooleanSetting(true));
    public Setting<String> prefix = new Setting<>("Prefix", ";", v -> chat.getValue().isOpen());
    public Setting<Boolean> capes = new Setting<>("Capes", true);

    public IRC() {
        super("IRC", "chat in minecraft between users \n" + Formatting.YELLOW + "in beta test", Category.Client);
    }

    private final TimerUtils timer = new TimerUtils();

    @EventHandler
    public void onTick(EventTick e) {
        if (timer.hasTimeElapsed(5000)) {
            DrugHack.getInstance().getGvobavs().onUpdate();
            timer.reset();
        }
    }

    @EventHandler
    public void onChatSend(EventChatSend e) {
        if (e.getMessage().startsWith(prefix.getValue()) && chat.getValue().isEnabled()) {
            if (!e.isCanceled()) e.cancel();
            DrugHack.getInstance().getIvc().getSocket().sendText(Formatting.WHITE + "<" + CustomFormatting.CLIENT + DrugHack.getInstance().getProtection().getUser() + Formatting.WHITE + "> " + Formatting.RESET + e.getMessage().substring(1), true);
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        DrugHack.getInstance().getGvobavs().onConnect();
        DrugHack.getInstance().getIvc().onConnect();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        DrugHack.getInstance().getGvobavs().onDisconnect();
        DrugHack.getInstance().getIvc().onDisconnect();
    }
}