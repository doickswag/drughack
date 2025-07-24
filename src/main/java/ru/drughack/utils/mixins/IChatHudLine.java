package ru.drughack.utils.mixins;

public interface IChatHudLine {
    boolean drughack$isClientMessage();

    void drughack$setClientMessage(boolean clientMessage);

    String drughack$getClientIdentifier();

    void drughack$setClientIdentifier(String clientIdentifier);
}