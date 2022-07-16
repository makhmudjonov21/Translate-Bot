package Bot;

import DataBase.Database;
import Language.Lang;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import model.ComplaintsOrOffers;
import model.TgUser;
import model.Translate;
import model.TranslationHistory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyBot extends TelegramLongPollingBot {
    int index = 0;
    int page = 1;

    @Override
    public String getBotUsername() {
        return " Yandex_Translator_Max_bot";
    }

    @Override
    public String getBotToken() {
        return "5085782104:AAGBg0DQhDjHlgdz6A7iltkVu6h1fQLtp6g";
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

            if (update.hasMessage()) {
                TgUser user = BotService.getOrCreateTgUser(BotService.getChatId(update), update);
                if (update.getMessage().hasContact()) {
                    if (user.getBotState().equals(BotState.SHARE_CONTACT)) {
                        execute(BotService.getContact(update.getMessage().getContact(), user));
                        deleteMessage(user);
                    }
                } else if ((update.getMessage().hasText())) {
                    String text = update.getMessage().getText();
                    System.out.println("text: " + text);
                    System.out.println("Phone number: " + user.getPhoneNumber());
                    System.out.println("Chat id: " + user.getChatId());
                    if (text.equals("/start")) {
                        execute(BotService.start(update, user));
                    }else if(text.equals("\uD83D\uDD8ASend answer")){
                        deleteMessage(user);
                        if(user.getBotState().equals(BotState.ONE)) {
                            user.setBotState(BotState.SEND_ANSWER_ONE);
                            BotService.saveUserChanges(user);
                            execute(BotService.sendAnswerOne(user));
                        }
                    }else if(text.equals("\uD83D\uDDD1Delete")){
                        deleteMessage(user);
                        if(user.getBotState().equals(BotState.ONE)) {
                            user.setBotState(BotState.DELETE_ONE);
                            BotService.saveUserChanges(user);
                            execute(BotService.deleteOne(user,index));
                            execute(BotService.showAdminMenu(user));
                        }
                    }
                    else {
                        if (user.getBotState().equals(BotState.SHARE_CONTACT)) {
                            execute(BotService.shareContact(update));
                        } else if (user.getBotState().equals(BotState.ENTER_CODE)) {
                            if (text.equals("\uD83D\uDCE9Resend code\uD83D\uDCE9")) {
                                user.setBotState(BotState.RESEND_CODE);
                                BotService.saveUserChanges(user);
                                execute(BotService.resendCode(update, user));
                            } else {
                                boolean verifiedCode = BotService.getVerifiedCode(update, user);
                                if (verifiedCode) {
                                    if (user.isAdmin()) {
                                        execute(BotService.showAdminMenu((BotService.getOrCreateTgUser(user.getChatId(), update))));
                                    } else {
                                        execute(BotService.showUserMenu((BotService.getOrCreateTgUser(user.getChatId(), update))));
                                    }
                                } else {
                                    SendMessage sendMessage = new SendMessage();
                                    sendMessage.setChatId(user.getChatId());
                                    sendMessage.setText("❌Error code. Please, enter correct code.");
                                    execute(sendMessage);
                                }
                            }
                        } else if (user.getBotState().equals(BotState.CREATE_AND_SEND_ADVERT2)) {
                            if (text.equals("\uD83D\uDCDDAdvert with text\uD83D\uDCDD")) {
                                user.setBotState(BotState.CREATE_AND_SEND_ADVERT_TEXT);
                                BotService.saveUserChanges(user);
                                execute(BotService.advertText(user));
                                if (user.getBotState().equals(BotState.SEND_ADVERTISEMENT)) {
                                    execute(BotService.sendAdvertisement(user));
                                }
                                deleteMessage(user);
                            } else if (text.equals("\uD83C\uDF07Advert with photo\uD83C\uDF07")) {
                                user.setBotState(BotState.CREATE_AND_SEND_ADVERT_PHOTO);
                                BotService.saveUserChanges(user);
                                execute(BotService.advertPhoto(user));
                                if (user.getBotState().equals(BotState.SEND_ADVERTISEMENT)) {
                                    execute(BotService.sendAdvertisement(user));
                                }
                                deleteMessage(user);
                            }
                        } else if (user.getBotState().equals(BotState.CREATE_AND_SEND_NEWS2)) {
                            if (text.equals("♨️News with text♨️")) {
                                user.setBotState(BotState.CREATE_AND_SEND_NEWS_TEXT);
                                BotService.saveUserChanges(user);
                                execute(BotService.newsText(user));
                                if (user.getBotState().equals(BotState.SEND_NEWS)) {
                                    execute(BotService.sendNews(user));
                                }
                                deleteMessage(user);
                            }
                        } else if (user.getBotState().equals(BotState.CREATE_AND_SEND_ADVERT_PHOTO1)) {
                            PhotoSize photoSize = update.getMessage().getPhoto().get(0);
                            String fileId = photoSize.getFileId();
                            for (TgUser tgUser : Database.tgUserList) {
                                SendPhoto sendPhoto = new SendPhoto();
                                sendPhoto.setPhoto(new InputFile(fileId));
                                sendPhoto.setChatId(tgUser.getChatId());
                                sendPhoto.setCaption("Assalomu aleykum");
                                execute(sendPhoto);
                            }
                            execute(BotService.sendAdvertisement(user));
                        } else if (user.getBotState().equals(BotState.CREATE_AND_SEND_ADVERT_TEXT1)) {
                            SendMessage sendMessage = new SendMessage();
                            for (TgUser tgUser : Database.tgUserList) {
                                sendMessage.setChatId(tgUser.getChatId());
                                sendMessage.setText(update.getMessage().getText());
                                BotService.saveUserChanges(user);
                                execute(sendMessage);
                            }
                            execute(BotService.sendAdvertisement(user));
                        } else if (user.getBotState().equals(BotState.CREATE_AND_SEND_NEWS_TEXT1)) {
                            SendMessage sendMessage = new SendMessage();
                            for (TgUser tgUser : Database.tgUserList) {
                                sendMessage.setChatId(tgUser.getChatId());
                                sendMessage.setText(update.getMessage().getText());
                                BotService.saveUserChanges(user);
                                execute(sendMessage);
                            }
                            execute(BotService.sendNews(user));
                        } else if (user.getBotState().equals(BotState.SEND_COMPLAINTS_OR_OFFERS)) {
                            deleteMessage(user);
                            if (text.equals("✅Yes✅")) {
                                user.setBotState(BotState.DOING_COMPLAINTS_OR_OFFERS);
                                execute(BotService.doingComplaintsOrOffers(user));
                            } else if (text.equals("❌No❌") || text.equals("\uD83D\uDD19Back to main menu\uD83D\uDD19")) {
                                BotService.saveUserChanges(user);
                                execute(BotService.showUserMenu(user));
                            }
                        } else if (user.getBotState().equals(BotState.DOING_COMPLAINTS_OR_OFFERS1)) {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(user.getChatId());
                            Database.complaintsOrOffersList.add(new ComplaintsOrOffers(update.getMessage().getText(), LocalDateTime.now(), user));
                            user.setBotState(BotState.SHOW_USER_MENU);
                            execute(BotService.showUserMenu(user));
                            BotService.saveUserChanges(user);
                        } else if (user.getBotState().equals(BotState.SHOW_AND_ANSWER_TO_COMPLAINTS_OR_OFFERS)) {
                            deleteMessage(user);
                            if (text.equals("✅Yes✅")) {
                                user.setBotState(BotState.ANSWERING_COMPLAINTS_OR_OFFERS);
                                SendMessage sendMessage = new SendMessage();
                                sendMessage.setChatId(user.getChatId());
                                if (!Database.complaintsOrOffersList.isEmpty()) {
                                    int page = 1;
                                    String str = "";
                                    if(Database.complaintsOrOffersList.size()>page*5){
                                        str+=((page - 1) * 5+1) + "-" + (page * 5) + " from " + Database.complaintsOrOffersList.size() + "\n\n";
                                    }else if(Database.complaintsOrOffersList.size()<page*5){
                                        str+=((page - 1) * 5+1) + "-" + Database.complaintsOrOffersList.size() + " from " + Database.complaintsOrOffersList.size() + "\n\n";
                                    }
                                        if (Database.complaintsOrOffersList.size() >= 5) {
                                            for (int i = 0; i < 5; i++) {
                                                str += i + 1 + "-" +
                                                        "\uD83D\uDCDD"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getText() + "-->\n" +
                                                        "\uD83D\uDC64"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getTgUser().getName() + "-->\n" +
                                                        Database.complaintsOrOffersList.get((page - 1) * 5 + i).getLocalDateTime().format(DateTimeFormatter.ofPattern("\uD83D\uDCC6dd-MM-yyyy   ⏱hh:mm:ss a\n"));
                                            }
                                            sendMessage.setText(str);
                                            sendMessage.setReplyMarkup(BotService.inlineKeyboardMarkup(user));
                                            BotService.saveUserChanges(user);
                                            execute(sendMessage);
                                            page++;
                                        } else if (Database.complaintsOrOffersList.size() >= 1 && Database.complaintsOrOffersList.size() <= 4) {
                                            for (int i = 0; i < Database.complaintsOrOffersList.size(); i++) {
                                                str += i + 1 + "-" +
                                                        "\uD83D\uDCDD"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getText() + "-->\n" +
                                                        "\uD83D\uDC64"+Database.complaintsOrOffersList.get((page - 1) * 5 + i).getTgUser().getName() + "-->\n" +
                                                        Database.complaintsOrOffersList.get((page - 1) * 5 + i).getLocalDateTime().format(DateTimeFormatter.ofPattern("\uD83D\uDCC6dd-MM-yyyy   ⏱hh:mm:ss a\n"));
                                            }
                                            sendMessage.setText(str);
                                            sendMessage.setReplyMarkup(BotService.inlineKeyboardMarkup(user));
                                            BotService.saveUserChanges(user);
                                            execute(sendMessage);
                                            page++;
                                        }
                                } else {
                                    sendMessage.setText("Complaints and offers list is empty...");
                                    user.setBotState(BotState.SHOW_ADMIN_MENU);
                                    BotService.saveUserChanges(user);
                                    execute(sendMessage);
                                }
                            }
                            else if (text.equals("❌No❌") || text.equals("\uD83D\uDD19Back to main menu\uD83D\uDD19")) {
                                BotService.saveUserChanges(user);
                                if (user.isAdmin()) {
                                    execute(BotService.showAdminMenu(user));
                                } else {
                                    execute(BotService.showUserMenu(user));
                                }
                            }
                        }
                            else if(user.getBotState().equals(BotState.ENGLISH_UZBEK1)){
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(user.getChatId());
                            sendMessage.setText(enUz(text, user));
                            BotService.saveUserChanges(user);
                            execute(sendMessage);
                            execute(BotService.showUserMenu(user));
                        }else if(user.getBotState().equals(BotState.UZBEK_ENGLISH1)) {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(user.getChatId());
                            sendMessage.setText(uzEn(text, user));
                            BotService.saveUserChanges(user);
                            execute(sendMessage);
                            execute(BotService.showUserMenu(user));
                        }else if(user.getBotState().equals(BotState.ENGLISH_SPAIN1)){
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(user.getChatId());
                            sendMessage.setText(enEs(text, user));
                            BotService.saveUserChanges(user);
                            execute(sendMessage);
                            execute(BotService.showUserMenu(user));
                        }else if(user.getBotState().equals(BotState.SPAIN_ENGLISH1)){
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(user.getChatId());
                            sendMessage.setText(esEn(text, user));
                            BotService.saveUserChanges(user);
                            execute(sendMessage);
                            execute(BotService.showUserMenu(user));
                        }else if(user.getBotState().equals(BotState.UZBEK_SPAIN1)){
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(user.getChatId());
                            sendMessage.setText(uzEs(text, user));
                            BotService.saveUserChanges(user);
                            execute(sendMessage);
                            execute(BotService.showUserMenu(user));
                        }else if(user.getBotState().equals(BotState.SPAIN_UZBEK1)){
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(user.getChatId());
                            sendMessage.setText(esUz(text, user));
                            BotService.saveUserChanges(user);
                            execute(sendMessage);
                            execute(BotService.showUserMenu(user));
                        }else if(user.getBotState().equals(BotState.SEND_ANSWER1)){
                            BotService.saveUserChanges(user);
                            execute(BotService.sendAnswer1(user, update,index));
                            execute(BotService.showAdminMenu(user));
                        }
                    }
                }
            } else if (update.hasCallbackQuery()) {
                TgUser tgUser = BotService.getOrCreateTgUser(BotService.getChatId(update), update);
                String data = update.getCallbackQuery().getData();

                if (data.equals("Show users list")) {
                    tgUser.setBotState(BotState.SHOW_USER_LIST);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.showUserList(tgUser));
                    execute(BotService.showAdminMenu(tgUser));
                } else if (data.equals("Show searched words")) {
                    tgUser.setBotState(BotState.SHOW_SEARCHED_WORDS_LIST);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.showSearchedWordsList(tgUser));
                    if(tgUser.isAdmin()) {
                        execute(BotService.showAdminMenu(tgUser));
                    }else {
                        execute(BotService.showUserMenu(tgUser));
                    }
                } else if (data.equals("Send advertisement")) {
                    tgUser.setBotState(BotState.SEND_ADVERTISEMENT);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.sendAdvertisement(tgUser));
                } else if (data.equals("Send news")) {
                    tgUser.setBotState(BotState.SEND_NEWS);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.sendNews(tgUser));
                } else if (data.equals("Show and answer to complaints or offers")) {
                    tgUser.setBotState(BotState.SHOW_AND_ANSWER_TO_COMPLAINTS_OR_OFFERS);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.showAndAnswerToComplaintsOrOffers(tgUser));
                } else if (data.equals("Translate")) {
                    tgUser.setBotState(BotState.TRANSLATE);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.translate(tgUser));
                } else if (data.equals("Send complaint or offer")) {
                    tgUser.setBotState(BotState.SEND_COMPLAINTS_OR_OFFERS);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.sendComplaintOrOffer(tgUser));
                } else if (data.equals("English-Uzbek")) {
                    tgUser.setBotState(BotState.ENGLISH_UZBEK);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.enUz(tgUser));
                } else if (data.equals("Uzbek-English")) {
                    tgUser.setBotState(BotState.UZBEK_ENGLISH);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.uzEn(tgUser));
                } else if (data.equals("Spain-English")) {
                    tgUser.setBotState(BotState.SPAIN_ENGLISH);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.esEn(tgUser));
                } else if (data.equals("English-Spain")) {
                    tgUser.setBotState(BotState.ENGLISH_SPAIN);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.enEs(tgUser));
                } else if (data.equals("Uzbek-Spain")) {
                    tgUser.setBotState(BotState.UZBEK_SPAIN);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.uzEs(tgUser));
                } else if (data.equals("Spain-Uzbek")) {
                    tgUser.setBotState(BotState.SPAIN_UZBEK);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.esUz(tgUser));
                } else if (data.equals("Back")) {
                    if (tgUser.isAdmin()) {
                        tgUser.setBotState(BotState.SHOW_ADMIN_MENU);
                        BotService.saveUserChanges(tgUser);
                        execute(BotService.showAdminMenu(tgUser));
                    } else {
                        tgUser.setBotState(BotState.SHOW_USER_MENU);
                        BotService.saveUserChanges(tgUser);
                        execute(BotService.showUserMenu(tgUser));
                    }
                } else if (data.equals("Advert of PDP ACADEMY")) {
                    tgUser.setBotState(BotState.ADVERT_PDP);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(tgUser.getChatId());
                    if (Database.tgUserList.size() > 1) {
                        for (TgUser user : Database.tgUserList) {
                            sendMessage.setChatId(user.getChatId());
                            sendMessage.setParseMode("Markdown");
                            sendMessage.setText("\uD83D\uDD30Dasturlash uchun [PDP ACADEMY](https://t.me/pdpuz) ni tanlang!");
                            BotService.saveUserChanges(tgUser);
                            execute(sendMessage);
                        }
                    } else {
                        sendMessage.setText("Users list is empty now\uD83E\uDD37\uD83C\uDFFB\u200D♂️...");
                        execute(sendMessage);
                    }
                    execute(BotService.showAdminMenu(tgUser));
                } else if (data.equals("Advert of IELTS ACADEMY")) {
                    tgUser.setBotState(BotState.ADVERT_IELTS);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(tgUser.getChatId());
                    if (Database.tgUserList.size() > 1) {
                        for (TgUser user : Database.tgUserList) {
                            sendMessage.setChatId(user.getChatId());
                            sendMessage.setParseMode("Markdown");
                            sendMessage.setText("\uD83D\uDD30IELTS uchun [IELTS ACADEMY](https://t.me/ielts_monster) ni tanlang!");
                            BotService.saveUserChanges(tgUser);
                            execute(sendMessage);
                        }
                    } else {
                        sendMessage.setText("Users list is empty now\uD83E\uDD37\uD83C\uDFFB\u200D♂️...");
                        execute(sendMessage);
                    }
                    execute(BotService.showAdminMenu(tgUser));
                } else if (data.equals("Advert of SAT ACADEMY")) {
                    tgUser.setBotState(BotState.ADVERT_SAT);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(tgUser.getChatId());
                    if (Database.tgUserList.size() > 1) {
                        for (TgUser user : Database.tgUserList) {
                            sendMessage.setChatId(user.getChatId());
                            sendMessage.setParseMode("Markdown");
                            sendMessage.setText("\uD83D\uDD30SAT uchun [SAT ACADEMY](https://t.me/sat_teacher) ni tanlang!");
                            BotService.saveUserChanges(tgUser);
                            execute(sendMessage);
                        }
                    } else {
                        sendMessage.setText("Users list is empty now\uD83E\uDD37\uD83C\uDFFB\u200D♂️...");
                        execute(sendMessage);
                    }
                    execute(BotService.showAdminMenu(tgUser));
                } else if (data.equals("Create and send advertisement")) {
                    tgUser.setBotState(BotState.CREATE_AND_SEND_ADVERT1);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.createAndSendAdvert(tgUser));
                } else if (data.equals("BBC world news website")) {
                    tgUser.setBotState(BotState.BBC_NEWS);
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(tgUser.getChatId());
                    if (Database.tgUserList.size() > 1) {
                        for (TgUser user : Database.tgUserList) {
                            sendPhoto.setChatId(user.getChatId());
                            File file = new File("src/main/resources/bbc.jpg");
                            InputFile inputFile = new InputFile(file);
                            sendPhoto.setPhoto(inputFile);
                            sendPhoto.setParseMode("Markdown");
                            sendPhoto.setCaption("♨️World's least [BBC.com](https://www.google.com/search?q=bbc+news+world&sxsrf=AOaemvJ6tKfUlQrfuph8TY1d5egOQqd6Vg:1639801372052&source=lnms&tbm=nws&sa=X&ved=2ahUKEwiQrqGSwOz0AhUCyosKHQ5XAfEQ_AUoAXoECAIQAw&biw=1366&bih=625&dpr=1)" +
                                    " news. The best website to aware world's essential news");
                            BotService.saveUserChanges(tgUser);
                            execute(sendPhoto);
                        }
                    } else {
                        File file = new File("src/main/resources/empty.jpg");
                        InputFile inputFile = new InputFile(file);
                        sendPhoto.setPhoto(inputFile);
                        sendPhoto.setCaption("Users list is empty now\uD83E\uDD37\uD83C\uDFFB\u200D♂️...");
                        execute(sendPhoto);
                    }
                    execute(BotService.showAdminMenu(tgUser));
                } else if (data.equals("KUN.UZ news website")) {
                    tgUser.setBotState(BotState.KUN_UZ_NEWS);
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(tgUser.getChatId());
                    if (Database.tgUserList.size() > 1) {
                        for (TgUser user : Database.tgUserList) {
                            sendPhoto.setChatId(user.getChatId());
                            File file = new File("src/main/resources/kunUz.jpg");
                            InputFile inputFile = new InputFile(file);
                            sendPhoto.setPhoto(inputFile);
                            sendPhoto.setParseMode("Markdown");
                            sendPhoto.setCaption("♨️Uzbekistan's least [KUN.uz](https://www.google.com/search?q=kun+uz+yangiliklari&sxsrf=AOaemvLOyakDlC1Lt6z4K1DH4MhZF-iIxA:1639804344432&source=lnms&tbm=nws&sa=X&ved=2ahUKEwjbnc2by-z0AhVDxIsKHQt6BZkQ_AUoAnoECAIQBA&biw=1366&bih=625&dpr=1)" +
                                    " news. The most popular recent news website in Uzbekistan.");
                            BotService.saveUserChanges(tgUser);
                            execute(sendPhoto);
                        }
                    } else {
                        File file = new File("src/main/resources/empty.jpg");
                        InputFile inputFile = new InputFile(file);
                        sendPhoto.setPhoto(inputFile);
                        sendPhoto.setCaption("Users list is empty now\uD83E\uDD37\uD83C\uDFFB\u200D♂️...");
                        execute(sendPhoto);
                    }
                    execute(BotService.showAdminMenu(tgUser));
                } else if (data.equals("ESPN sport news website")) {
                    tgUser.setBotState(BotState.ESPN_SPORT_NEWS);
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(tgUser.getChatId());
                    if (Database.tgUserList.size() > 1) {
                        for (TgUser user : Database.tgUserList) {
                            sendPhoto.setChatId(user.getChatId());
                            File file = new File("src/main/resources/ESPN.png");
                            InputFile inputFile = new InputFile(file);
                            sendPhoto.setPhoto(inputFile);
                            sendPhoto.setParseMode("Markdown");
                            sendPhoto.setCaption("♨️World sport's least [ESPN.com](https://www.google.com/search?q=espn+sports&hl=ru&sxsrf=AOaemvJlX_DOfq_vSHzeyXH4nnrO7e20Pw:1639805045781&source=lnms&tbm=nws&sa=X&ved=2ahUKEwjkmYTqzez0AhUPyYsKHch2D24Q_AUoAnoECAIQBA&biw=1366&bih=625&dpr=1)" +
                                    " news. World's recent sport news here.");
                            BotService.saveUserChanges(tgUser);
                            execute(sendPhoto);
                        }
                    } else {
                        File file = new File("src/main/resources/empty.jpg");
                        InputFile inputFile = new InputFile(file);
                        sendPhoto.setPhoto(inputFile);
                        sendPhoto.setCaption("Users list is empty now\uD83E\uDD37\uD83C\uDFFB\u200D♂️...");
                        execute(sendPhoto);
                    }
                    execute(BotService.showAdminMenu(tgUser));
                } else if (data.equals("Create and send news")) {
                    tgUser.setBotState(BotState.CREATE_AND_SEND_NEWS1);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.createAndSendNews(tgUser));
                } else if(data.startsWith("AAAA:")){
                    index = Integer.parseInt(data.substring(data.indexOf(":")+1));
                    tgUser.setBotState(BotState.ONE);
                    BotService.saveUserChanges(tgUser);
                    execute(BotService.one(tgUser));
                } else if(data.equals("Oldinga")){
                    tgUser.setBotState(BotState.OLDINGA);
                    BotService.saveUserChanges(tgUser);
                    page++;
                    execute(BotService.oldinga(tgUser, page));
                } else if(data.equals("Ortga")){
                    tgUser.setBotState(BotState.ORTGA);
                    BotService.saveUserChanges(tgUser);
                    if(page>=2) {
                        page--;
                    }
                    execute(BotService.ortga(tgUser, page));
                }
            }
    }


    @SneakyThrows
    public void deleteMessage(TgUser user){
        SendMessage sendMessageRemove = new SendMessage();
        sendMessageRemove.setChatId(user.getChatId());
        sendMessageRemove.setText(".");
        sendMessageRemove.setReplyMarkup(new ReplyKeyboardRemove(true));
        Message message = execute(sendMessageRemove);
        DeleteMessage deleteMessage = new DeleteMessage(user.getChatId(), message.getMessageId());
        execute(deleteMessage);
    }

    public static String enUz(String str, TgUser user) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://deep-translate1.p.rapidapi.com/language/translate/v2"))
                .header("content-type", "application/json")
                .header("x-rapidapi-host", "deep-translate1.p.rapidapi.com")
                .header("x-rapidapi-key", "15c13345f3mshcb0244eb62475a9p12a7ecjsnf4d4895f0eea")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n    " +
                        "\"q\":\""+str+"\",\r\n    " +
                        "\"source\":\""+Lang.EN.getName1()+"\",\r\n    " +
                        "\"target\":\""+Lang.UZ.getName1()+"\"\r\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        Translate translate = gson.fromJson(response.body(), Translate.class);
        Database.translationHistory.add(new TranslationHistory(user, str, translate.getData().getTranslations().getTranslatedText(), Lang.EN.getName1(), Lang.UZ.getName1()));
        return translate.getData().getTranslations().getTranslatedText();
    }

    public static String uzEn(String str, TgUser user) throws IOException, InterruptedException {
HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://deep-translate1.p.rapidapi.com/language/translate/v2"))
        .header("content-type", "application/json")
        .header("x-rapidapi-host", "deep-translate1.p.rapidapi.com")
        .header("x-rapidapi-key", "15c13345f3mshcb0244eb62475a9p12a7ecjsnf4d4895f0eea")
        .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n    " +
                "\"q\":\""+str+"\",\r\n    " +
                "\"source\":\""+Lang.UZ.getName1()+"\",\r\n    " +
                "\"target\":\""+Lang.EN.getName1()+"\"\r\n}"))
        .build();
    HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    Gson gson = new Gson();
    Translate translate = gson.fromJson(response.body(), Translate.class);
        Database.translationHistory.add(new TranslationHistory(user, str, translate.getData().getTranslations().getTranslatedText(), Lang.UZ.getName1(), Lang.EN.getName1()));
       return translate.getData().getTranslations().getTranslatedText();
    }

    public static String esEn(String str, TgUser user) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://deep-translate1.p.rapidapi.com/language/translate/v2"))
                .header("content-type", "application/json")
                .header("x-rapidapi-host", "deep-translate1.p.rapidapi.com")
                .header("x-rapidapi-key", "15c13345f3mshcb0244eb62475a9p12a7ecjsnf4d4895f0eea")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n    " +
                        "\"q\":\""+str+"\",\r\n    " +
                        "\"source\":\""+Lang.ES.getName1()+"\",\r\n    " +
                        "\"target\":\""+Lang.EN.getName1()+"\"\r\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        Translate translate = gson.fromJson(response.body(), Translate.class);
        Database.translationHistory.add(new TranslationHistory(user, str, translate.getData().getTranslations().getTranslatedText(),Lang.ES.getName1(), Lang.EN.getName1()));
        return translate.getData().getTranslations().getTranslatedText();
    }

    public static String enEs(String str, TgUser user) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://deep-translate1.p.rapidapi.com/language/translate/v2"))
                .header("content-type", "application/json")
                .header("x-rapidapi-host", "deep-translate1.p.rapidapi.com")
                .header("x-rapidapi-key", "15c13345f3mshcb0244eb62475a9p12a7ecjsnf4d4895f0eea")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n    " +
                        "\"q\":\""+str+"\",\r\n    " +
                        "\"source\":\""+Lang.EN.getName1()+"\",\r\n    " +
                        "\"target\":\""+Lang.ES.getName1()+"\"\r\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        Translate translate = gson.fromJson(response.body(), Translate.class);
        Database.translationHistory.add(new TranslationHistory(user, str, translate.getData().getTranslations().getTranslatedText(), Lang.EN.getName1(), Lang.ES.getName1()));
        return translate.getData().getTranslations().getTranslatedText();
    }

    public static String uzEs(String str, TgUser user) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://deep-translate1.p.rapidapi.com/language/translate/v2"))
                .header("content-type", "application/json")
                .header("x-rapidapi-host", "deep-translate1.p.rapidapi.com")
                .header("x-rapidapi-key", "15c13345f3mshcb0244eb62475a9p12a7ecjsnf4d4895f0eea")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n    " +
                        "\"q\":\""+str+"\",\r\n    " +
                        "\"source\":\""+Lang.UZ.getName1()+"\",\r\n    " +
                        "\"target\":\""+Lang.ES.getName1()+"\"\r\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        Translate translate = gson.fromJson(response.body(), Translate.class);
        Database.translationHistory.add(new TranslationHistory(user, str, translate.getData().getTranslations().getTranslatedText(), Lang.UZ.getName1(), Lang.ES.getName1()));
        return translate.getData().getTranslations().getTranslatedText();
    }

    public static String esUz(String str, TgUser user) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://deep-translate1.p.rapidapi.com/language/translate/v2"))
                .header("content-type", "application/json")
                .header("x-rapidapi-host", "deep-translate1.p.rapidapi.com")
                .header("x-rapidapi-key", "15c13345f3mshcb0244eb62475a9p12a7ecjsnf4d4895f0eea")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n    " +
                        "\"q\":\""+str+"\",\r\n    " +
                        "\"source\":\""+Lang.ES.getName1()+"\",\r\n    " +
                        "\"target\":\""+Lang.UZ.getName1()+"\"\r\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        Translate translate = gson.fromJson(response.body(), Translate.class);
        Database.translationHistory.add(new TranslationHistory(user, str, translate.getData().getTranslations().getTranslatedText(), Lang.ES.getName1(), Lang.UZ.getName1()));
        return translate.getData().getTranslations().getTranslatedText();
    }
}
