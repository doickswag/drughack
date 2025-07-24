package ru.drughack.api.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.drughack.DrugHack;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {

    public AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "getSkinTextures", at = @At(value = "RETURN"), cancellable = true)
    private void getCape(CallbackInfoReturnable<SkinTextures> cir) {
        SkinTextures skinTextures = cir.getReturnValue();
        Identifier texture = skinTextures.texture();
        Identifier drugCapeTexture = Identifier.of("drughack", "textures/capes/drughack.png");
        Identifier THCapeTexture = Identifier.of("drughack", "textures/capes/thunderhack.png");
        SkinTextures.Model model = skinTextures.model();
        String textureUrl = skinTextures.textureUrl();
        boolean secure = skinTextures.secure();
        boolean irc = DrugHack.getInstance().getModuleManager().getIrc().isToggled() && DrugHack.getInstance().getModuleManager().getIrc().capes.getValue();
        boolean isDrugHack = irc && DrugHack.getInstance().getGvobavs().isUser(getGameProfile().getName());
        boolean isTH = irc && DrugHack.getInstance().getGvobavs().isTHUser(getGameProfile().getName());
        if (isDrugHack) cir.setReturnValue(new SkinTextures(texture, textureUrl, drugCapeTexture, drugCapeTexture, model, secure));
        else if (isTH) cir.setReturnValue(new SkinTextures(texture, textureUrl, THCapeTexture, THCapeTexture, model, secure));
    }
}