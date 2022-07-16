import Bot.MyBot;
import DataBase.Database;
import lombok.SneakyThrows;
import model.TgUser;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            try {
                telegramBotsApi.registerBot(new MyBot());
            } catch (TelegramApiRequestException e) {
                e.printStackTrace();
            }
        Database.tgUserList.add(new TgUser("2109886735","@INFINITYLlFE","Izzatbek Mahmudjonov","+998941102288",true));
        }

}
