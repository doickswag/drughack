package ru.drughack.gui.api;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import ru.drughack.DrugHack;
import ru.drughack.utils.other.DrugEvents;

@Getter @Setter
public class Button {
    private boolean state;
    protected float x, y, width, height;
    private boolean hidden;
    private final String name, description;

    public Button(String name, String description) {
        this.name = name;
        this.height = 13;
        this.description = description;
        this.x = -25;
        this.y = -25;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {}
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) { if (mouseButton == 0 && isHovering(mouseX, mouseY)) this.state = !this.state; }
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) { }
    public void update() {}
    public void onKeyTyped(char typedChar, int keyCode) { }
    public void onKeyPressed(int key) { }
    public boolean getState() { return this.state; }
    public boolean isHovering(int mouseX, int mouseY) { return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + getHeight(); }
    public void playSound(String type) {
        if (type.equalsIgnoreCase("click")) {
            DrugHack.getInstance().getSoundManager().playSound(DrugEvents.clickEvent, DrugHack.getInstance().getModuleManager().getUi().volume.getValue());
        } else if (type.equalsIgnoreCase("rclick")) {
            DrugHack.getInstance().getSoundManager().playSound(DrugEvents.rclickEvent, DrugHack.getInstance().getModuleManager().getUi().volume.getValue());
        } else if (type.equalsIgnoreCase("hover")) {
            DrugHack.getInstance().getSoundManager().playSound(DrugEvents.hoverEvent, DrugHack.getInstance().getModuleManager().getUi().volume.getValue());
        }
    }
}