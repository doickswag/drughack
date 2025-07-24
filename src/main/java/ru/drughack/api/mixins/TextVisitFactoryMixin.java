package ru.drughack.api.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.drughack.DrugHack;
import ru.drughack.modules.impl.misc.NameProtect;
import ru.drughack.utils.interfaces.Wrapper;
import ru.drughack.utils.formatting.CustomFormatting;
import ru.drughack.utils.formatting.FormattingUtils;

@Mixin(TextVisitFactory.class)
public abstract class TextVisitFactoryMixin implements Wrapper {
    @WrapOperation(method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At(value = "INVOKE", target = "Ljava/lang/String;charAt(I)C", ordinal = 1))
    private static char visitFormatted(String instance, int index, Operation<Character> original, @Local(ordinal = 2) LocalRef<Style> style) {
        CustomFormatting customFormatting = CustomFormatting.byCode(instance.charAt(index));
        if (customFormatting != null) style.set(FormattingUtils.withExclusiveFormatting(style.get(), customFormatting));

        return original.call(instance, index);
    }

    @ModifyVariable(method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static String replaceText(String value) {
        NameProtect module = DrugHack.getInstance().getModuleManager().getNameProtect();
        if (module.isToggled()) return value.replaceAll(mc.getSession().getUsername(), module.name.getValue());
        return value;
    }
}
