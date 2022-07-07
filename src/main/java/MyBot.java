import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MyBot extends TelegramLongPollingBot implements TelegramBotUtils {

    private String chatId;
    private String message;
    private String state;

    private String[] months = {"january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december"};

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        this.chatId =update.getMessage().getChatId().toString();
        if (update.hasMessage()) {
            String text = update.getMessage().getText();

            switch (text){
                case "/start" -> {
                    this.state = "/start";
                }
                case "Search by passport" -> {
                    this.state = "Search by passport";
                }
                case "Search by car number" -> {
                    this.state = "Search by car number";
                }
                case "Fine history" -> {
                    this.state = "Fine history";
                }
                case "Search by months" -> {
                    this.state = "Search by months";
                }
            }

            executeState(text);
        }
    }

    private void executeState(String text) throws SQLException {
        switch (state){
            case "/start" -> {
                message = "Welcome to fine checker bot";
                execute(menu(), null);
            }
            case "Search by passport" -> {
                message = "Enter user passport number:";
                this.state = "search_by_passport_number";
                execute(null, null);
            }
            case "Search by car number" -> {
                message = "Enter car number:";
                this.state = "search_by_car_number";
                execute(null, null);
            }
            case "Fine history" -> {
                this.state = "fine_history";
            }

//                case "Search by months" -> {
//                    this.state = "search_by_months";
//                }
            case "search_by_passport_number", "search_by_car_number" -> {
                showFine(text);
            }
            case "fine_history" -> {
                this.state = "fine_hitory";
            }

        }

    }


    private void execute(ReplyKeyboardMarkup r, InlineKeyboardMarkup i){
        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.setReplyMarkup(i == null ? r : i);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private ReplyKeyboardMarkup menu(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Search by passport");
        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add("Search by car number");
        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow2.add("Fine history");

        keyboardRows.add(keyboardRow);
        keyboardRows.add(keyboardRow1);
        keyboardRows.add(keyboardRow2);

        return replyKeyboardMarkup;
    }

    private InlineKeyboardMarkup showMonths(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> list = new ArrayList<>();
        inlineKeyboardMarkup.setKeyboard(list);
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
        for(int i=0; i<12; i++){
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton((months[i]));
            inlineKeyboardButton.setCallbackData( months[i]);
            inlineKeyboardButtons.add(inlineKeyboardButton);

            if((i+1)%3 == 0){
                list.add(inlineKeyboardButtons);
                inlineKeyboardButtons = new ArrayList<>();
            }
        }
        return inlineKeyboardMarkup;
    }


    private void showFine(String text) throws SQLException {
        System.out.println(message + " " + chatId);

        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/car_fine", "postgres", "2504");

            String query = "select * from get_user_fine(?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, text);

            ResultSet resultSet = preparedStatement.executeQuery();

            String stringBuilder = "";
            while (resultSet.next()) {
                stringBuilder = "user name: " + resultSet.getString("user_name") + "\n" +
                        "user password num: " + resultSet.getString("user_password_num") + "\n" +
                        "car name: " + resultSet.getString("car_name") + "\n" +
                        "car number: " + resultSet.getString("car_number") +
                        "fine amount: " + resultSet.getString("fine_amount") +
                        "fine status: " + resultSet.getString("fine_status");
            }
            stringBuilder = stringBuilder + "\n" +
                    getQueryExecutionTime("explain analyse select * from get_user_fine(?)", message);
            SendMessage sendMessage = new SendMessage(chatId, stringBuilder);
            execute(sendMessage);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }finally {
            connection.close();
        }




    }


    public String getQueryExecutionTime(String query, String messageFromUser){
        List<String> results = new ArrayList<>();
        String res = "";
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/car_fine", "postgres", "2504");
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, messageFromUser);
            ResultSet resultSet = preparedStatement.executeQuery();
            int i = 0;
            while (resultSet.next()){
                if (i > 1)
                    results.add(resultSet.getString("QUERY PLAN") + "\n");
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int len = results.size();
        for (int i = len - 1; i > -1; i--) {
            res += results.get(i);
        }

        return res;
    }

    public String getQueryExecutionTime(String query, String messageFromUser1, String messageFromUser2){
        List<String> results = new ArrayList<>();
        String res = "";
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/car_fine_bot", "postgres", "0707");
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, messageFromUser1);
            preparedStatement.setString(2, messageFromUser2);
            ResultSet resultSet = preparedStatement.executeQuery();
            int i = 0;
            while (resultSet.next()){
                if (i > 1)
                    results.add(resultSet.getString("QUERY PLAN") + "\n");
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int len = results.size();
        for (int i = len - 1; i > -1; i--) {
            res += results.get(i);
        }
        return res;
    }



}
