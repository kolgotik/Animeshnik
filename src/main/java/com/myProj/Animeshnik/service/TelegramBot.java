package com.myProj.Animeshnik.service;

import com.myProj.Animeshnik.config.BotConfig;
import com.myProj.Animeshnik.model.User;
import com.myProj.Animeshnik.model.UserRepository;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    @Autowired
    private AnimeService animeService;

    @Autowired
    private UserRepository userRepository;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "start the bot"));
        botCommandList.add(new BotCommand("/random", "get random anime"));
        botCommandList.add(new BotCommand("/by_genre", "get anime by genre"));
        botCommandList.add(new BotCommand("/by_rating", "get anime by rating"));
        botCommandList.add(new BotCommand("/help", "info on how to use this bot"));
        botCommandList.add(new BotCommand("/settings", "set custom settings"));
        try {
            execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));

        } catch (TelegramApiException e) {
            log.error("Error setting bot`s command list: " + e.getMessage());
        }
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
                registerUser(update.getMessage());
                sendMessageButton(update.getMessage().getChatId());
            } else if ("/random".equals(message)) {
                String anime = animeService.getRandomAnime();
                sendMessage(update.getMessage().getChatId(), anime);
            } else {
                sendMessage(update.getMessage().getChatId(), "Command is not supported.");
            }
        }
        if (update.hasCallbackQuery()) {

            String buttonData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (buttonData) {
                case "/random":
                    // Do something when the "random" button is pressed
                    String anime = animeService.getRandomAnime();
                    sendMessage(chatId, anime);
                    break;
                case "/by_genre":
                    // Do something when the "by_genre" button is pressed
                    sendMessage(chatId, "Recommend by genre is in development.");
                    break;
                case "/by_rating":
                    // Do something when the "by_rating" button is pressed
                    sendMessage(chatId, "Recommend by rating is in development.");
                    break;
                default:
                    break;
            }
        }


    }

    private void registerUser(Message message) {

        if (userRepository.findById(message.getChatId()).isEmpty()) {

            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("User saved: " + user);
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
        randomButton.setCallbackData("/random");

        InlineKeyboardButton genreButton = new InlineKeyboardButton("Recommend by genre");
        genreButton.setCallbackData("/by_genre");

        InlineKeyboardButton ratingButton = new InlineKeyboardButton("Recommend by rating");
        ratingButton.setCallbackData("/by_rating");

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
