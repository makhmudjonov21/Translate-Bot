package Exception;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Scanner;

public class MyException {
    SendMessage sendMessage = new SendMessage();
    public static Integer number2 () {
        SendMessage sendMessage = new SendMessage();
        Scanner scanner = new Scanner(System.in);
        try {
            Integer number2= scanner.nextInt();
            return number2;
        } catch (Exception e){
            sendMessage.setText("Wrong command...");
            return number2();
        }
    }
}
