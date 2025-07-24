package ru.drughack.modules.settings.impl;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

@Setter
@Getter
public class Bind {
    private int key;
    private boolean hold;
    private final boolean mouse;

    public Bind(int key, boolean hold, boolean mouse) {
        this.key = key;
        this.hold = hold;
        this.mouse = mouse;
    }

    public String getBind() {
        if (mouse) return "M" + (key + 1);

        if (key == -1) return "None";

        String kn = null;
        try {
            for (Field declaredField : GLFW.class.getDeclaredFields()) {
                if (declaredField.getName().startsWith("GLFW_KEY_")) {
                    int a = (int) declaredField.get(null);
                    if (a == this.key) {
                        String nb = declaredField.getName().substring("GLFW_KEY_".length());
                        kn = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
                        break;
                    }
                }
            }
        } catch (Exception ignore) {
            kn = "unknown." + this.key;
        }

        if (kn == null) return "";
        return kn.toUpperCase();
    }

    public boolean isEmpty() {
        return this.key < 0;
    }

    public String toString() {
        return this.isEmpty() ? "None" : (this.key < 0
                ? "None"
                : this.capitalise(InputUtil.fromKeyCode(this.key, 0).getTranslationKey()));
    }

    private String capitalise(String str) {
        if (str.isEmpty()) return "";
        return Character.toUpperCase(str.charAt(0)) + (str.length() != 1 ? str.substring(1).toLowerCase() : "");
    }
}