package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranslationHistory {
    private TgUser user;
    private String str;
    private String translation;
    private String fromLang;
    private String toLang;
}
