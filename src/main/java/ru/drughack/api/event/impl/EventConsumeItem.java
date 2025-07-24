package ru.drughack.api.event.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import ru.drughack.api.event.Event;

@Getter @AllArgsConstructor
public class EventConsumeItem extends Event {
    private final ItemStack stack;
}