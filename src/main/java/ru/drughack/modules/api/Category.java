package ru.drughack.modules.api;

import lombok.AllArgsConstructor;
import ru.drughack.modules.settings.api.Nameable;

@AllArgsConstructor
public enum Category implements Nameable {
    Combat("Combat"),
    Misc("Misc"),
    Render("Render"),
    Movement("Movement"),
    Player("Player"),
    Exploit("Exploit"),
    Client("Client"),
    Hud("Hud");

    private final String name;

    @Override
    public String getName() {
        return name;
    }
}