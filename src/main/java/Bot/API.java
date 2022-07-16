package Bot;

import DataBase.Database;
import Language.Lang;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import model.TgUser;
import model.Translate;
import model.TranslationHistory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class API {
    public static TgUser user = new TgUser();
    @SneakyThrows
    public static void main(String[] args) {
        System.out.println(xx("Hello", user));
    }
    public static String xx(String str, TgUser user) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://deep-translate1.p.rapidapi.com/language/translate/v2"))
            .header("content-type", "application/json")
            .header("x-rapidapi-host", "deep-translate1.p.rapidapi.com")
            .header("x-rapidapi-key", "15c13345f3mshcb0244eb62475a9p12a7ecjsnf4d4895f0eea")
            .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n    " +
                    "\"q\":\""+str+"\",\r\n    " +
                    "\"source\":\""+ Lang.EN.getName1()+"\",\r\n    " +
                    "\"target\":\""+Lang.UZ.getName1()+"\"\r\n}"))
            .build();
    HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    Gson gson = new Gson();
    Translate translate = gson.fromJson(response.body(), Translate.class);
    Database.translationHistory.add(new TranslationHistory(user, str, translate.getData().getTranslations().getTranslatedText(), Lang.EN.getName1(), Lang.UZ.getName1()));
    return translate.getData().getTranslations().getTranslatedText();
}
}
