package com.myProj.Animeshnik.model;

import com.myProj.Animeshnik.config.BotConfig;
import com.myProj.Animeshnik.service.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Getter
@Setter
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private BotConfig config;
    final static String GREETING_TEXT = """
            Konnichiwa, fellow animeshnik - san\s
            I am the greatest Animeshnik bot, my powers are beyond reality\040
                        
            Press /keyboard to activate command keyboard            
            Use menu button or keyboard thing near the paper clip symbol to get access to my commands
                        
            My commands are listed below:

            /random - to receive absolutely random japanese animation
                        
            /watchlist - to get your anime

            and other commands are in development :(""";
    @Autowired
    private AnimeService animeService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VirtualKeyboardService virtualKeyboardService;
    @Autowired
    private AnimeDBService animeDBService;
    @Autowired
    private WatchlistService watchlistService;

    @Autowired
    private BotCommandService botCommandService;
    private String unparsedAnime;

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

                botCommandService.registerUser(update.getMessage(), userRepository);
                executeMessage(virtualKeyboardService.sendMessageNoVirtualKeyboard(update.getMessage().getChatId(), GREETING_TEXT));

            } else if ("/keyboard".equals(message)) {
                executeMessage(virtualKeyboardService.sendMessageWithVirtualKeyboard(update.getMessage().getChatId(), "Keyboard!",
                        virtualKeyboardService));
            } else if ("/random".equals(message)) {

                unparsedAnime = animeService.getRandomAnime();
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime);

                executeMessage(watchlistService.addAnimeToWatchListButton(update.getMessage().getChatId(),
                        parsedAnime));

            } else if ("/watchlist".equals(message)) {

                List<String> userAnimeList;
                String formattedList;
                try {

                    User user = userRepository.findById(update.getMessage().getChatId()).orElseThrow(() -> new UserPrincipalNotFoundException("User not found"));

                    if (user != null && user.getAnimeList() != null) {
                        userAnimeList = user.getAnimeList();
                        formattedList = watchlistService.formatAnimeList(userAnimeList);
                        if (formattedList.isEmpty()) {
                            formattedList = "There are no anime in your list.";
                        }
                        log.info("parsed watchlist: " + formattedList);
                        prepareAndSendMessage(update.getMessage().getChatId(), formattedList);

                    } else {
                        prepareAndSendMessage(update.getMessage().getChatId(), "There are no anime in your list. ");
                    }


                } catch (UserPrincipalNotFoundException e) {
                    log.error("User not found: " + e.getMessage());
                    prepareAndSendMessage(update.getMessage().getChatId(), """
                            You are not yet registered, press /start to register, then you'll be able to add anime.""");
                    throw new RuntimeException(e);
                }
            } else {
                prepareAndSendMessage(update.getMessage().getChatId(), "Command is not supported.");
            }
        } else if (update.hasCallbackQuery()) {

            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (callbackData.equals("ADD_ANIME_TO_WATCHLIST_BUTTON")) {

                String titleName = update.getCallbackQuery().getMessage().getText();
                String extractedTitle = animeService.extractAnimeTitle(titleName);

                try {
                    User user = userRepository.findById(chatId).orElseThrow(() -> new UserPrincipalNotFoundException("User not found"));
                    List<String> userAnimeList;

                    if (user.getAnimeList() != null) {
                        userAnimeList = user.getAnimeList();
                        if (!userAnimeList.contains(extractedTitle)) {
                            animeDBService.addAnimeToWatchlist(chatId, extractedTitle);
                            EditMessageText messageToExecute = botCommandService.updateMessageText(chatId, (int) messageId,
                                    "Added anime: " + extractedTitle + " to your watchlist, check: \n/watchlist");
                            executeMessage(messageToExecute);
                        } else {
                            EditMessageText animeDuplicateMsg = botCommandService.updateMessageText(chatId, (int) messageId,
                                    "Anime: " + extractedTitle + " is already in watchlist, check: \n/watchlist");
                            executeMessage(animeDuplicateMsg);
                        }
                    } else {
                        user.setAnimeList(new ArrayList<>());
                        animeDBService.addAnimeToWatchlist(chatId, extractedTitle);
                        EditMessageText messageToExecute = botCommandService.updateMessageText(chatId, (int) messageId,
                                "Added anime: " + extractedTitle + " to your watchlist, check: \n/watchlist");
                        executeMessage(messageToExecute);
                    }
                } catch (UserPrincipalNotFoundException e) {
                    log.info("User not found: " + e.getMessage());
                    throw new RuntimeException(e);
                }

            } else if ("/by_genre".equals(callbackData)) {// Do something when the "by_genre" button is pressed
                prepareAndSendMessage(chatId, "Recommend by genre is in development.");
            } else if ("/by_rating".equals(callbackData)) {// Do something when the "by_rating" button is pressed
                prepareAndSendMessage(chatId, "Recommend by rating is in development.");
            }
        }


    }

    private void executeMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);

        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void executeMessage(EditMessageText editMessageText) {
        try {
            execute(editMessageText);

        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        executeMessage(sendMessage);
    }
}