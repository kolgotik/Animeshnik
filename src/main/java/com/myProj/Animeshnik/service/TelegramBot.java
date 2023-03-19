package com.myProj.Animeshnik.service;

import com.myProj.Animeshnik.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();


            if ("/start".equals(message)) {
                startOnCommandReceived(update.getMessage().getChatId(), "Hi, " + update.getMessage().getChat().getFirstName());
                sendMessageButton(update.getMessage().getChatId());
            } else {
                sendMessage(update.getMessage().getChatId(), "Command is not supported.");
            }
        }
        if (update.hasCallbackQuery()) {

            String buttonData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (buttonData) {
                case "random":
                    // Do something when the "random" button is pressed
                    String query = "query ($id: Int) {\n"
                            + "  Media (id: $id, type: ANIME) {\n"
                            + "    id\n"
                            + "    title {\n"
                            + "      romaji\n"
                            + "      english\n"
                            + "      native\n"
                            + "    }\n"
                            + "  }\n"
                            + "}";

                    Map<String, Object> variables = new LinkedHashMap<>();
                    variables.put("id", 15125);

                    String body = String.format("{\"query\":\"%s\",\"variables\":%s}", query, variables.toString());

                    try {
                        URL url = new URL("https://graphql.anilist.co");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setDoOutput(true);

                        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                        writer.write(body);
                        writer.flush();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        System.out.println(response.toString());
                    } catch (Exception e) {
                        log.error("Error occurred: " + e.getMessage());
                    }


                    break;
                case "by_genre":
                    // Do something when the "by_genre" button is pressed
                    sendMessage(chatId, "Recommend by genre is in development.");
                    break;
                case "by_rating":
                    // Do something when the "by_rating" button is pressed
                    sendMessage(chatId, "Recommend by rating is in development.");
                    break;
                default:
                    break;
            }
        }


    }


    private void startOnCommandReceived(long chatId, String firstName) {
        String answer = "Helo, " + firstName + ", nice to meet you!";
        sendMessage(chatId, answer);
        log.info("Replied to user: " + firstName);
    }

    private void sendMessageButton(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Greetings! Please select an option: ");
        InlineKeyboardButton randomButton = new InlineKeyboardButton("Random recommendation");
        randomButton.setCallbackData("random");

        InlineKeyboardButton genreButton = new InlineKeyboardButton("Recommend by genre");
        genreButton.setCallbackData("by_genre");

        InlineKeyboardButton ratingButton = new InlineKeyboardButton("Recommend by rating");
        ratingButton.setCallbackData("by_rating");

        List<InlineKeyboardButton> keyboardRow1 = new ArrayList<>();
        keyboardRow1.add(randomButton);

        List<InlineKeyboardButton> keyboardRow2 = new ArrayList<>();
        keyboardRow2.add(genreButton);

        List<InlineKeyboardButton> keyboardRow3 = new ArrayList<>();
        keyboardRow3.add(ratingButton);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(keyboardRow1);
        keyboard.add(keyboardRow2);
        keyboard.add(keyboardRow3);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(markup);


        try {
            execute(sendMessage);

        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
