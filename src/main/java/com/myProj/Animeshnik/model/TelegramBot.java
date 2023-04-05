package com.myProj.Animeshnik.model;

import com.myProj.Animeshnik.DAO.UserDAO;
import com.myProj.Animeshnik.config.BotConfig;
import com.myProj.Animeshnik.service.*;
import com.myProj.Animeshnik.serviceImpl.AnimeServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.attribute.UserPrincipalNotFoundException;
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
    private AnimeServiceImpl animeService;
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
    @Autowired
    private UserDAO userDAO;
    private String unparsedAnime;
    private String anime;

    private Integer animeId;

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

            } else if ("/by_genre".equals(message)) {
                prepareAndSendMessage(update.getMessage().getChatId(), "Recommend by genre is in development.");
            } else if ("/by_rating".equals(message)) {
                prepareAndSendMessage(update.getMessage().getChatId(), "Recommend by rating is in development.");
            } else if ("/keyboard".equals(message)) {
                executeMessage(virtualKeyboardService.sendMessageWithVirtualKeyboard(update.getMessage().getChatId(), "Keyboard!",
                        virtualKeyboardService));
            } else if ("/random".equals(message)) {
                unparsedAnime = animeService.getRandomAnime();
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime);
                animeId = animeService.animeId;
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(update.getMessage().getChatId(), parsedAnime);
                } else {
                    executeMessage(watchlistService.addAnimeToWatchListButton(update.getMessage().getChatId(), parsedAnime, animeId));
                }


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
                            prepareAndSendMessage(update.getMessage().getChatId(), formattedList);
                        } else {
                            log.info("parsed watchlist: " + formattedList);
                            //prepareAndSendMessage(update.getMessage().getChatId(), formattedList);
                            executeMessage(watchlistService.animeList(update.getMessage().getChatId(), userAnimeList, user, user.getAnimeIdList()));
                        }
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

            User userForWatchlistActions = userRepository.findById(chatId).orElseThrow();


            String currentCallback = update.getCallbackQuery().getData();


            if (callbackData.startsWith("ADD_ANIME_TO_WATCHLIST_BUTTON")) {

                animeId = Integer.valueOf(currentCallback.replace("ADD_ANIME_TO_WATCHLIST_BUTTON", ""));
                    /*int animeIdIndex = animeIdList.indexOf(animeId);
                    anime = animeList.get(animeIdIndex);*/


                String titleName = update.getCallbackQuery().getMessage().getText();

                    /*anime.setOverallInfo(update.getCallbackQuery().getMessage().getText());
                    log.info("Overall anime info: " + anime.getOverallInfo());*/

                anime = animeService.extractAnimeTitle(titleName);
                //String titleForDataRetrieve = animeService.getAnimeTitleFromResponse(unparsedAnime);
                //log.info("Title for data retrieve: " + titleForDataRetrieve);
                //animeId = animeService.getAnimeIdFromAPI(extractedTitle);


                if (anime.getBytes().length > 64) {
                    anime = anime.substring(0, 61) + "...";
                }


                log.info("Retrieved Id: " + animeId);

                log.info("Retrieved Anime: " + anime);

                try {
                    User user = userRepository.findById(chatId).orElseThrow(() -> new UserPrincipalNotFoundException("User not found"));
                    List<String> userAnimeList;
                    List<Integer> userAnimeIdList;

                    if (user.getAnimeList() != null) {
                        userAnimeList = user.getAnimeList();
                        userAnimeIdList = user.getAnimeIdList();
                        log.info("Anime list: " + userAnimeList);
                        log.info("AnimeId list: " + userAnimeIdList);
                        if (!userAnimeList.contains(anime)) {
                            animeDBService.addAnimeToWatchlist(chatId, anime, animeId);
                            EditMessageText messageToExecute = botCommandService.updateMessageText(chatId, (int) messageId,
                                    "Added anime: " + anime + " to your watchlist, check: \n/watchlist");
                            executeMessage(messageToExecute);
                        } else {
                            EditMessageText animeDuplicateMsg = botCommandService.updateMessageText(chatId, (int) messageId,
                                    "Anime: " + anime + " is already in watchlist, check: \n/watchlist");
                            executeMessage(animeDuplicateMsg);
                        }
                    } else {
                        user.setAnimeList(new ArrayList<>());
                        user.setAnimeIdList(new ArrayList<>());
                        animeDBService.addAnimeToWatchlist(chatId, anime, animeId);
                        EditMessageText messageToExecute = botCommandService.updateMessageText(chatId, (int) messageId,
                                "Added anime: " + anime + " to your watchlist, check: \n/watchlist");
                        executeMessage(messageToExecute);
                    }
                } catch (UserPrincipalNotFoundException e) {
                    log.error("User not found: " + e.getMessage());
                    throw new RuntimeException(e);
                } catch (DataIntegrityViolationException e) {
                    log.error("Data too long: " + e.getMessage());
                    String parsedAnime = animeService.parseJSONAnime(unparsedAnime);
                    executeMessage(watchlistService.addAnimeToWatchListButton(chatId, "Yamero! Your list is too big, remove some anime /watchlist \n\n" + parsedAnime, animeId, (int) messageId));
                }

            }


            User user;

            for (Integer id : userForWatchlistActions.getAnimeIdList()) {

                if (callbackData.equals(String.valueOf(id))) {

                    String animeTitle = null;

                    try {
                        user = userRepository.findById(chatId).orElseThrow(() -> new UserPrincipalNotFoundException("User not found"));
                        log.info("Anime: " + animeTitle);

                        Integer indexOfAnime = user.getAnimeIdList().indexOf(id);
                        animeTitle = userForWatchlistActions.getAnimeList().get(indexOfAnime);
                        animeId = id;

                        EditMessageText editMessageText;

                        if (animeTitle != null) {
                            editMessageText = watchlistService.animeDetails(chatId, animeTitle, animeId, (int) messageId);
                        } else {
                            editMessageText = new EditMessageText();
                            editMessageText.setChatId(String.valueOf(chatId));
                            editMessageText.setMessageId((int) messageId);
                            editMessageText.setText("Error: Could not extract anime title from message");
                        }
                        executeMessage(editMessageText);
                    } catch (UserPrincipalNotFoundException e) {
                        log.error("User not found: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            }


            List<String> animeList = userForWatchlistActions.getAnimeList();
            List<Integer> animeIdList = userForWatchlistActions.getAnimeIdList();
            List<String> listForRemove = new ArrayList<>();

            for (Integer id : animeIdList) {
                String modifiedId = "REMOVE" + id;
                listForRemove.add(modifiedId);
            }

            for (String animeToRemoveId : listForRemove) {
                if (callbackData.equals(animeToRemoveId)) {
                    animeId = Integer.valueOf(animeToRemoveId.replace("REMOVE", ""));
                    int animeIdIndex = animeIdList.indexOf(animeId);
                    anime = animeList.get(animeIdIndex);
                    executeMessage(watchlistService.addYesNoButton(chatId, anime, messageId));

                }
            }
            if (callbackData.equals("YES")) {

                animeDBService.removeAnimeFromWatchlist(userForWatchlistActions, animeId, chatId);
                try {
                    userForWatchlistActions = userRepository.findById(chatId).orElseThrow(() -> new UserPrincipalNotFoundException("User not found"));
                } catch (UserPrincipalNotFoundException e) {
                    log.error("no user found: " + e.getMessage());
                }
                List<String> userAnimeList = userForWatchlistActions.getAnimeList();
                executeMessage(watchlistService.animeList(chatId, userAnimeList, userForWatchlistActions, messageId, userRepository));
            }

            if (callbackData.equals("BACK_TO_LIST")) {
                executeMessage(watchlistService.animeList(chatId, userForWatchlistActions.getAnimeList(), userForWatchlistActions, messageId, userRepository));
            }

            if (callbackData.equals("BACK_TO_OPTIONS")) {
                executeMessage(watchlistService.animeDetails(chatId, anime, animeId, (int) messageId));
            }

            if (callbackData.equals("NO")) {
                executeMessage(watchlistService.animeDetails(chatId, anime, animeId, (int) messageId));
            }


            List<String> listOfIdForDescription = new ArrayList<>();

            for (Integer animeId : animeIdList) {
                String modifiedId = "DESCRIPTION" + animeId;
                listOfIdForDescription.add(modifiedId);

            }
            for (String animeToGetDescrId : listOfIdForDescription) {
                if (callbackData.equals(animeToGetDescrId)) {
                    animeId = Integer.valueOf(animeToGetDescrId.replace("DESCRIPTION", ""));
                    log.info("ID: " + animeId);
                    String rawDescription = animeService.getAnimeDescription(animeId);
                    int animeIdIndex = animeIdList.indexOf(animeId);
                    anime = animeList.get(animeIdIndex);
                    //String parsedDescription = watchlistService.parseJSONDescription(rawDescription);
                    executeMessage(watchlistService.parseJSONDescription(chatId, rawDescription, messageId));
                    log.info("Retrieved description:" + rawDescription);
                }

            }

        }


    }

    private void executeMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);

        } catch (TelegramApiException e) {
            log.error("Error occurred at TG bot 300: " + e.getMessage());
        }
    }

    private void executeMessage(EditMessageText editMessageText) {
        try {
            execute(editMessageText);

        } catch (TelegramApiException e) {
            log.error("Error occurred 309: " + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        executeMessage(sendMessage);
    }

    private void prepareAndUpdateMessage(long chatId, int messageId, String textToSend) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setText(textToSend);
        editMessageText.setMessageId(messageId);

        executeMessage(editMessageText);
    }
}
