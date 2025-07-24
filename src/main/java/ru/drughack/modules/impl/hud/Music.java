package ru.drughack.modules.impl.hud;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.texture.AbstractTexture;
import ru.drughack.DrugHack;
import ru.drughack.api.event.impl.EventRender2D;
import ru.drughack.api.event.impl.EventTick;
import ru.drughack.managers.FontManager;
import ru.drughack.modules.api.HudModule;
import ru.drughack.utils.render.ColorUtils;
import ru.drughack.utils.render.Renderer2D;

import java.awt.*;

public class Music extends HudModule {

    public Music() {
        super("Music", -1, -1);
    }

    private AbstractTexture texture;

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;
        DrugHack.getInstance().getMediaPlayer().onTick();
        if (DrugHack.getInstance().getMediaPlayer().isChangeTrack()) {
            DrugHack.getInstance().getChatManager().message("Change");
            if (texture != null) texture.close();
            texture = Renderer2D.convertToTexture(DrugHack.getInstance().getMediaPlayer().getImage());
            DrugHack.getInstance().getMediaPlayer().setChangeTrack(false);
        }
    }

    @Override
    public void onRender2D(EventRender2D e) {
        super.onRender2D(e);
        float height = 0;
        float scale = 0.5f;
        FontManager font = DrugHack.getInstance().getFontManager();
        Renderer2D.renderQuad(e.getContext().getMatrices(), getX(), getY(), getX() + getWidth(), getY() + getHeight(), Color.BLACK);
        if (!DrugHack.getInstance().getMediaPlayer().fullNullCheck()) {

            //Scissor
            e.getContext().enableScissor((int) getX(), (int) getY(), (int) (getX() + getWidth()), (int) (getY() + getHeight()));

            //Title
            font.drawTextWithShadow(e.getContext(),
                    DrugHack.getInstance().getMediaPlayer().getTitle(),
                    (int) (getX() - font.getWidth(DrugHack.getInstance().getMediaPlayer().getTitle()) / 2f + getWidth() / 2f + 52f / 2f),
                    (int) (getY() + getHeight() / 4f - font.getHeight() / 2f),
                    ColorUtils.getGlobalColor()
            );
            height += font.getHeight() + 1f;

            //Artist
            font.drawTextWithShadow(e.getContext(),
                    DrugHack.getInstance().getMediaPlayer().getArtist(),
                    (int) (getX() - font.getWidth(DrugHack.getInstance().getMediaPlayer().getArtist()) / 2f + getWidth() / 2f + 52f / 2f),
                    (int) (getY() + height + getHeight() / 4f - font.getHeight() / 2f),
                    ColorUtils.getGlobalColor()
            );

            //Time
            height += font.getHeight() + 1f;
            font.drawTextWithShadow(e.getContext(),
                    DrugHack.getInstance().getMediaPlayer().getCurrentPosition() + "/" + DrugHack.getInstance().getMediaPlayer().getCurrentDuration(),
                    (int) (getX() - font.getWidth(DrugHack.getInstance().getMediaPlayer().getCurrentPosition() + "/" + DrugHack.getInstance().getMediaPlayer().getCurrentDuration()) / 2f + getWidth() / 2f + 52f / 2f),
                    (int) (getY() + height + getHeight() / 4f - font.getHeight() / 2f),
                    ColorUtils.getGlobalColor()
            );

            //Owner
            e.getContext().getMatrices().push();
            e.getContext().getMatrices().scale(scale, scale, scale);
            font.drawTextWithShadow(e.getContext(),
                    DrugHack.getInstance().getMediaPlayer().getOwner().substring(0, 1).toUpperCase() + DrugHack.getInstance().getMediaPlayer().getOwner().substring(1),
                    (int) ((getX() + getWidth() - font.getWidth(DrugHack.getInstance().getMediaPlayer().getOwner()) * scale) / scale) - 2,
                    (int) ((getY() + getHeight() - font.getHeight() * scale) / scale) - 2,
                    ColorUtils.getGlobalColor()
            );
            e.getContext().getMatrices().pop();

            //Line
            Renderer2D.renderQuad(e.getContext().getMatrices(),
                    getX() + 55,
                    getY() + getHeight() - 10,
                    (getX() + 55) + (90 * DrugHack.getInstance().getMediaPlayer().getWidth()),
                    getY() + getHeight() - 8,
                    ColorUtils.getGlobalColor()
            );

            //Texture
            if (texture != null) Renderer2D.renderTexture(e.getContext().getMatrices(), getX(), getY(), getX() + 50, getY() + 50, texture, Color.WHITE);

            //Scissor
            e.getContext().disableScissor();
        } else {
            scale = 1.5f;
            e.getContext().getMatrices().push();
            e.getContext().getMatrices().scale(scale, scale, scale);
            font.drawTextWithShadow(e.getContext(),
                    "Nothing's playing.",
                    (int) ((getX() - font.getWidth("Nothing's playing.") * scale / 2f + getWidth() / 2f) / scale),
                    (int) ((getY() + getHeight() / 2f - font.getHeight() / 2f) / scale),
                    ColorUtils.getPulse(ColorUtils.getGlobalColor())
            );
            e.getContext().getMatrices().pop();
        }

        Renderer2D.renderOutline(e.getContext().getMatrices(), getX(), getY(), getX() + getWidth(), getY() + getHeight(), ColorUtils.getPulse(ColorUtils.getGlobalColor()));
        setBounds(getX(), getY(), 150, 50);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        texture = Renderer2D.convertToTexture(DrugHack.getInstance().getMediaPlayer().getImage());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (texture != null) texture.close();
    }
}