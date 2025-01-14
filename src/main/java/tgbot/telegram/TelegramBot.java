package tgbot.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tgbot.BotException;
import tgbot.processors.HttpRequest;
import tgbot.processors.Parser;
import tgbot.structs.Coordinates;
import tgbot.processors.Process;
import tgbot.structs.MessageContainer;
import java.util.HashMap;
import java.util.Map;

public class TelegramBot extends TelegramLongPollingBot {
    //�������� ������� ������, ����� ������� ������ ��������� ���� ���������� ������
    private final Map<String, Process> managerOfThreads = new HashMap<>();
    private final Map<String, String> userCities;
    private final Button button = new Button();
    private final HttpRequest httpRequest = new HttpRequest();
    private final Parser parser = new Parser();

    TelegramBot(Map<String, String> userCities) {
        this.userCities = userCities;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        String text = "";
        Coordinates userGeolocation = null;

        if (update.hasMessage() && update.getMessage().hasText()) {
            text = update.getMessage().getText();
        } else if (update.getMessage().hasLocation()) {
            Location location = update.getMessage().getLocation();
            userGeolocation = new Coordinates(location);
        }

        mainLogic(chatId, text, userGeolocation);
    }

    private void mainLogic(String chatId, String text, Coordinates userGeolocation) {
        if (!managerOfThreads.containsKey(chatId)) {
            managerOfThreads.put(String.valueOf(chatId), new Process(parser, httpRequest));
        }

        Process process = managerOfThreads.get(chatId);

        if (userCities.containsKey(chatId)) {
            process.getMapApiProcess().setCity(userCities.get(chatId));
        }

        MessageContainer messageData = process.processing(chatId, text, userGeolocation, getBotToken(), userCities);
        if (messageData != null) {
            sendMessage(messageData, process);
        }
    }

    public void sendMessage(MessageContainer messageData, Process process) {
        SendMessage message = new SendMessage(messageData.getChatId(), messageData.getData());

        if (process.getMapApiProcess().getButton()) {
            button.setUpGeolocation(message);
        }
        if (process.getMapApiProcess().getButtonDel() | process.getMapApiProcess().getDelLast()) {
            button.removeKeyboard(message);
        }
        if (process.getMapApiProcess().getRouteList()) {
            button.route(message);
        }
        if (process.getMapApiProcess().getPlaceList()) {
            button.place(message);
        }

        try {
            execute(message);
            if (messageData.isFlag() && process.getMapApiProcess().getMiddlePointOnMap()) {
                process.getMapApiProcess().coordinatesMapDisplay(getBotToken(), messageData.getChatId());
            }
        } catch (TelegramApiException | BotException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "maps_test_bot";
    }

    @Override
    public String getBotToken() {
        return System.getenv("TG_TOKEN");
    }
}