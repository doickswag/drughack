package ru.drughack.utils.other;

import org.lwjgl.glfw.GLFW;

import static ru.drughack.utils.interfaces.Wrapper.mc;

public class CursorUtils {

    public static long ARROW = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
    public static long HAND = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
    public static long RESIZEH = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR);
    public static long IBEAM = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);

    public static void setCursor(long type) {
        if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) GLFW.glfwSetCursor(mc.getWindow().getHandle(), type);
    }
}