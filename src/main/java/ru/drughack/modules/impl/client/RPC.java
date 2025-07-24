package ru.drughack.modules.impl.client;

import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import ru.drughack.DrugHack;
import meteordevelopment.orbit.EventHandler;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.modules.api.Category;
import ru.drughack.modules.api.Module;
import ru.drughack.modules.settings.Setting;

public class RPC extends Module {

    private final RichPresence rpc = new RichPresence();

    public Setting<Boolean> uid = new Setting<>("UID", true);
    public Setting<Boolean> server = new Setting<>("Server", false);

    public RPC() {
        super("RPC", "discord activity", Category.Client);
        toggle();
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (DiscordIPC.getUser() != null) {
            if (server.getValue()) {
                if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getConnection() != null && mc.getCurrentServerEntry() != null) {
                    rpc.setDetails("playing: " + mc.getCurrentServerEntry().address);
                } else if (mc.isInSingleplayer()) {
                    rpc.setDetails("playing: singleplayer");
                } else {
                    rpc.setDetails("in menu");
                }
            }
            if (uid.getValue()) rpc.setState("uid: " + DrugHack.getInstance().getProtection().getUid());
            DiscordIPC.setActivity(rpc);
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        DiscordIPC.start(1367135636548747375L, null);
        rpc.setStart(System.currentTimeMillis() / 1000);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        DiscordIPC.stop();
    }
}