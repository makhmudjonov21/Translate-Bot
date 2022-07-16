package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintsOrOffers {
    private UUID id = UUID.randomUUID();
    private String text;
    private LocalDateTime localDateTime;
    private String answer;
    private TgUser tgUser;

    public ComplaintsOrOffers(String text, LocalDateTime localDateTime, TgUser tgUser) {
        this.text = text;
        this.localDateTime = localDateTime;
        this.tgUser = tgUser;
    }

    public ComplaintsOrOffers(String text) {
        this.text = text;
    }
}
