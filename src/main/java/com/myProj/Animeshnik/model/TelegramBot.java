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

            /random - to receive random anime
            
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
    private List<String> animeList = new ArrayList<>();

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
                sendMessageNoVirtualKeyboard(update.getMessage().getChatId(), GREETING_TEXT);

            }else if ("/keyboard".equals(message)){

                sendMessageWithVirtualKeyboard(update.getMessage().getChatId(), "Keyboard!");

            }
            else if ("/random".equals(message)) {

                unparsedAnime = animeService.getRandomAnime();
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime);

                addAnimeToWatchListButton(update.getMessage().getChatId(), parsedAnime);


            } else if ("/watchlist".equals(message)) {

                try {

                    User user = userRepository.findById(update.getMessage().getChatId()).orElseThrow(() -> new UserPrincipalNotFoundException("User not found"));
                    List<String> userAnimeList;
                    String formattedList;
                    if (user.getAnimeList() != null) {

                        userAnimeList = user.getAnimeList();
                        formattedList = watchlistService.formatAnimeList(userAnimeList);
                        log.info("parsed watchlist: " + formattedList);
                        prepareAndSendMessage(update.getMessage().getChatId(), formattedList);

                    } else {
                        prepareAndSendMessage(update.getMessage().getChatId(), "There are no anime in your list. ");
                    }


                } catch (UserPrincipalNotFoundException e) {
                    log.info("User not found: " + e.getMessage());
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
                        if (userAnimeList.contains(extractedTitle)) {
                            EditMessageText messageToExecute = botCommandService.updateMessageText(chatId, (int) messageId,
                                    "Anime: " + extractedTitle + " is already in watchlist, check: \n/watchlist");
                            executeMessage(messageToExecute);
                        }
                    }
                    animeDBService.addAnimeToWatchlist(chatId, extractedTitle);

                    EditMessageText messageToExecute = botCommandService.updateMessageText(chatId, (int) messageId,
                            "Added anime: " + extractedTitle + " to your watchlist, check: \n/watchlist");
                    executeMessage(messageToExecute);


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

    /*protected void updateMessageText(long chatId, int messageId, String updatedText) {

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
        editMessageText.setText(updatedText);
        executeMessage(editMessageText);
    }*/

    private void addAnimeToWatchListButton(long chatId, String anime) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var addAnimeToWatchlistButton = new InlineKeyboardButton();
        addAnimeToWatchlistButton.setText("Add anime to watchlist");
        addAnimeToWatchlistButton.setCallbackData("ADD_ANIME_TO_WATCHLIST_BUTTON");

        rowInline.add(addAnimeToWatchlistButton);

        rowsInline.add(rowInline);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setText(anime);
        executeMessage(sendMessage);
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
        sendMessageWithVirtualKeyboard(chatId, answer);
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

        executeMessage(sendMessage);
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

    private void sendMessageWithVirtualKeyboard(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        virtualKeyboardService.sendGeneralVirtualCommandKeyboardWithMessage(sendMessage);

        executeMessage(sendMessage);
    }

    private void sendMessageNoVirtualKeyboard(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        executeMessage(sendMessage);
    }

    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        executeMessage(sendMessage);
    }
}
