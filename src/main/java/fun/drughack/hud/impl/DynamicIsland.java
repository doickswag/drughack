package fun.drughack.hud.impl;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.hud.HudElement;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.animations.infinity.InfinityAnimation;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.network.Server;
import fun.drughack.utils.render.ColorUtils;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.resource.language.I18n;

import java.awt.*;

public class DynamicIsland extends HudElement {

    private final Animation internetAnimation = new Animation(300, 1f, false, Easing.BOTH_SINE);
    private final Animation mediaAnimation = new Animation(300, 1f, false, Easing.BOTH_SINE);
    private final Animation pvpAnimation = new Animation(300, 1f, false, Easing.BOTH_SINE);
    private final InfinityAnimation widthAnimation = new InfinityAnimation(Easing.LINEAR);

    public DynamicIsland() {
        super("DynamicIsland");
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (Module.fullNullCheck() || DrugHack.getInstance().isPanic()) return;
        DrugHack.getInstance().getMediaPlayer().onTick();
    }

    @Override
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck() || closed()) return;

        String name = "DrugHack";
        String track =  DrugHack.getInstance().getMediaPlayer().getLastTitle() + (DrugHack.getInstance().getMediaPlayer().getArtist().isEmpty() ? "" : " - " + DrugHack.getInstance().getMediaPlayer().getArtist());
        String pvp = I18n.translate("elements.dynamicisland.pvp");
        String pvpTimer = Server.getPvpTimer().isEmpty() ? "30" : Server.getPvpTimer();
        boolean isPvp = Server.isPvp();
        boolean mediaNull = DrugHack.getInstance().getMediaPlayer().fullNullCheck();
        internetAnimation.update(Server.getPing(mc.player) < 150);
        mediaAnimation.update(!mediaNull && !isPvp);
        pvpAnimation.update(isPvp);
        float padding = 2f;
        float round = 6f;
        float width = widthAnimation.animate(15 + Fonts.BOLD.getWidth(isPvp ? pvp : mediaNull ? name : track, 7f) + padding * (isPvp ? 3 : 2), 200);
        float height = 15f;
        float x = mc.getWindow().getScaledWidth() / 2f - width / 2f;
        float y = 4f;
        Render2D.startScissor(e.getContext(), x, y, width + 1f, height);

        Render2D.drawStyledRect(
                e.getContext().getMatrices(),
                x,
                y,
                width,
                height,
                round,
                new Color(0, 0, 0, 200),
                255
        );

        if (!mediaNull && !isPvp) {
            Render2D.drawTexture(
                    e.getContext().getMatrices(),
                    x + padding,
                    y + padding,
                    height - padding * 2,
                    height - padding * 2,
                    4f,
                    DrugHack.getInstance().getMediaPlayer().getTexture(),
                    new Color(255, 255, 255, (int) (255 * mediaAnimation.getValue()))
            );
        } else if (!isPvp) {
            Render2D.drawRoundedRect(
                    e.getContext().getMatrices(),
                    x + padding,
                    y + padding,
                    height - padding * 2,
                    height - padding * 2,
                    4f,
                    ColorUtils.getGlobalColor((int) (255 * mediaAnimation.getReversedValue()))
            );
        } else if (isPvp) {
            Render2D.drawRoundedRect(
                    e.getContext().getMatrices(),
                    x + padding,
                    y + padding,
                    height + 2f - padding * 2,
                    height - padding * 2,
                    4f,
                    new Color(255, 0, 0, (int) (255 * pvpAnimation.getValue()))
            );

            Render2D.drawFont(e.getContext().getMatrices(),
                    Fonts.BOLD.getFont(6f),
                    pvpTimer,
                    x + height - Fonts.BOLD.getWidth(pvpTimer, 6f) / 2 - padding * 3.5f,
                    y - (padding / 2f) + (Fonts.BOLD.getHeight(6f) / 1.5f),
                    new Color(255, 255, 255, (int) (255 * pvpAnimation.getValue()))
            );
        }

        if (!mediaNull && !isPvp) {
            Render2D.drawFont(e.getContext().getMatrices(),
                    Fonts.BOLD.getFont(7f),
                    track,
                    x + height,
                    y - (padding / 2f) + (Fonts.BOLD.getHeight(7f) / 2f),
                    new Color(255, 255, 255, (int) (255 * mediaAnimation.getValue()))
            );
        } else if (!isPvp) {
            Render2D.drawFont(e.getContext().getMatrices(),
                    Fonts.BOLD.getFont(7f),
                    name,
                    x + height,
                    y - (padding / 2f) + (Fonts.BOLD.getHeight(7f) / 2f),
                    new Color(255, 255, 255, (int) (255 * mediaAnimation.getReversedValue()))
            );
        } else if (isPvp) {
            Render2D.drawFont(e.getContext().getMatrices(),
                    Fonts.BOLD.getFont(7f),
                    pvp,
                    x + height + padding,
                    y - (padding / 2f) + (Fonts.BOLD.getHeight(7f) / 2f),
                    new Color(255, 255, 255, (int) (255 * pvpAnimation.getValue()))
            );
        }

        Render2D.stopScissor(e.getContext());

        Render2D.drawFont(e.getContext().getMatrices(),
                Fonts.BOLD.getFont(7f),
                MathUtils.getCurrentTime(),
                x - (padding * 3f) - Fonts.BOLD.getWidth(MathUtils.getCurrentTime(), 7f),
                y - (padding / 2f) + (Fonts.BOLD.getHeight(7f) / 2f),
                Color.BLACK
        );

        Render2D.drawFont(e.getContext().getMatrices(),
                Fonts.ICONS.getFont(7f),
                "Q",
                x + width + (padding * 3f),
                y + padding + (Fonts.ICONS.getHeight(7f) / 2f) - 2f,
                new Color(0, 0, 0, (int) (255 * internetAnimation.getValue()))
        );

        Render2D.drawFont(e.getContext().getMatrices(),
                Fonts.ICONS.getFont(7f),
                "P",
                x + width + (padding * 3f),
                y + padding + (Fonts.ICONS.getHeight(7f) / 2f) - 2f,
                new Color(0, 0, 0, (int) (255 * internetAnimation.getReversedValue()))
        );

        super.onRender2D(e);
    }
}