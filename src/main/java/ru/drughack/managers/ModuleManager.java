package ru.drughack.managers;

import lombok.Getter;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventMouse;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.gui.ClickGuiScreen;
import ru.drughack.gui.HudEditorScreen;
import ru.drughack.modules.api.HudModule;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.impl.client.*;
import ru.drughack.modules.impl.combat.*;
import ru.drughack.modules.impl.exploit.*;
import ru.drughack.modules.impl.hud.*;
import ru.drughack.modules.impl.misc.*;
import ru.drughack.modules.impl.movement.*;
import ru.drughack.modules.impl.player.*;
import ru.drughack.modules.impl.render.*;
import ru.drughack.modules.settings.Setting;
import ru.drughack.utils.other.CursorUtils;

import java.lang.reflect.Field;
import java.util.*;

import static ru.drughack.utils.interfaces.Wrapper.mc;

@Getter
public class ModuleManager {

    public List<Module> modules = new ArrayList<>();
    public List<Integer> activeMouseKeys = new ArrayList<>();
    public boolean holdMouse;

    private Speed speed;
    private AutoRespawn autoRespawn;
    private FastUse fastUse;
    private Sprint sprint;
    private UI ui;
    private HitboxDesync hitboxDesync;
    private Notifications notifications;
    private ItemScroller itemScroller;
    private Watermark watermark;
    private HudEditor hudEditor;
    private ViewModel viewModel;
    private ModuleList moduleList;
    private Indicators indicators;
    private HoleESP holeESP;
    private TextRadar textRadar;
    private Step step;
    private FastFall fastFall;
    private FeetTrap feetTrap;
    private Renders renders;
    private NameProtect nameProtect;
    private AntiCheat antiCheat;
    private SpeedMine speedMine;
    private AutoTrap autoTrap;
    private SelfTrap selfTrap;
    private ViewClip viewClip;
    private NoRender noRender;
    private FontModule fontModule;
    private NameTags nameTags;
    private Offhand offHand;
    private Blocker blocker;
    private HoleFill holeFill;
    private Welcomer welcomer;
    private Aura aura;
    private TickShift tickShift;
    private NoSlow noSlow;
    private Velocity velocity;
    private Criticals criticals;
    private AutoReconnect autoReconnect;
    private FastLatency fastLatency;
    private CrosshairIndicators crosshairIndicators;
    private AutoCrystal autoCrystal;
    private Suicide suicide;
    private MultiTask multiTask;
    private Freecam freecam;
    private Chams chams;
    private Announcer announcer;
    private Reach reach;
    private Fullbright fullbright;
    private Replenish replenish;
    private Armor armor;
    private Totems totems;
    private AutoXP autoXP;
    private Metrics metrics;
    private NoRotate noRotate;
    private AutoEZ autoEZ;
    private Swing swing;
    private RPC rpc;
    private BetterChat betterChat;
    private Coordinates coordinates;
    private IRC irc;
    private AutoClicker autoClicker;
    private HitSounds hitSounds;
    private KillSounds killSounds;
    private Blink blink;
    private AutoMine autoMine;
    private Shaders shaders;
    private Phase phase;
    private Zoom zoom;
    private BreakHighlight breakHighlight;
    private Fly fly;
    private Disabler disabler;
    private BedTags bedTags;
    private NoFall noFall;
    private AirJump airJump;
    private ClickPearl clickPearl;
    private AutoSoup autoSoup;
    private NoJumpDelay noJumpDelay;
    private NoFriendDamage noFriendDamage;
    private AntiFireball antiFireball;
    private AutoPotion autoPotion;
    private ElytraSwap elytraSwap;
    private Spider spider;
    private Music music;
    private HighJump highJump;

    public ModuleManager() {
        DrugHack.getInstance().getEventHandler().subscribe(this);
        registerModules();
        for (Module module : modules) regiterSettings(module);
    }

