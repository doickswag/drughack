package ru.drughack.api.mixins.accesors;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeInventoryScreen.class)
public interface ICreativeInventoryScreen {

    @Accessor("selectedTab")
    static ItemGroup getSelectedTab() {
        throw new AssertionError();
    }
}