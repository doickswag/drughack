package ru.drughack.commands.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import ru.drughack.commands.Command;
import ru.drughack.utils.interfaces.Wrapper;

import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FakePlayerCommand extends Command implements Wrapper {
    boolean spawn = false;
    public static OtherClientPlayerEntity player = null;

    public FakePlayerCommand() {
        super("<empty>", "FakePlayer", "Spawns the fakeplayer", "fakeplayer");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .executes(context -> {
                    spawn = !spawn;
                    if (spawn) {
                        if (mc.player == null || mc.world == null) return SINGLE_SUCCESS;
                        player = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), "drughack.cc"));
                        player.copyPositionAndRotation(mc.player);
                        player.setId(-673);
                        player.copyFrom(mc.player);
                        player.setHealth(20f);
                        player.setAbsorptionAmount(16f);
                        NbtCompound compoundTag = new NbtCompound();
                        mc.player.writeCustomDataToNbt(compoundTag);
                        player.readCustomDataFromNbt(compoundTag);
                        mc.world.addEntity(player);
                        player.tick();
                        sendMessage("Successfully spawn the fakeplayer");
                    } else {
                        if (mc.player == null || mc.world == null) return SINGLE_SUCCESS;
                        mc.world.removeEntity(player.getId(), Entity.RemovalReason.DISCARDED);
                        sendMessage("Successfully removed the fakeplayer");
                    }

                    return SINGLE_SUCCESS;
                });
    }
}