    private void registerModules() {
        addModules(
                speed = new Speed(),
                autoRespawn = new AutoRespawn(),
                fastUse = new FastUse(),
                sprint = new Sprint(),
                ui = new UI(),
                hitboxDesync = new HitboxDesync(),
                notifications = new Notifications(),
                itemScroller = new ItemScroller(),
                watermark = new Watermark(),
                hudEditor = new HudEditor(),
                viewModel = new ViewModel(),
                moduleList = new ModuleList(),
                indicators = new Indicators(),
                holeESP = new HoleESP(),
                textRadar = new TextRadar(),
                step = new Step(),
                fastFall = new FastFall(),
                feetTrap = new FeetTrap(),
                renders = new Renders(),
                nameProtect = new NameProtect(),
                antiCheat = new AntiCheat(),
                speedMine = new SpeedMine(),
                autoTrap = new AutoTrap(),
                selfTrap = new SelfTrap(),
                viewClip = new ViewClip(),
                noRender = new NoRender(),
                fontModule = new FontModule(),
                nameTags = new NameTags(),
                offHand = new Offhand(),
                blocker = new Blocker(),
                holeFill = new HoleFill(),
                welcomer = new Welcomer(),
                aura = new Aura(),
                tickShift = new TickShift(),
                noSlow = new NoSlow(),
                velocity = new Velocity(),
                criticals = new Criticals(),
                autoReconnect = new AutoReconnect(),
                fastLatency = new FastLatency(),
                crosshairIndicators = new CrosshairIndicators(),
                autoCrystal = new AutoCrystal(),
                suicide = new Suicide(),
                multiTask = new MultiTask(),
                freecam = new Freecam(),
                chams = new Chams(),
                announcer = new Announcer(),
                reach = new Reach(),
                fullbright = new Fullbright(),
                replenish = new Replenish(),
                armor = new Armor(),
                totems = new Totems(),
                autoXP = new AutoXP(),
                metrics = new Metrics(),
                noRotate = new NoRotate(),
                autoEZ = new AutoEZ(),
                swing = new Swing(),
                rpc = new RPC(),
                betterChat = new BetterChat(),
                coordinates = new Coordinates(),
                irc = new IRC(),
                autoClicker = new AutoClicker(),
                hitSounds = new HitSounds(),
                killSounds = new KillSounds(),
                blink = new Blink(),
                autoMine = new AutoMine(),
                shaders = new Shaders(),
                phase = new Phase(),
                zoom = new Zoom(),
                breakHighlight = new BreakHighlight(),
                fly = new Fly(),
                disabler = new Disabler(),
                bedTags = new BedTags(),
                noFall = new NoFall(),
                airJump = new AirJump(),
                clickPearl = new ClickPearl(),
                autoSoup = new AutoSoup(),
                noJumpDelay = new NoJumpDelay(),
                noFriendDamage = new NoFriendDamage(),
                antiFireball = new AntiFireball(),
                autoPotion = new AutoPotion(),
                elytraSwap = new ElytraSwap(),
                spider = new Spider(),
                music = new Music(),
                highJump = new HighJump()
        );
    }

    private void regiterSettings(Module module) {
        try {

            for (Field field : module.getClass().getDeclaredFields()) {
                if (!Setting.class.isAssignableFrom(field.getType())) continue;
                field.setAccessible(true);
                Setting<?> setting = (Setting<?>) field.get(module);
                if (setting != null && !module.getSettings().contains(setting)) module.getSettings().add(setting);
            }

            if (!module.getSettings().contains(module.bind)) module.getSettings().add(module.bind);
            if (module instanceof HudModule hud && !module.getSettings().contains(hud.pos)) module.getSettings().add(hud.pos);
        } catch (IllegalAccessException e) {
            DrugHack.LOGGER.error("Failed to register settings for module: {}", module.getName(), e);
        }
    }

    @EventHandler
    private void onTick(EventTick e) {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ClickGuiScreen) && !(mc.currentScreen instanceof HudEditorScreen)) CursorUtils.setCursor(CursorUtils.ARROW);
    }

    private void addModules(Module... modulez) {
        Arrays.sort(modulez, Comparator.comparing(Module::getName));
        modules.addAll(List.of(modulez));
    }

    public Module getModule(String name) {
        for (Module module : modules) {
            if (!module.getName().equalsIgnoreCase(name)) continue;
            return module;
        }
        return null;
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (!clazz.isInstance(module)) continue;
            return (T) module;
        }
        return null;
    }

    public ArrayList<Module> getEnabledModules() {
        ArrayList<Module> enabledModules = new ArrayList<>();
        for (Module module : modules) {
            if (!module.isToggled()) continue;
            enabledModules.add(module);
        }
        return enabledModules;
    }

    public List<Category> getCategories() {
        return Arrays.asList(Category.values());
    }

    public List<Module> getModules(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).toList();
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        modules.stream().filter(Module::isToggled).forEach(module -> module.onRender2D(e));
    }

    @EventHandler
    public void onMouse(EventMouse e) {
        modules.stream().filter(Module::isToggled).forEach(module -> module.onMouse(e));
        if (e.getAction() == 0) holdMouse = false;
        if (e.getAction() == 1) holdMouse = true;
    }

    public void onKeyPressed(int eventKey) {
        if (eventKey == -1 || eventKey == 0) return;
        if (mc.currentScreen != null) return;

        modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey) module.toggle();
        });
    }

    public void onKeyReleased(int eventKey) {
        if (eventKey == -1 || eventKey == 0 ) return;
        if (mc.currentScreen != null) return;

        modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey && module.getBind().isHold()) module.disable();
        });
    }

    public void onMouseKeyPressed(int eventKey) {
        if (eventKey == -1) return;
        if (mc.currentScreen != null) return;

        modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + (eventKey + 1))) module.toggle();
        });
    }

    public void onMouseKeyReleased(int eventKey) {
        if (eventKey == -1) return;
        if (mc.currentScreen != null) return;
        activeMouseKeys.add(eventKey);

        modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + (eventKey + 1)) && module.getBind().isHold()) module.disable();
        });
    }
}