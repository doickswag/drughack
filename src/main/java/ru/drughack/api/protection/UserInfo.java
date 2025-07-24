package ru.drughack.api.protection;

import lombok.*;

@Getter @Setter @AllArgsConstructor
public class UserInfo {
    public String user, password, hwid;
}