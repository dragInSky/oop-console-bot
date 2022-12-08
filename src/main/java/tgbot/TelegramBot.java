package tgbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;

public class TelegramBot extends TelegramLongPollingBot {
    private final Button button = new Button();
    private final MapApiProcess mapApiProcess = new MapApiProcess();
    private String command;
    private boolean cityCommand = false;

    @Override
    public void onUpdateReceived(Update update) {
        //System.out.println("Starting: " + update.getMessage().getChatId());
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            if (cityCommand) {
                cityCommand = false;
                mapApiProcess.setCity(text + ", ");
                sendMessage(update.getMessage(), "�� �������� ����� �� " + text + "\n/help - ������ ���� ������");
            } else if (mapApiProcess.getRepeatCommand()) {
                commandProcess(update.getMessage(), command, text);
            } else {
                command = text;
                commandProcess(update.getMessage(), text, "");
            }
        } else { //������ ������ ������
            if (update.getMessage().hasLocation()) { //���� ���� ��������� ���������
                Location location = update.getMessage().getLocation();
                Coordinates userGeolocation = new Coordinates(location);
                routeProcess(update.getMessage(), userGeolocation);
            }
        }
//        try {
//            Thread.sleep(10_000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println("Finishing: " + update.getMessage().getChatId());
    }

    private void commandProcess(Message msg, String message, String addr) {
        if (!Objects.equals(addr, "")) {
            addr = mapApiProcess.getCity() + addr;
        }
        switch (message) {
            case "/start" -> startCommandProcess(msg);
            case "/help" ->  sendMessage(msg,
                    """
                    ������ ���� ������:
                    /changecity - �������� �����
                    /map - ������� �� ����� ����� �� ������
                    /info - ������� �� ������ ���������� �� ������������
                    /route - ����� ����� ������� ��� ���� �����
                    """);
            case "/changecity" -> changeCityProcess(msg);
            case "/map" -> mapDisplayProcess(msg, msg.getChatId().toString(), addr);
            case "/info" -> addrInfoProcess(msg, addr);
            case "/route" -> routeProcess(msg, addr);
            default ->  sendMessage(msg,"�������\\��������� �������!");
        }
    }

    private void startCommandProcess(Message msg) {
        sendMessage(msg, """
                ��� ������������ 2gis ���, � ����:
                - �������� ����� ������� ��� ���� �����;
                - �������� �� ����� ����� �� ������;
                - �������� �� ������ ���������� �� ������������.
                """);
        changeCityProcess(msg);
    }

    private void changeCityProcess(Message msg) {
        sendMessage(msg, "������� �����, � ������� �� ����������");
        cityCommand = true;
    }

    private void mapDisplayProcess(Message msg, String id, String address) {
        try {
            String data = mapApiProcess.mapDisplay(getBotToken(), id, address);
            if (data != null) {
                sendMessage(msg, data);
            }
        } catch (BotException e) {
            sendMessage(msg, e.getMessage());
        }
    }

    private void addrInfoProcess(Message msg, String address) {
        try {
            String info = mapApiProcess.addrInfo(address);
            sendMessage(msg, info);
        } catch (BotException e) {
            sendMessage(msg, e.getMessage());
        }
    }

    private void routeProcess(Message msg, Coordinates geolocation) {
        try {
            String route = mapApiProcess.createRouteWithAddress(geolocation);
            sendMessage(msg, route);
        } catch (BotException e) {
            mapApiProcess.resetValues();
            sendMessage(msg, e.getMessage());
        }
    }

    private void routeProcess(Message msg, String addr) {
        try {
            String route = mapApiProcess.createRouteWithAddress(addr, SearchCategories.CAFE);
            sendMessage(msg, route);
            if (mapApiProcess.getMiddlePointOnMap()) {
                mapApiProcess.coordinatesMapDisplay(getBotToken(), msg.getChatId().toString());
            }
        } catch (BotException e) {
            mapApiProcess.resetValues();
            sendMessage(msg, e.getMessage());
        }
    }

    private void sendMessage(Message msg, String data) {
        String chatId = msg.getChatId().toString();
        SendMessage message = new SendMessage(chatId, data);

        if (mapApiProcess.getButton()) {
            button.setUpGeolocation(message);
        }
        if (mapApiProcess.getButtonDel()) {
            button.removeKeyboard(message);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
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