package ru.drughack;

import lombok.*;
import lombok.experimental.FieldDefaults;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.drughack.managers.*;
import ru.drughack.gui.ClickGuiScreen;
import ru.drughack.gui.HudEditorScreen;
import ru.drughack.api.protection.*;

import java.io.File;
import java.lang.invoke.MethodHandles;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DrugHack implements ModInitializer {

    @Getter private static DrugHack instance;

    //event
    private IEventBus eventHandler;

    //protection
    private Gvobavs gvobavs;
    private IVC ivc;
    private Protection protection;
    private MediaPlayer mediaPlayer;

    //managers
    private ChatManager chatManager;
    private FontManager fontManager;
    private FriendManager friendManager;
    private WorldManager worldManager;
    private PositionManager positionManager;
    private RotationManager rotationManager;
    private ServerManager serverManager;
    private RenderManager renderManager;
    private TargetManager targetManager;
    private ModuleManager moduleManager;
    private CommandManager commandManager;
    private ClickGuiScreen clickGuiScreen;
    private HudEditorScreen hudEditorScreen;
    private ConfigManager configManager;
    private SoundManager soundManager;
    private ShaderManager shaderManager;

    //dirs
    private final File clientDir = new File(MinecraftClient.getInstance().runDirectory, File.separator + "drughack");
    private final File configDir = new File(clientDir + File.separator + "configs");

    //others
    public static long initTime;
    public static final Logger LOGGER = LogManager.getLogger("DrugHack");

    @Override
    public void onInitialize() {
        LOGGER.info("[DrugHack] Starting initialization.");
        initTime = System.currentTimeMillis();
        instance = this;
        createDirs(clientDir, configDir);
        LOGGER.info("[DrugHack] Starting initialization events.");
        eventHandler = new EventBus();
        eventHandler.registerLambdaFactory("ru.drughack", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        LOGGER.info("[DrugHack] Successfully initialized events.");
        LOGGER.info("[DrugHack] Starting initialization protection.");
        gvobavs = new Gvobavs();
        ivc = new IVC();
        protection = new Protection();
        mediaPlayer = new MediaPlayer();
        LOGGER.info("[DrugHack] Successfully initialized protection.");
        LOGGER.info("[DrugHack] Starting initialization managers.");
        chatManager = new ChatManager();
        fontManager = new FontManager();
        friendManager = new FriendManager();
        worldManager = new WorldManager();
        positionManager = new PositionManager();
        rotationManager = new RotationManager();
        serverManager = new ServerManager();
        renderManager = new RenderManager();
        targetManager = new TargetManager();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        clickGuiScreen = new ClickGuiScreen();
        hudEditorScreen = new HudEditorScreen();
        configManager = new ConfigManager();
        soundManager = new SoundManager();
        shaderManager = new ShaderManager();
        LOGGER.info("[DrugHack] Successfully initialized managers.");
        LOGGER.info("[DrugHack] Successfully initialized for {} ms.", System.currentTimeMillis() - initTime);
    }

    private void createDirs(File... file) {
        for (File f : file) if (!f.exists()) f.mkdirs();
    }
}