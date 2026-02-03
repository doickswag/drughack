package fun.drughack.modules.impl.misc;

import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.impl.BooleanSetting;

public class NoPush extends Module {

    public BooleanSetting players = new BooleanSetting("settings.nopush.players", true);
    public BooleanSetting blocks = new BooleanSetting("settings.nopush.blocks", true);
    public BooleanSetting water = new BooleanSetting("settings.nopush.water", true);

    public NoPush() {
        super("NoPush", Category.Misc);
    }
}