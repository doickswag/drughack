package ru.drughack.api.mixins.accesors;

import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextColor.class)
public interface ITextColor {
    @Invoker("<init>")
    static TextColor create(int rgb, String name) {
        throw new AssertionError();
    }
}