package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationCode {
    private UUID id = UUID.randomUUID();
    private TgUser user;
    private String code;
    private boolean verified;

    public VerificationCode(TgUser user) {
        this.user = user;
    }
}
