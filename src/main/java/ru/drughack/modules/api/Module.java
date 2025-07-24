package ru.drughack.modules.api;

import lombok.Getter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventMouse;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.modules.settings.impl.Bind;
import ru.drughack.modules.settings.Setting;
import ru.drughack.modules.settings.builders.BindSetting;
import ru.drughack.utils.animations.Animation;
import ru.drughack.utils.animations.Easing;
import ru.drughack.utils.interfaces.Wrapper;

import java.util.ArrayList;
import java.util.List;

public class Module implements Wrapper {
    @Getter private final Animation animation = new Animation(Easing.CIRC_OUT, 350);
    @Getter private final String description;
    @Getter private final Category category;
    @Getter private final String name;
    @Getter private boolean toggled;

    public Setting<Bind> bind = BindSetting.builder("Key")
            .defaultValue(new Bind(-1, false, false))
            .description("bind of module")
            .build().toSetting();

    @Getter public List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void onEnable() {}

    public void onDisable() {}

    public void onRender2D(EventRender2D e) {}

    public void onMouse(EventMouse e) {}

    public String getDisplayInfo() {
        return null;
    }

    public void setToggled(boolean enabled) {
        if (enabled) this.enable();
        else this.disable();
    }

    public void enable() {
        this.toggled = true;
        this.onEnable();
        animation.reset();
        DrugHack.getInstance().getEventHandler().subscribe(this);
        if ((!fullNullCheck()) && DrugHack.getInstance().getModuleManager().getNotifications().isToggled()) DrugHack.getInstance().getChatManager().added(Formatting.WHITE + getName(), getName().toLowerCase());
    }

    public void disable() {
        this.toggled = false;
        this.onDisable();
        animation.reset();
        DrugHack.getInstance().getEventHandler().unsubscribe(this);
        if ((!fullNullCheck()) && DrugHack.getInstance().getModuleManager().getNotifications().isToggled()) DrugHack.getInstance().getChatManager().deleted(Formatting.WHITE + getName(), getName().toLowerCase());
    }

    public void toggle() {
        this.setToggled(!this.isToggled());
    }

    public Bind getBind() {
        return this.bind.getValue();
    }

    public void setBind(int key, boolean hold, boolean mouse) {
        this.bind.setValue(new Bind(key, hold, mouse));
    }

    public boolean isKeyPressed(int button) {
        if (button == -1) return false;

        if (DrugHack.getInstance().getModuleManager().activeMouseKeys.contains(button)) {
            DrugHack.getInstance().getModuleManager().activeMouseKeys.clear();
            return true;
        }

        if (button < 10) return false;

        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), button);
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    public Setting<?> getSettingByName(String name) {
        for (Setting<?> setting : settings) {
            if (!setting.getName().equalsIgnoreCase(name)) continue;
            return setting;
        }
        return null;
    }

    public void resetValues() {
        for (Setting<?> uncastedSetting : settings) uncastedSetting.reset();
    }
}