package Bot;

import DataBase.Database;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import lombok.SneakyThrows;
import model.TgUser;
import model.VerificationCode;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class BotService {

    public static final String TWILLIO_SID = "ACd37a48bbeacddd7537950eb023008685";
    public static final String TWILLIO_TOKEN = "52789eac9b2bc0e0fbeb7523cf0cc3d9";
    public static final String TWILLIO_PHONE = "+15306275036";
    public static String code = "";
    public static int page = 1;


    //GET CHAT ID  method*********************************************************
    public static String getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        }
        return "";
    }


    //GET USERNAME method********************************************
    public static String getUsername(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getUserName();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getFrom().getUserName();
        }
        return "";
    }


    //GET NAME method********************************************
    public static String getName(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getFirstName();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getFrom().getFirstName();
        }
        return "";
    }


    //GET OR CREATE TG USER  method*********************************************************
    public static TgUser getOrCreateTgUser(String chatId, Update update) {
        String username = getUsername(update);
        String name = getName(update);
        for (TgUser tgUser : Database.tgUserList) {
            if (tgUser.getChatId().equals(chatId)) {
                return tgUser;
            }
        }
        TgUser newUser = new TgUser(chatId, username, name, false);
        Database.tgUserList.add(newUser);
        return newUser;
    }


    //GET OR CREATE VERIFICATION CODE  method*********************************************************
    public static VerificationCode getOrCreateVerificationCode(TgUser user) {
        for (VerificationCode verification : Database.verificationCodeList) {
            if (verification.getUser().getChatId().equals(user.getChatId())) {
                return verification;
            }
        }
        VerificationCode verification = new VerificationCode(user);
        Database.verificationCodeList.add(verification);
        return verification;
    }


    //SAVE USER CHANGES  method*********************************************************
    public static TgUser saveUserChanges(TgUser changedUser) {
        for (TgUser tgUser : Database.tgUserList) {
            if (tgUser.getChatId().equals(changedUser.getChatId())) {
                tgUser = changedUser;
                return tgUser;
            }
        }
        return null;
    }


    //SAVE TWILIO VERIFICATION CHANGES  method*********************************************************
    public static VerificationCode saveVerificationCodeChanges(VerificationCode changedVerification) {
        for (VerificationCode verification : Database.verificationCodeList) {
            if (verification.getId().equals(changedVerification.getId())) {
                verification = changedVerification;
                return verification;
            }
        }
        return null;
    }


    //USER LIST method**************************************************
    public static SendMessage userList(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setText(Database.tgUserList.toString());
        return sendMessage;
    }


    //CHECK PHONE NUMBER method********************************************
    public static String checkPhoneNumber(String phoneNumber) {
        return phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
    }


    //GENERATE REPLY KEYBOARD MARKUP method******************************************
    public static ReplyKeyboardMarkup generateMarkup(TgUser user) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardButton row1button1 = new KeyboardButton();
        if (user.getBotState().equals(BotState.SHARE_CONTACT)) {
            row1button1.setText("\uD83D\uDCF1Share contact\uD83D\uDCF1");
            row1button1.setRequestContact(true);
            row1.add(row1button1);
            rowList.add(row1);
        } else if (user.getBotState().equals(BotState.CREATE_AND_SEND_ADVERT1)) {
            row1button1.setText("\uD83D\uDCDDAdvert with text\uD83D\uDCDD");
            row1.add(row1button1);
            rowList.add(row1);
        } else if (user.getBotState().equals(BotState.CREATE_AND_SEND_NEWS1)) {
            row1button1.setText("♨️News with text♨️");
            row1.add(row1button1);
            rowList.add(row1);
        } else if (user.getBotState().equals(BotState.SEND_COMPLAINTS_OR_OFFERS)) {
            KeyboardButton row1button2 = new KeyboardButton();
            row1button1.setText("✅Yes✅");
            row1button2.setText("❌No❌");
            row1.add(row1button1);
            row1.add(row1button2);
            rowList.add(row1);

            KeyboardRow row2 = new KeyboardRow();
            KeyboardButton row2button1 = new KeyboardButton();
            row2button1.setText("\uD83D\uDD19Back to main menu\uD83D\uDD19");
            row2.add(row2button1);
            rowList.add(row2);
        } else if (user.getBotState().equals(BotState.SHOW_AND_ANSWER_TO_COMPLAINTS_OR_OFFERS)) {
            KeyboardButton row1button2 = new KeyboardButton();
            row1button1.setText("✅Yes✅");
            row1button2.setText("❌No❌");
            row1.add(row1button1);
            row1.add(row1button2);
            rowList.add(row1);

            KeyboardRow row2 = new KeyboardRow();
            KeyboardButton row2button1 = new KeyboardButton();
            row2button1.setText("\uD83D\uDD19Back to main menu\uD83D\uDD19");
            row2.add(row2button1);
            rowList.add(row2);
        }else if(user.getBotState().equals(BotState.ONE)){
            KeyboardButton row1button2 = new KeyboardButton();
            row1button1.setText("\uD83D\uDD8ASend answer");
            row1button2.setText("\uD83D\uDDD1Delete");
            row1.add(row1button1);
            row1.add(row1button2);
            rowList.add(row1);
        }
        markup.setKeyboard(rowList);
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        return markup;
    }


    //START method****************************************************
    public static SendMessage start(Update update, TgUser user) {
        return shareContact(update);
    }


    //SHARE CONTACT method*************************************************
    public static SendMessage shareContact(Update update) {
        String chatId = getChatId(update);
        TgUser user = getOrCreateTgUser(chatId, update);
        user.setBotState(BotState.SHARE_CONTACT);
        saveUserChanges(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("\uD83D\uDCF1Share your own phone number...");
        sendMessage.setReplyMarkup(generateMarkup(user));
        return sendMessage;
    }


    //CREATE CODE method*****************************************
    public static boolean createCode(TgUser user) {
        Random r = new Random();
        code = String.format("%06d", r.nextInt(999999));
        return true;
    }


    //SEND CODE method*****************************************
//    public static boolean sendCode(TgUser user){
//        Twilio.init(TWILLIO_SID, TWILLIO_TOKEN);
//        Random r = new Random();
//        code = String.format("%06d", r.nextInt(999999));
//        try {
//            Message message = com.twilio.rest.api.v2010.account.Message.creator(
//                    new PhoneNumber(user.getPhoneNumber()),
//                    new PhoneNumber(TWILLIO_PHONE),
//                    "Verification code: " + code).create();
//            TwilioVerification verification = getOrCreateTwilioVerification(user);
//            verification.setCode(code);
//            verification.setVerified(false);
//            saveTwilioVerificationChanges(verification);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//        return false;
//    }


    //GET CONTACT method*************************************************
    public static SendMessage getContact(Contact contact, TgUser user) {
        String phoneNumber = checkPhoneNumber(contact.getPhoneNumber());
        user.setPhoneNumber(phoneNumber);
        boolean createCode1 = createCode(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        if (createCode1) {
            user.setBotState(BotState.ENTER_CODE);
            sendMessage.setText("\uD83D\uDCE9This code is your verification code. Please enter verification code: " + code);
        } else {
            sendMessage.setText("\uD83D\uDCE9This code is your verification code. Please enter verification code: " + code);
        }
        saveUserChanges(user);
        return sendMessage;
    }


    //RESEND CODE method**********************************************
    public static SendMessage resendCode(Update update, TgUser user) {
        boolean createCode1 = createCode(user);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        if (createCode1) {
            sendMessage.setText("\uD83D\uDCE9This code is your verification code. Please, enter verification code : " + code);
        } else {
            sendMessage.setText("\uD83D\uDCE9This code is your verification code. Please, enter verification code : " + code);
        }
        saveUserChanges(user);
        return sendMessage;
    }


    //GET VERIFIED CODE method********************************************
//    public static boolean getVerifiedCode(Update update, TgUser user) {
//        TwilioVerification verification = getOrCreateTwilioVerification(user);
//        if (verification.getCode().equals(update.getMessage().getText())) {
//            verification.setVerified(true);
//            saveTwilioVerificationChanges(verification);
//            if(user.isAdmin()) {
//                user.setBotState(BotState.SHOW_ADMIN_MENU);
//            }else {
//                user.setBotState(BotState.SHOW_USER_MENU);
//            }
//            saveUserChanges(user);
//            return true;
//        } else {
//            return false;
//        }
//    }


    //GET VERIFIED CODE method********************************************
    public static boolean getVerifiedCode(Update update, TgUser user) {
        VerificationCode verification = getOrCreateVerificationCode(user);
        if (code.equals(update.getMessage().getText())) {
            verification.setVerified(true);
            saveVerificationCodeChanges(verification);
            if (user.isAdmin()) {
                user.setBotState(BotState.SHOW_ADMIN_MENU);
            } else {
                user.setBotState(BotState.SHOW_USER_MENU);
            }
            saveUserChanges(user);
            return true;
        } else {
            return false;
        }
    }


    //INLINE KEYBOARD BUTTON method****************************************
    public static InlineKeyboardMarkup inlineKeyboardMarkup(TgUser user) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton row1button1 = new InlineKeyboardButton();
        InlineKeyboardButton row1button2 = new InlineKeyboardButton();
        if (user.getBotState().equals(BotState.SHOW_ADMIN_MENU)) {
            row1button1.setText("\uD83E\uDD35\uD83C\uDFFC\u200D♂️\uD83E\uDDD1\uD83C\uDFFB\u200D⚖️Show users list\uD83E\uDD35\uD83C\uDFFC\u200D♂️\uD83E\uDDD1\uD83C\uDFFB\u200D⚖️");
            row1button1.setCallbackData("Show users list");
            row1.add(row1button1);
            row1button2.setText("\uD83D\uDCCBShow searched words\uD83D\uDCCB");
            row1button2.setCallbackData("Show searched words");
            row1.add(row1button2);
            rowList.add(row1);

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton row2button1 = new InlineKeyboardButton();
            InlineKeyboardButton row2button2 = new InlineKeyboardButton();
            row2button1.setText("⚜️Send advertisement⚜️");
            row2button1.setCallbackData("Send advertisement");
            row2.add(row2button1);
            row2button2.setText("\uD83D\uDDDESend news\uD83D\uDDDE");
            row2button2.setCallbackData("Send news");
            row2.add(row2button2);
            rowList.add(row2);

            List<InlineKeyboardButton> row3 = new ArrayList<>();
            InlineKeyboardButton row3button1 = new InlineKeyboardButton();
            row3button1.setText("\uD83D\uDCDDShow and answer to complaints or offers\uD83D\uDCDD");
            row3button1.setCallbackData("Show and answer to complaints or offers");
            row3.add(row3button1);
            rowList.add(row3);
        } else if (user.getBotState().equals(BotState.SHOW_USER_MENU)) {
            row1button1.setText("♻️Translate♻️");
            row1button1.setCallbackData("Translate");
            row1button2.setText("\uD83D\uDCCBShow searched words\uD83D\uDCCB");
            row1button2.setCallbackData("Show searched words");
            row1.add(row1button1);
            row1.add(row1button2);
            rowList.add(row1);

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton row2button1 = new InlineKeyboardButton();
            row2button1.setText("\uD83D\uDCDDSend complaint or offer\uD83D\uDCDD");
            row2button1.setCallbackData("Send complaint or offer");
            row2.add(row2button1);
            rowList.add(row2);
        } else if (user.getBotState().equals(BotState.TRANSLATE)) {
            row1button1.setText("\uD83C\uDDEC\uD83C\uDDE7English-\uD83C\uDDFA\uD83C\uDDFFUzbek");
            row1button1.setCallbackData("English-Uzbek");
            row1button2.setText("\uD83C\uDDFA\uD83C\uDDFFUzbek-\uD83C\uDDEC\uD83C\uDDE7English");
            row1button2.setCallbackData("Uzbek-English");
            row1.add(row1button1);
            row1.add(row1button2);
            rowList.add(row1);

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton row2button1 = new InlineKeyboardButton();
            InlineKeyboardButton row2button2 = new InlineKeyboardButton();
            row2button1.setText("\uD83C\uDDEA\uD83C\uDDF8Spain-\uD83C\uDDEC\uD83C\uDDE7English");
            row2button1.setCallbackData("Spain-English");
            row2button2.setText("\uD83C\uDDEC\uD83C\uDDE7English-\uD83C\uDDEA\uD83C\uDDF8Spain");
            row2button2.setCallbackData("English-Spain");
            row2.add(row2button1);
            row2.add(row2button2);
            rowList.add(row2);

            List<InlineKeyboardButton> row3 = new ArrayList<>();
            InlineKeyboardButton row3button1 = new InlineKeyboardButton();
            InlineKeyboardButton row3button2 = new InlineKeyboardButton();
            row3button1.setText("\uD83C\uDDFA\uD83C\uDDFFUzbek-\uD83C\uDDEA\uD83C\uDDF8Spain");
            row3button1.setCallbackData("Uzbek-Spain");
            row3button2.setText("\uD83C\uDDEA\uD83C\uDDF8Spain-\uD83C\uDDFA\uD83C\uDDFFUzbek");
            row3button2.setCallbackData("Spain-Uzbek");
            row3.add(row3button1);
            row3.add(row3button2);
            rowList.add(row3);

            List<InlineKeyboardButton> row4 = new ArrayList<>();
            InlineKeyboardButton row4button1 = new InlineKeyboardButton();
            row4button1.setText("\uD83D\uDD19Back to main menu\uD83D\uDD19");
            row4button1.setCallbackData("Back");
            row4.add(row4button1);
            rowList.add(row4);
        } else if (user.getBotState().equals(BotState.SEND_ADVERTISEMENT)) {
            row1button1.setText("\uD83D\uDD30Advert of PDP ACADEMY\uD83D\uDD30");
            row1button1.setCallbackData("Advert of PDP ACADEMY");
            row1.add(row1button1);
            rowList.add(row1);

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton row2button1 = new InlineKeyboardButton();
            row2button1.setText("\uD83D\uDD30Advert of IELTS ACADEMY\uD83D\uDD30");
            row2button1.setCallbackData("Advert of IELTS ACADEMY");
            row2.add(row2button1);
            rowList.add(row2);

            List<InlineKeyboardButton> row3 = new ArrayList<>();
            InlineKeyboardButton row3button1 = new InlineKeyboardButton();
            row3button1.setText("\uD83D\uDD30Advert of SAT ACADEMY\uD83D\uDD30");
            row3button1.setCallbackData("Advert of SAT ACADEMY");
            row3.add(row3button1);
            rowList.add(row3);

            List<InlineKeyboardButton> row4 = new ArrayList<>();
            InlineKeyboardButton row4button1 = new InlineKeyboardButton();
            row4button1.setText("*️⃣Create and send advertisement*️⃣");
            row4button1.setCallbackData("Create and send advertisement");
            row4.add(row4button1);
            rowList.add(row4);

            List<InlineKeyboardButton> row5 = new ArrayList<>();
            InlineKeyboardButton row5button1 = new InlineKeyboardButton();
            row5button1.setText("\uD83D\uDD19Back to main menu\uD83D\uDD19");
            row5button1.setCallbackData("Back");
            row5.add(row5button1);
            rowList.add(row5);
        } else if (user.getBotState().equals(BotState.SEND_NEWS)) {
            row1button1.setText("♨️BBC world news website♨️");
            row1button1.setCallbackData("BBC world news website");
            row1.add(row1button1);
            rowList.add(row1);

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton row2button1 = new InlineKeyboardButton();
            row2button1.setText("♨️KUN.UZ news website♨️");
            row2button1.setCallbackData("KUN.UZ news website");
            row2.add(row2button1);
            rowList.add(row2);

            List<InlineKeyboardButton> row3 = new ArrayList<>();
            InlineKeyboardButton row3button1 = new InlineKeyboardButton();
            row3button1.setText("♨️ESPN sport news website♨️");
            row3button1.setCallbackData("ESPN sport news website");
            row3.add(row3button1);
            rowList.add(row3);

            List<InlineKeyboardButton> row4 = new ArrayList<>();
            InlineKeyboardButton row4button1 = new InlineKeyboardButton();
            row4button1.setText("*️⃣Create and send news*️⃣");
            row4button1.setCallbackData("Create and send news");
            row4.add(row4button1);
            rowList.add(row4);

            List<InlineKeyboardButton> row5 = new ArrayList<>();
            InlineKeyboardButton row5button1 = new InlineKeyboardButton();
            row5button1.setText("\uD83D\uDD19Back to main menu\uD83D\uDD19");
            row5button1.setCallbackData("Back");
            row5.add(row5button1);
            rowList.add(row5);
        } else if (user.getBotState().equals(BotState.CREATE_AND_SEND_ADVERT_TEXT1)) {
            row1button1.setText("\uD83D\uDD19Back to main menu\uD83D\uDD19");
            row1button1.setCallbackData("Back");
            row1.add(row1button1);
            rowList.add(row1);
        } else if (user.getBotState().equals(BotState.ANSWERING_COMPLAINTS_OR_OFFERS) || user.getBotState().equals(BotState.OLDINGA) || user.getBotState().equals(BotState.ORTGA)) {
            InlineKeyboardButton row1button3 = new InlineKeyboardButton();
            InlineKeyboardButton row1button4 = new InlineKeyboardButton();
            InlineKeyboardButton row1button5 = new InlineKeyboardButton();
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton row2button1 = new InlineKeyboardButton();
            InlineKeyboardButton row2button2 = new InlineKeyboardButton();
            int a = Database.complaintsOrOffersList.size();
            if(a-(5*(page-1))==1) {
                if(a==1) {
                    int i = 0;
                    row1button1.setText("1");
                    row1button1.setCallbackData("AAAA:" + i);
                    row1.add(row1button1);
                    rowList.add(row1);
                }else if(a>5){
                    int i = 5*(page-1);
                    row1button1.setText("1");
                    row1button1.setCallbackData("AAAA:" + i);
                    row1.add(row1button1);
                    rowList.add(row1);
                    row2button1.setText("⬅️");
                    row2button1.setCallbackData("Ortga");
                    row2.add(row2button1);
                    rowList.add(row2);
                }
            }else if(a-(5*(page-1))==2) {
                if(a==2) {
                    int i = 0;
                    row1button1.setText("1");
                    row1button1.setCallbackData("AAAA:" + i);
                    row1.add(row1button1);
                    i++;
                    row1button2.setText("2");
                    row1button2.setCallbackData("AAAA:" + i);
                    row1.add(row1button2);
                    rowList.add(row1);
                }else if(a>5){
                    int i = 5*(page-1);
                    row1button1.setText("1");
                    row1button1.setCallbackData("AAAA:" + i);
                    row1.add(row1button1);
                    i++;
                    row1button2.setText("2");
                    row1button2.setCallbackData("AAAA:" + i);
                    row1.add(row1button2);
                    rowList.add(row1);
                    row2button1.setText("⬅️");
                    row2button1.setCallbackData("Ortga");
                    row2.add(row2button1);
                    rowList.add(row2);
                }
            }else if(a-(5*(page-1))==3) {
                if(a==3) {
                    int i = 0;
                    row1button1.setText("1");
                    row1button1.setCallbackData("AAAA:" + i);
                    row1.add(row1button1);
                    i++;
                    row1button2.setText("2");
                    row1button2.setCallbackData("AAAA:" + i);
                    row1.add(row1button2);
                    i++;
                    row1button3.setText("3");
                    row1button3.setCallbackData("AAAA:" + i);
                    row1.add(row1button3);
                    rowList.add(row1);
                }else if(a>5){
                    int i = 5*(page-1);
                    row1button1.setText("1");
                    row1button1.setCallbackData("AAAA:" + i);
                    row1.add(row1button1);
                    i++;
                    row1button2.setText("2");
                    row1button2.setCallbackData("AAAA:" + i);
                    row1.add(row1button2);
                    i++;
                    row1button3.setText("3");
                    row1button3.setCallbackData("AAAA:" + i);
                    row1.add(row1button3);
                    rowList.add(row1);
                    row2button1.setText("⬅️");
                    row2button1.setCallbackData("Ortga");
                    row2.add(row2button1);
                    rowList.add(row2);
                }
            }else if(a-(5*(page-1))==4) {
                if(a==4) {
                    int i = 0;
                    row1button1.setText("1");
                    row1button1.setCallbackData("AAAA:" + i);
                    row1.add(row1button1);
                    i++;
                    row1button2.setText("2");
                    row1button2.setCallbackData("AAAA:" + i);
                    row1.add(row1button2);
                    i++;
                    row1button3.setText("3");
                    row1button3.setCallbackData("AAAA:" + i);
                    row1.add(row1button3);
                    i++;
                    row1button4.setText("4");
                    row1button4.setCallbackData("AAAA:" + i);
                    row1.add(row1button4);
                    rowList.add(row1);
                }else if(a>5) {
                    int i = 5 * (page - 1);
                    row1button1.setText("1");
                    row1button1.setCallbackData("AAAA:" + i);
                    row1.add(row1button1);
                    i++;
                    row1button2.setText("2");
                    row1button2.setCallbackData("AAAA:" + i);
                    row1.add(row1button2);
                    i++;
                    row1button3.setText("3");
                    row1button3.setCallbackData("AAAA:" + i);
                    row1.add(row1button3);
                    i++;
                    row1button4.setText("4");
                    row1button4.setCallbackData("AAAA:" + i);
                    row1.add(row1button4);
                    rowList.add(row1);
                    row2button1.setText("⬅️");
                    row2button1.setCallbackData("Ortga");
                    row2.add(row2button1);
                    rowList.add(row2);
                }
            }else if(a==5) {
                int i = 0;
                    row1button1.setText("1");
                    row1button1.setCallbackData("AAAA:"+i);
                    row1.add(row1button1);
                    i++;
                    row1button2.setText("2");
                    row1button2.setCallbackData("AAAA:"+i);
                    row1.add(row1button2);
                    i++;
                    row1button3.setText("3");
                    row1button3.setCallbackData("AAAA:"+i);
                    row1.add(row1button3);
                    i++;
                    row1button4.setText("4");
                    row1button4.setCallbackData("AAAA:"+i);
                    row1.add(row1button4);
                    i++;
                    row1button5.setText("5");
                    row1button5.setCallbackData("AAAA:"+i);
                    row1.add(row1button5);
                    rowList.add(row1);
            }else if(a-5>=1 && page==1) {
                int i = 5*(page-1);
                row1button1.setText("1");
                row1button1.setCallbackData("AAAA:"+i);
                row1.add(row1button1);
                i++;
                row1button2.setText("2");
                row1button2.setCallbackData("AAAA:"+i);
                row1.add(row1button2);
                i++;
                row1button3.setText("3");
                row1button3.setCallbackData("AAAA:"+i);
                row1.add(row1button3);
                i++;
                row1button4.setText("4");
                row1button4.setCallbackData("AAAA:"+i);
                row1.add(row1button4);
                i++;
                row1button5.setText("5");
                row1button5.setCallbackData("AAAA:"+i);
                row1.add(row1button5);
                row2button2.setText("➡️");
                row2button2.setCallbackData("Oldinga");
                row2.add(row2button2);
                rowList.add(row1);
                rowList.add(row2);
            }else if(a-5>=1 && page>=2) {
                int i = 5*(page-1);
                row1button1.setText("1");
                row1button1.setCallbackData("AAAA:"+i);
                row1.add(row1button1);
                i++;
                row1button2.setText("2");
                row1button2.setCallbackData("AAAA:"+i);
                row1.add(row1button2);
                i++;
                row1button3.setText("3");
                row1button3.setCallbackData("AAAA:"+i);
                row1.add(row1button3);
                i++;
                row1button4.setText("4");
                row1button4.setCallbackData("AAAA:"+i);
                row1.add(row1button4);
                i++;
                row1button5.setText("5");
                row1button5.setCallbackData("AAAA:"+i);
                row1.add(row1button5);
                row2button1.setText("⬅️");
                row2button1.setCallbackData("Ortga");
                row2.add(row2button1);
                row2button2.setText("➡️");
                row2button2.setCallbackData("Oldinga");
                row2.add(row2button2);
                rowList.add(row1);
                rowList.add(row2);
            }
        }
        markup.setKeyboard(rowList);
        return markup;
    }

    //SHOW USER MENU method*******************************************
    public static SendPhoto showUserMenu(TgUser user) {
        user.setBotState(BotState.SHOW_USER_MENU);
        SendPhoto sendPhoto = new SendPhoto();
        File file = new File("src\\main\\resources\\logo.jpg");
        sendPhoto.setChatId(user.getChatId());
        InputFile inputFile = new InputFile(file);
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setCaption("\uD83D\uDDC2DICTIONARY BOT'S MENU\uD83D\uDDC2");
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup(user));
        saveUserChanges(user);
        return sendPhoto;
    }


    //SHOW ADMIN MENU method*******************************************
    public static SendPhoto showAdminMenu(TgUser user) {
        user.setBotState(BotState.SHOW_ADMIN_MENU);
        SendPhoto sendPhoto = new SendPhoto();
        File file = new File("src\\main\\resources\\logo.jpg");
        sendPhoto.setChatId(user.getChatId());
        InputFile inputFile = new InputFile(file);
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setCaption("\uD83D\uDDC2DICTIONARY BOT'S MENU\uD83D\uDDC2");
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup(user));
        saveUserChanges(user);
        return sendPhoto;
    }


    //SHOW USER LIST method****************************************
    public static SendDocument showUserList(TgUser tgUser) throws IOException {
        tgUser.setBotState(BotState.SHOW_USER_LIST);
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(tgUser.getChatId());
        XSSFWorkbook workbook = new XSSFWorkbook();
        File file = new File("E:\\PDP\\Java\\GITHUB_PROJECTS\\DictionaryBot\\Translate-Bot\\src\\main\\resources\\usersList.xlsx");
        FileOutputStream userListXlsx = new FileOutputStream("E:\\PDP\\Java\\GITHUB_PROJECTS\\DictionaryBot\\Translate-Bot\\src\\main\\resources\\usersList.xlsx");
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow row = sheet.createRow(0);
        XSSFCell cell = row.createCell(0);
        cell.setCellValue("Chat id");
        row.createCell(1).setCellValue("Name");
        row.createCell(2).setCellValue("Username");
        row.createCell(3).setCellValue("Phone number");
        for (int i = 0; i < Database.tgUserList.size(); i++) {
            XSSFRow row1 = sheet.createRow(i + 1);
            row1.createCell(0).setCellValue(Database.tgUserList.get(i).getChatId());
            row1.createCell(1).setCellValue(Database.tgUserList.get(i).getName());
            row1.createCell(2).setCellValue(Database.tgUserList.get(i).getUsername());
            row1.createCell(3).setCellValue(Database.tgUserList.get(i).getPhoneNumber());
        }
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 6000);
        sheet.setColumnWidth(3, 6000);
        workbook.write(userListXlsx);
        userListXlsx.close();
        workbook.close();
        InputFile inputFile = new InputFile(file);
        sendDocument.setDocument(inputFile);
        saveUserChanges(tgUser);
        return sendDocument;
    }


    //SHOW SEARCHED WORDS LIST method**************************************
    @SneakyThrows
    public static SendDocument showSearchedWordsList(TgUser tgUser) {
        tgUser.setBotState(BotState.SHOW_SEARCHED_WORDS_LIST);
        PdfWriter pdfWriter = new PdfWriter("src\\main\\resources\\searchedWords.pdf");
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        pdfDocument.addNewPage();
        Document document = new Document(pdfDocument);
        float[] width = {120F, 70F, 130F, 70F, 130F};
        Table table = new Table(width);
        table.addCell("User's phone number");
        table.addCell("From lang");
        table.addCell("Translating word");
        table.addCell("To lang");
        table.addCell("Translated word");
        if (!tgUser.isAdmin()) {
            for (int i = 0; i < Database.translationHistory.size(); i++) {
                if (Database.translationHistory.get(i).getUser().equals(tgUser)) {
                    table.addCell(Database.translationHistory.get(i).getUser().getPhoneNumber());
                    table.addCell(Database.translationHistory.get(i).getFromLang());
                    table.addCell(Database.translationHistory.get(i).getStr());
                    table.addCell(Database.translationHistory.get(i).getToLang());
                    table.addCell(Database.translationHistory.get(i).getTranslation());
                }
            }
        } else if (tgUser.isAdmin()) {
            for (int i = 0; i < Database.translationHistory.size(); i++) {

                table.addCell(Database.translationHistory.get(i).getUser().getPhoneNumber());
                table.addCell(Database.translationHistory.get(i).getFromLang());
                table.addCell(Database.translationHistory.get(i).getStr());
                table.addCell(Database.translationHistory.get(i).getToLang());
                table.addCell(Database.translationHistory.get(i).getTranslation());
            }
        }
        document.add(table);
        document.close();
        File file = new File("src\\main\\resources\\searchedWords.pdf");
        InputFile inputFile = new InputFile(file);
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(tgUser.getChatId());
        sendDocument.setDocument(inputFile);
        saveUserChanges(tgUser);
        return sendDocument;
    }


    //SEND ADVERTISEMENT method**********************************************
    public static SendMessage sendAdvertisement(TgUser tgUser) {
        tgUser.setBotState(BotState.SEND_ADVERTISEMENT);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("⚜️Choose Advertisement⚜️");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup(tgUser));
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //SEND NEWS method*************************************
    public static SendMessage sendNews(TgUser tgUser) {
        tgUser.setBotState(BotState.SEND_NEWS);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("\uD83C\uDF10Choose news websites\uD83C\uDF10");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup(tgUser));
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //SEND COMPLAINTS OR OFFERS method************************************
    public static SendMessage sendComplaintOrOffer(TgUser tgUser) {
        tgUser.setBotState(BotState.SEND_COMPLAINTS_OR_OFFERS);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("Do you have any complaints or offers...");
        sendMessage.setReplyMarkup(generateMarkup(tgUser));
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //DOING COMPLAINTS OR OFFERS method*********************************************
    public static SendMessage doingComplaintsOrOffers(TgUser tgUser) {
        tgUser.setBotState(BotState.DOING_COMPLAINTS_OR_OFFERS);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("You may write your complaints or offers here...");
        tgUser.setBotState(BotState.DOING_COMPLAINTS_OR_OFFERS1);
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //SHOW AND ANSWER TO COMPLAINTS OR OFFERS method******************************
    public static SendMessage showAndAnswerToComplaintsOrOffers(TgUser tgUser) {
        tgUser.setBotState(BotState.SHOW_AND_ANSWER_TO_COMPLAINTS_OR_OFFERS);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("Do you want show or answer to complaints or offers...");
        sendMessage.setReplyMarkup(generateMarkup(tgUser));
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //TRANSLATE method**********************************************
    public static SendPhoto translate(TgUser tgUser) {
        tgUser.setBotState(BotState.TRANSLATE);
        SendPhoto sendPhoto = new SendPhoto();
        File file = new File("src\\main\\resources\\logo.jpg");
        sendPhoto.setChatId(tgUser.getChatId());
        InputFile inputFile = new InputFile(file);
        sendPhoto.setPhoto(inputFile);
        sendPhoto.setCaption("\uD83D\uDDC2CHOOSE TRANSLATING LANGUAGES\uD83D\uDDC2");
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup(tgUser));
        saveUserChanges(tgUser);
        return sendPhoto;
    }


    //CREATE AND SEND NEWS method*************************************
    public static SendMessage createAndSendNews(TgUser tgUser) {
        tgUser.setBotState(BotState.CREATE_AND_SEND_NEWS1);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("Choose type of news ");
        sendMessage.setReplyMarkup(generateMarkup(tgUser));
        tgUser.setBotState(BotState.CREATE_AND_SEND_NEWS2);
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //CREATE AND SEND ADVERT method*********************************
    public static SendMessage createAndSendAdvert(TgUser tgUser) {
        tgUser.setBotState(BotState.CREATE_AND_SEND_ADVERT1);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("Choose type of advertisement ");
        sendMessage.setReplyMarkup(generateMarkup(tgUser));
        tgUser.setBotState(BotState.CREATE_AND_SEND_ADVERT2);
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //ADVERT WITH PHOTO method*******************************************
    public static SendPhoto advertPhoto(TgUser user) {
        user.setBotState(BotState.CREATE_AND_SEND_ADVERT_PHOTO);
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(user.getChatId());
        if (Database.tgUserList.size() > 1) {
            File file = new File("src/main/resources/enter.jpg");
            InputFile inputFile = new InputFile(file);
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption("\uD83C\uDF07Please, enter photo form of advert\uD83C\uDF07");
            user.setBotState(BotState.CREATE_AND_SEND_ADVERT_PHOTO1);
        } else {
            File file = new File("src/main/resources/empty.jpg");
            InputFile inputFile = new InputFile(file);
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption("Users list is empty now\uD83E\uDD37\uD83C\uDFFB\u200D♂️...");
            user.setBotState(BotState.SEND_ADVERTISEMENT);
        }
        saveUserChanges(user);
        return sendPhoto;
    }


    //NEWS WITH TEXT method*****************************************
    public static SendMessage newsText(TgUser user) {
        user.setBotState(BotState.CREATE_AND_SEND_NEWS_TEXT);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        if (Database.tgUserList.size() > 1) {
            sendMessage.setText("♨️Please enter text form of news♨️");
            user.setBotState(BotState.CREATE_AND_SEND_NEWS_TEXT1);
        } else {
            sendMessage.setText("Users list is empty now\uD83E\uDD37\uD83C\uDFFB\u200D♂️...");
            user.setBotState(BotState.SEND_NEWS);
        }
        saveUserChanges(user);
        return sendMessage;
    }


    //ADVERT WITH TEXT method**************************************
    public static SendMessage advertText(TgUser user) {
        user.setBotState(BotState.CREATE_AND_SEND_ADVERT_TEXT);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        if (Database.tgUserList.size() > 1) {
            sendMessage.setText("\uD83D\uDCDDPlease enter text form of advert\uD83D\uDCDD");
            user.setBotState(BotState.CREATE_AND_SEND_ADVERT_TEXT1);
        } else {
            sendMessage.setText("Users list is empty now\uD83E\uDD37\uD83C\uDFFB\u200D♂️...");
            user.setBotState(BotState.SEND_ADVERTISEMENT);
        }
        saveUserChanges(user);
        return sendMessage;
    }


    //ENGLISH UZBEK method****************************************
    public static SendMessage enUz(TgUser tgUser) {
        tgUser.setBotState(BotState.ENGLISH_UZBEK);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("✏️Please enter word...");
        tgUser.setBotState(BotState.ENGLISH_UZBEK1);
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //UZBEK ENGLISH method******************************************
    public static SendMessage uzEn(TgUser tgUser) {
        tgUser.setBotState(BotState.UZBEK_ENGLISH);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("✏️Please enter word...");
        tgUser.setBotState(BotState.UZBEK_ENGLISH1);
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //RUSSIAN ENGLISH method*****************************************
    public static SendMessage esEn(TgUser tgUser) {
        tgUser.setBotState(BotState.SPAIN_ENGLISH);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("✏️Please enter word...");
        tgUser.setBotState(BotState.SPAIN_ENGLISH1);
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //ENGLISH RUSSIAN method*********************************************
    public static SendMessage enEs(TgUser tgUser) {
        tgUser.setBotState(BotState.ENGLISH_SPAIN);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("✏️Please enter word...");
        tgUser.setBotState(BotState.ENGLISH_SPAIN1);
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //UZBEK RUSSIAN method**********************************************
    public static SendMessage uzEs(TgUser tgUser) {
        tgUser.setBotState(BotState.UZBEK_SPAIN);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("✏️Please enter word...");
        tgUser.setBotState(BotState.UZBEK_SPAIN1);
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //RUSSIAN UZBEK method********************************************
    public static SendMessage esUz(TgUser tgUser) {
        tgUser.setBotState(BotState.SPAIN_UZBEK);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("✏️Please enter word...");
        tgUser.setBotState(BotState.SPAIN_UZBEK1);
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //SEND ANSWER ONE method***********************************
    public static SendMessage sendAnswerOne(TgUser tgUser) {
        tgUser.setBotState(BotState.SEND_ANSWER_ONE);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("You may write your answers here...");
        tgUser.setBotState(BotState.SEND_ANSWER1);
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //SEND ANSWER 1 method************************************
    public static SendMessage sendAnswer1(TgUser user, Update update,int index){
        user.setBotState(BotState.SEND_ANSWER1);
        SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(Database.complaintsOrOffersList.get(index).getTgUser().getChatId());
            sendMessage.setText("❓Your question : \n" +
                    Database.complaintsOrOffersList.get(index).getText() + "\n" +
                    "❗️Our answer: \n" +
                    update.getMessage().getText());
            saveUserChanges(user);
        return sendMessage;
    }


    //DELETE 1 method***************************
    public static SendMessage deleteOne(TgUser tgUser,int index) {
        tgUser.setBotState(BotState.DELETE_ONE);
        Database.complaintsOrOffersList.remove(index);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("Deleted");
        saveUserChanges(tgUser);
        return sendMessage;
    }



    public static SendMessage one(TgUser tgUser) {
        tgUser.setBotState(BotState.ONE);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        sendMessage.setText("Choose command...");
        sendMessage.setReplyMarkup(generateMarkup(tgUser));
        saveUserChanges(tgUser);
        return sendMessage;
    }


    //OLDINGA method*******************************************
    public static SendMessage oldinga(TgUser tgUser, int page1) {
        page = page1;
        tgUser.setBotState(BotState.OLDINGA);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
        String str = "";
        if(Database.complaintsOrOffersList.size()>page*5){
            str+=((page - 1) * 5+1) + "-" + (page * 5) + " from " + Database.complaintsOrOffersList.size() + "\n\n";
        }else if(Database.complaintsOrOffersList.size()<page*5){
            str+=((page - 1) * 5+1) + "-" + Database.complaintsOrOffersList.size() + " from " + Database.complaintsOrOffersList.size() + "\n\n";
        }
            if (Database.complaintsOrOffersList.size()-(5*(page-1)) >= 5) {
                for (int i = 0; i < 5; i++) {
                    str += i + 1 + "-" +
                            "\uD83D\uDCDD"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getText() + "-->\n" +
                            "\uD83D\uDC64"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getTgUser().getName() + "-->\n" +
                            Database.complaintsOrOffersList.get((page - 1) * 5 + i).getLocalDateTime().format(DateTimeFormatter.ofPattern("\uD83D\uDCC6dd-MM-yyyy   ⏱hh:mm:ss a\n"));
                }
                sendMessage.setText(str);
                sendMessage.setReplyMarkup(BotService.inlineKeyboardMarkup(tgUser));
                BotService.saveUserChanges(tgUser);
            } else if (Database.complaintsOrOffersList.size()-(5*(page-1)) >= 1 &&  Database.complaintsOrOffersList.size()-(5*(page-1)) <= 4) {
                for (int i = 0; i < Database.complaintsOrOffersList.size()-(5*(page-1)); i++) {
                    str += i + 1 + "-" +
                            "\uD83D\uDCDD"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getText() + "-->\n" +
                            "\uD83D\uDC64"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getTgUser().getName() + "-->\n" +
                            Database.complaintsOrOffersList.get((page - 1) * 5 + i).getLocalDateTime().format(DateTimeFormatter.ofPattern("\uD83D\uDCC6dd-MM-yyyy   ⏱hh:mm:ss a\n"));
                }
                sendMessage.setText(str);
                sendMessage.setReplyMarkup(BotService.inlineKeyboardMarkup(tgUser));
                BotService.saveUserChanges(tgUser);
            }
        return sendMessage;
    }

    public static SendMessage ortga(TgUser tgUser, int page1) {
        page = page1;
        tgUser.setBotState(BotState.ORTGA);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(tgUser.getChatId());
            String str = "";
            if (Database.complaintsOrOffersList.size() > page * 5) {
                str += ((page - 1) * 5 + 1) + "-" + (page * 5) + " from " + Database.complaintsOrOffersList.size() + "\n\n";
            } else if (Database.complaintsOrOffersList.size() < page * 5) {
                str += ((page - 1) * 5 + 1) + "-" + Database.complaintsOrOffersList.size() + " from " + Database.complaintsOrOffersList.size() + "\n\n";
            }
            if (Database.complaintsOrOffersList.size() - (5 * (page - 1)) >= 5) {
                for (int i = 0; i < 5; i++) {
                    str += i + 1 + "-" +
                            "\uD83D\uDCDD"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getText() + "-->\n" +
                            "\uD83D\uDC64"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getTgUser().getName() + "-->\n" +
                            Database.complaintsOrOffersList.get((page - 1) * 5 + i).getLocalDateTime().format(DateTimeFormatter.ofPattern("\uD83D\uDCC6dd-MM-yyyy   ⏱hh:mm:ss a\n"));
                }
                sendMessage.setText(str);
                sendMessage.setReplyMarkup(BotService.inlineKeyboardMarkup(tgUser));
                BotService.saveUserChanges(tgUser);
            } else if (Database.complaintsOrOffersList.size() - (5 * (page - 1)) >= 1 && Database.complaintsOrOffersList.size() - (5 * (page - 1)) <= 4) {
                for (int i = 0; i < Database.complaintsOrOffersList.size() - (5 * (page - 1)); i++) {
                    str += i + 1 + "-" +
                            "\uD83D\uDCDD"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getText() + "-->\n" +
                            "\uD83D\uDC64"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getTgUser().getName() + "-->\n" +
                            Database.complaintsOrOffersList.get((page - 1) * 5 + i).getLocalDateTime().format(DateTimeFormatter.ofPattern("\uD83D\uDCC6dd-MM-yyyy   ⏱hh:mm:ss a\n"));
                }
                sendMessage.setText(str);
                sendMessage.setReplyMarkup(BotService.inlineKeyboardMarkup(tgUser));
                BotService.saveUserChanges(tgUser);
            }
            return sendMessage;
    }
}

