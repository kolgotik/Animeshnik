package com.myProj.Animeshnik.model;

import com.myProj.Animeshnik.DAO.UserDAO;
import com.myProj.Animeshnik.config.BotConfig;
import com.myProj.Animeshnik.service.*;
import com.myProj.Animeshnik.serviceImpl.AnimeServiceImpl;
import com.myProj.Animeshnik.serviceImpl.GetAnimeByGenreServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Getter
@Setter
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private BotConfig config;
    final static String GREETING_TEXT = """
            Konnichiwa, my dear fellow animeshnik-san!\s
                        
            I am the most supreme and mighty Animeshnik bot, possessing powers that transcend the boundaries of reality itself!
                        
            Press the /keyboard command to unleash the full extent of my awe-inspiring abilities! Press menu button or, you can use the fancy little button (located near the paper clip symbol) to access all of my commands.
                                                    
            Behold, my current arsenal of commands:
                                                    
            /random - to receive the most outrageously random Japanese (and not only) animation imaginable!
                                                    
            /watchlist - to gaze upon the wondrous collection of your anime!
                                                    
            And fear not, for more amazing commands are in development, just waiting to be unleashed upon the unsuspecting masses!""";
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
    private GetAnimeByRatingService getAnimeByRatingService;
    @Autowired
    private BotCommandService botCommandService;
    @Autowired
    private GetAnimeByGenreService genreService;
    @Autowired
    private UserDAO userDAO;
    private String unparsedAnime;
    private String anime;
    @Autowired
    private GetAnimeByGenreServiceImpl byGenreServiceImpl;

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
                executeMessage(genreService.sendGenreSelection(update.getMessage().getChatId()));
                //prepareAndSendMessage(update.getMessage().getChatId(), "Recommend by genre is in development.");
            } else if ("/by_rating".equals(message)) {
                executeMessage(getAnimeByRatingService.getAnimeByRatingOptions(update.getMessage().getChatId()));
                //prepareAndSendMessage(update.getMessage().getChatId(), "Recommend by rating is in development.");
            } else if ("/keyboard".equals(message)) {
                executeMessage(virtualKeyboardService.sendMessageWithVirtualKeyboard(update.getMessage().getChatId(), "Keyboard!",
                        virtualKeyboardService));
            } else if ("/random".equals(message)) {
                String unparsedAnime = animeService.getRandomAnime(); // Get random anime
                int animeId = animeService.getAnimeIdFromJSON(unparsedAnime);
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime); // Parse anime JSON
                String imgLink = animeService.extractImgLink(unparsedAnime); // Extract image link
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(String.valueOf(update.getMessage().getChatId()));
                sendPhoto.setPhoto(new InputFile(imgLink));

                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(update.getMessage().getChatId(), parsedAnime);
                } else {
                    executeMessageWithImage(sendPhoto);
                    executeMessage(watchlistService.addAnimeToWatchListButton(update.getMessage().getChatId(), parsedAnime, animeId));
                }
            }

            /*else if ("/random".equals(message)) {
                unparsedAnime = animeService.getRandomAnime();
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime);
                String imgLink = animeService.extractImgLink(unparsedAnime);
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(String.valueOf(update.getMessage().getChatId()));
                sendPhoto.setPhoto(new InputFile(imgLink));
                animeId = animeService.animeId;
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(update.getMessage().getChatId(), parsedAnime);
                } else {
                    executeMessageWithImage(sendPhoto);
                    executeMessage(watchlistService.addAnimeToWatchListButton(update.getMessage().getChatId(), parsedAnime, animeId));
                }*/


            else if ("/watchlist".equals(message)) {

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
                            //executeMessageWithImage(watchlistService.animeListWithImg(update.getMessage().getChatId(), userAnimeList, user, user.getAnimeIdList()));
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

            if (callbackData.equals("BELOW-FIFTY")) {
                unparsedAnime = getAnimeByRatingService.getAnimeByRating50();
                while (unparsedAnime.equals("{\"data\":{\"Page\":{\"media\":[]}}}") || unparsedAnime.isBlank() || unparsedAnime.isEmpty()) {
                    unparsedAnime = getAnimeByRatingService.getAnimeByRating50();
                }
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime);
                String imgLink = animeService.extractImgLink(unparsedAnime);
                animeId = animeService.animeId;
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(chatId, parsedAnime);
                } else {
                    executeMessageWithImage(new SendPhoto(String.valueOf(chatId), new InputFile(imgLink)));
                    executeMessage(watchlistService.addAnimeByRatingToWatchListButton(chatId, parsedAnime, animeId, "BELOW-FIFTY"));
                }
            }
            if (callbackData.equals("FIFTY-SIXTY")) {
                unparsedAnime = getAnimeByRatingService.getAnimeByRating50to60();
                while (unparsedAnime.equals("{\"data\":{\"Page\":{\"media\":[]}}}") || unparsedAnime.isBlank() || unparsedAnime.isEmpty()) {
                    unparsedAnime = getAnimeByRatingService.getAnimeByRating50to60();
                }
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime);
                String imgLink = animeService.extractImgLink(unparsedAnime);
                animeId = animeService.animeId;
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(chatId, parsedAnime);
                } else {
                    executeMessageWithImage(new SendPhoto(String.valueOf(chatId), new InputFile(imgLink)));
                    executeMessage(watchlistService.addAnimeByRatingToWatchListButton(chatId, parsedAnime, animeId, "FIFTY-SIXTY"));
                }
            }
            if (callbackData.equals("SIXTY-EIGHTY")) {
                unparsedAnime = getAnimeByRatingService.getAnimeByRating60to80();
                while (unparsedAnime.equals("{\"data\":{\"Page\":{\"media\":[]}}}") || unparsedAnime.isBlank() || unparsedAnime.isEmpty()) {
                    unparsedAnime = getAnimeByRatingService.getAnimeByRating60to80();
                }
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime);
                String imgLink = animeService.extractImgLink(unparsedAnime);
                animeId = animeService.animeId;
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(chatId, parsedAnime);
                } else {
                    executeMessageWithImage(new SendPhoto(String.valueOf(chatId), new InputFile(imgLink)));
                    executeMessage(watchlistService.addAnimeByRatingToWatchListButton(chatId, parsedAnime, animeId, "SIXTY-EIGHTY"));
                }
            }
            if (callbackData.equals("EIGHTY-HUNDRED")) {
                String unparsedAnime = getAnimeByRatingService.getAnimeByRating80to100();
                while (unparsedAnime.equals("{\"data\":{\"Page\":{\"media\":[]}}}") || unparsedAnime.isBlank() || unparsedAnime.isEmpty()) {
                    unparsedAnime = getAnimeByRatingService.getAnimeByRating80to100();
                }
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime);
                String imgLink = animeService.extractImgLink(unparsedAnime);
                animeId = animeService.animeId;
                int animeId = animeService.getAnimeIdFromJSON(unparsedAnime);
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(chatId, parsedAnime);
                } else {
                    executeMessageWithImage(new SendPhoto(String.valueOf(chatId), new InputFile(imgLink)));
                    executeMessage(watchlistService.addAnimeByRatingToWatchListButton(chatId, parsedAnime, animeId, "EIGHTY-HUNDRED"));
                }
            }
            ConcurrentHashMap<String, Boolean> genreOptions = byGenreServiceImpl.getGenreOption(chatId);
            if (genreOptions.keySet().stream().anyMatch(callbackData::contains)) {
                String genre = callbackData;
                boolean isSelected = genreOptions.get(genre);
                genreOptions.put(genre, !isSelected);
                //byGenreServiceImpl.setGenreOptions(options);
                executeMessage(genreService.updateGenreListOnSelect(chatId, (int) messageId, genreOptions));
            }

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
                                    "Added anime: " + anime + " to your watchlist, check: \n/watchlist or /keyboard to choose action");
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
                                "Added anime: " + anime + " to your watchlist, check: \n/watchlist or /keyboard to choose action");
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

            if (callbackData.equals("/random")) {
                String unparsedAnime = animeService.getRandomAnime();
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime);
                String imgLink = animeService.extractImgLink(unparsedAnime);
                animeId = animeService.animeId;
                int animeId = animeService.getAnimeIdFromJSON(unparsedAnime);
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(chatId, parsedAnime);
                } else {
                    executeMessageWithImage(new SendPhoto(String.valueOf(chatId), new InputFile(imgLink)));
                    executeMessage(watchlistService.addAnimeToWatchListButton(chatId, parsedAnime, animeId));
                }
            }

            User user;

            if (userForWatchlistActions.getAnimeList() != null && !userForWatchlistActions.getAnimeList().isEmpty()) {
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
            } else {
                return;
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

    private void executeMessageWithImage(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);

        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
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

    private void executeMessage(SendPoll sendPoll) {
        try {
            execute(sendPoll);

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

    private void prepareAndUpdateMessage(long chatId, int messageId, String textToSend) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setText(textToSend);
        editMessageText.setMessageId(messageId);

        executeMessage(editMessageText);
    }
}
