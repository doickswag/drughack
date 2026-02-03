package fun.drughack.hud.impl;

import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.hud.HudElement;
import fun.drughack.utils.math.Counter;
import fun.drughack.utils.network.Server;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Font;
import fun.drughack.utils.render.fonts.Fonts;

import java.awt.*;

public class Watermark extends HudElement {

    public Watermark() {
        super("Watermark");
    }

    @Override
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck() || closed()) return;
       
        float x = getX();
        float y = getY();
        float padding = 5f;
        float fontSize = 8f;
        String name = "DrugHack Recode";
        String ping = Server.getPing(mc.player) + "ms";
        String fps = Counter.getCurrentFPS() + "fps";
        Font font = Fonts.BOLD;
        float width1 = Fonts.BOLD.getWidth(name, fontSize);
        float width2 = font.getWidth(fps, fontSize);
        float width3 = font.getWidth(ping, fontSize);
        float finalWidth = width1 + width2 + width3 + (padding * 8.125f);
        float height = 17f;
        e.getContext().getMatrices().push();
        e.getContext().getMatrices().translate(x + finalWidth / 2, y + height / 2, 0f);
        e.getContext().getMatrices().scale(toggledAnimation.getValue(), toggledAnimation.getValue(), 0);
        e.getContext().getMatrices().translate(-(x + finalWidth / 2), -(y + height / 2), 0f);
        Render2D.drawStyledRect(e.getContext().getMatrices(), x, y, finalWidth, height, 3.5f, new Color(0, 0, 0, 200), 255);
        Render2D.drawFont(e.getContext().getMatrices(), font.getFont(fontSize), name, x + padding * 3.5f, y + 3.5f, Color.WHITE);
        Render2D.drawFont(e.getContext().getMatrices(), font.getFont(fontSize), fps, x + width1 + padding * 5.25f, y + 4f, Color.WHITE);
        Render2D.drawFont(e.getContext().getMatrices(), font.getFont(fontSize), ping, x + width1 + width2 + padding * 7.125f, y + 4f, Color.WHITE);
        Render2D.drawFont(e.getContext().getMatrices(), Fonts.ICONS.getFont(11f), "R", x + padding / 2f, y + 3.5f, Color.WHITE);
        Render2D.drawRoundedRect(e.getContext().getMatrices(), x + width1 + padding * 4.125f + 0.5f, y + 3, 2, 11.5f, 0, new Color(75, 75, 75, 150));
        Render2D.drawRoundedRect(e.getContext().getMatrices(), x + width1 + width2 + padding * 6f - 0.125f + 0.5f, y + 3, 2, 11.5f, 0, new Color(75, 75, 75, 150));
        e.getContext().getMatrices().pop();
        setBounds(getX(), getY(), finalWidth, height);
        super.onRender2D(e);
    }
}