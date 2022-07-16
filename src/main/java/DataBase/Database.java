package DataBase;

import model.ComplaintsOrOffers;
import model.TgUser;
import model.TranslationHistory;
import model.VerificationCode;

import java.util.ArrayList;
import java.util.List;

public class Database {
    public static List<TgUser> tgUserList = new ArrayList<>();
    public static List<VerificationCode> verificationCodeList = new ArrayList<>();
    public static List<ComplaintsOrOffers> complaintsOrOffersList = new ArrayList<>();
    public static List<TranslationHistory> translationHistory = new ArrayList<>();
}
