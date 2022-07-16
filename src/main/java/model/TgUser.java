package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TgUser {
    private UUID id = UUID.randomUUID();
    private String chatId;
    private String botState;
    private String username;
    private String name;
    private String phoneNumber;
    private Float lan;
    private Float lat;
    private boolean admin;

    public TgUser(String chatId) {
        this.chatId = chatId;
    }

    public TgUser(String chatId, String username, String name, boolean admin) {
        this.chatId = chatId;
        this.username = username;
        this.name = name;
        this.admin = admin;
    }

    public TgUser(String chatId, String username, String name, String phoneNumber, boolean admin) {
        this.chatId = chatId;
        this.username = username;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.admin = admin;
    }

    public TgUser(String chatId, String username, String name, String phoneNumber, boolean admin, Float lan, Float lat) {
        this.chatId = chatId;
        this.username = username;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.admin = admin;
        this.lan = lan;
        this.lat = lat;
    }
}
