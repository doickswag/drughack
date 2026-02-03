package fun.drughack.managers;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventMouse;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.api.events.impl.EventKey;
import fun.drughack.modules.impl.combat.*;
import fun.drughack.modules.impl.movement.*;
import fun.drughack.modules.impl.render.*;
import fun.drughack.modules.impl.misc.*;
import fun.drughack.modules.impl.client.*;
import fun.drughack.modules.settings.Setting;
import fun.drughack.utils.Wrapper;
import lombok.Getter;
import meteordevelopment.orbit.EventHandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class ModuleManager implements Wrapper {

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        DrugHack.getInstance().getEventHandler().subscribe(this);
        addModules(
                new Sprint(),
                new UI(),
                new NameTags(),
                new GuiMove(),
                new RPC(),
                new MultiTask(),
                new Aura(),
                new NoPush(),
                new NoRender(),
                new FastUse(),
                new MoveFix(),
                new FakeLag(),
                new NoAttackCooldown(),
                new NoSlow(),
                new AutoVault(),
                new WindHop(),
                new TargetEsp(),
                new NoFriendDamage(),
                new NoEntityTrace(),
                new Panic(),
                new NoJumpDelay(),
                new ItemHelper(),
                new AutoBuy(),
                new AuctionHelper(),
                new OffHand(),
                new AntiBot(),
                new DamageParticles(),
                new Fullbright(),
                new ElytraHelper(),
                new ClickPearl(),
                new ElytraForward(),
                new ElytraBooster(),
                new Targets(),
                new Teams(),
                new Scaffold(),
                new FuntimeHelper(),
                new AutoAccept(),
                new PotionTracker(),
                new UseTracker(),
                new ViewModel(),
                new ScoreboardHealth()
        );

        for (Module module : modules) {
            try {
                for (Field field : module.getClass().getDeclaredFields()) {
                    if (!Setting.class.isAssignableFrom(field.getType())) continue;
                    field.setAccessible(true);
                    Setting<?> setting = (Setting<?>) field.get(module);
                    if (setting != null && !module.getSettings().contains(setting)) module.getSettings().add(setting);
                }
            } catch (Exception ignored) {}
        }
    }

    private void addModules(Module... module) {
        this.modules.addAll(List.of(module));
    }

    @EventHandler
    public void onKey(EventKey e) {
        if (Module.fullNullCheck() || mc.currentScreen != null || DrugHack.getInstance().isPanic()) return;

        if (e.getAction() == 1)
            for (Module module : modules)
                if (module.getBind().getKey() == e.getKey() && !module.getBind().isMouse())
                    module.toggle();
    }

    @EventHandler
    public void onMouse(EventMouse e) {
        if (Module.fullNullCheck() || mc.currentScreen != null || DrugHack.getInstance().isPanic()) return;

        if (e.getAction() == 1)
            for (Module module : modules)
                if (module.getBind().getKey() == e.getButton() && module.getBind().isMouse())
                    module.toggle();
    }

    public List<Module> getModules(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).toList();
    }

    public List<Category> getCategories() {
        return Arrays.asList(Category.values());
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (!clazz.isInstance(module)) continue;
            return (T) module;
        }
        return null;
    }
}