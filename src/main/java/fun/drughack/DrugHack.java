package fun.drughack;

import fun.drughack.managers.*;
import fun.drughack.screen.clickgui.ClickGui;
import fun.drughack.screen.mainmenu.MainMenu;
import fun.drughack.utils.mediaplayer.MediaPlayer;
import fun.drughack.utils.Wrapper;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.api.ModInitializer;
import lombok.*;

import java.io.File;
import java.lang.invoke.MethodHandles;

@Getter
public class DrugHack implements ModInitializer, Wrapper {

    @Getter private static DrugHack instance;

    private IEventBus eventHandler;
    private NotifyManager notifyManager;
    private FriendManager friendManager;
    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private ServerManager serverManager;
    private RotationManager rotationManager;
    private MacroManager macroManager;
    private HudManager hudManager;
    private ConfigManager configManager;
    private WaypointManager waypointManager;
    private ClickGui clickGui;
    private MainMenu mainMenu;
    private MediaPlayer mediaPlayer;
    @Setter private boolean panic = false;
    private long initTime;

    public static Logger LOGGER = LogManager.getLogger(DrugHack.class);
    private final File globalsDir = new File(mc.runDirectory, "drughack");
    private final File configsDir = new File(globalsDir, "configs");
    private final File abItemsDir = new File(globalsDir, "abitems");

    @Override
    public void onInitialize() {
        LOGGER.info("[DrugHack] Starting initialization.");
        initTime = System.currentTimeMillis();
        instance = this;
        createDirs(globalsDir, configsDir, abItemsDir);
        eventHandler = new EventBus();
        eventHandler.registerLambdaFactory("fun.drughack", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        notifyManager = new NotifyManager();
        friendManager = new FriendManager();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        serverManager = new ServerManager();
        rotationManager = new RotationManager();
        macroManager = new MacroManager();
        hudManager = new HudManager();
        configManager = new ConfigManager();
        waypointManager = new WaypointManager();
        clickGui = new ClickGui();
        mainMenu = new MainMenu();
        mediaPlayer = new MediaPlayer();
        LOGGER.info("[DrugHack] Successfully initialized for {} ms.", System.currentTimeMillis() - initTime);
    }

    private void createDirs(File... file) {
        for (File f : file) f.mkdirs();
    }

    public static Identifier id(String texture) {
        return Identifier.of("drughack", texture);
    }
}