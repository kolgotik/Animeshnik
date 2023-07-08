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
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
            
            /by_genre - to get anime with your preferred genres!
            
            /by_rating - to get anime by it's rating there are several options!
                                                    
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

    private ConcurrentHashMap<Long, Integer> pageCountMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long, List<Integer>> listOfId = new ConcurrentHashMap<>();

    private Integer animeId;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "general info about the bot"));
        botCommandList.add(new BotCommand("/keyboard", "activates command keyboard"));
        botCommandList.add(new BotCommand("/random", "get random anime"));
        botCommandList.add(new BotCommand("/by_genre", "get anime by genre"));
        botCommandList.add(new BotCommand("/by_rating", "get anime by rating"));
        botCommandList.add(new BotCommand("/watchlist", "get anime added to your watchlist"));
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
            String replacedMessage = "";

            if (message.equals("\uD83C\uDFB2 random")) {
                replacedMessage = message.replaceAll("\uD83C\uDFB2 random", "/random");
            }
            if (message.equals("\uD83C\uDFAD by genre")) {
                replacedMessage = message.replaceAll("\uD83C\uDFAD by genre", "/by_genre");
            }
            if (message.equals("⭐ by rating")) {
                replacedMessage = message.replaceAll("⭐ by rating", "/by_rating");
            }
            if (message.equals("\uD83D\uDCDD watchlist")) {
                replacedMessage = message.replaceAll("\uD83D\uDCDD watchlist", "/watchlist");
            }

            if ("/start".equals(message)) {

                botCommandService.registerUser(update.getMessage(), userRepository);
                executeMessage(virtualKeyboardService.sendMessageNoVirtualKeyboard(update.getMessage().getChatId(), GREETING_TEXT));

            } else if ("/by_genre".equals(replacedMessage) || "/by_genre".equals(message)) {

                pageCountMap.clear();
                listOfId.clear();

                ConcurrentHashMap<String, Boolean> genreOptions = byGenreServiceImpl.getGenreOption(update.getMessage().getChatId());
                genreOptions.replaceAll((k, v) -> false);

                executeMessage(genreService.sendGenreSelection(update.getMessage().getChatId()));

            } else if ("/by_rating".equals(replacedMessage) || "/by_rating".equals(message)) {

                executeMessage(getAnimeByRatingService.getAnimeByRatingOptions(update.getMessage().getChatId()));

            } else if ("/keyboard".equals(replacedMessage) || "/keyboard".equals(message)) {

                executeMessage(virtualKeyboardService.sendMessageWithVirtualKeyboard(update.getMessage().getChatId(), "Keyboard!",
                        virtualKeyboardService));

            } else if ("/random".equals(replacedMessage) || "/random".equals(message)) {

                String unparsedAnime = animeService.getRandomAnime(); // Get random anime
                int animeId = animeService.getAnimeIdFromJSON(unparsedAnime);
                String imgLink = animeService.extractImgLink(unparsedAnime); // Extract image link
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime, imgLink); // Parse anime JSON
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(String.valueOf(update.getMessage().getChatId()));
                sendPhoto.setPhoto(new InputFile(imgLink));

                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(update.getMessage().getChatId(), parsedAnime);
                } else {
                    executeMessage(watchlistService.addAnimeToWatchListButton(update.getMessage().getChatId(), parsedAnime, animeId));
                }
            }

            else if ("/watchlist".equals(replacedMessage) || "/watchlist".equals(message)) {

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

            if (callbackData.equals("BELOW-FIFTY")) {
                unparsedAnime = getAnimeByRatingService.getAnimeByRating50();
                while (unparsedAnime.equals("{\"data\":{\"Page\":{\"media\":[]}}}") || unparsedAnime.isBlank() || unparsedAnime.isEmpty()) {
                    unparsedAnime = getAnimeByRatingService.getAnimeByRating50();
                }
                String imgLink = animeService.extractImgLink(unparsedAnime);
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime, imgLink); // Parse anime JSON
                animeId = animeService.animeId;
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(chatId, parsedAnime);
                } else {
                    executeMessage(watchlistService.addAnimeByRatingToWatchListButton(chatId, parsedAnime, animeId, "BELOW-FIFTY"));
                }
            }
            if (callbackData.equals("FIFTY-SIXTY")) {
                unparsedAnime = getAnimeByRatingService.getAnimeByRating50to60();
                while (unparsedAnime.equals("{\"data\":{\"Page\":{\"media\":[]}}}") || unparsedAnime.isBlank() || unparsedAnime.isEmpty()) {
                    unparsedAnime = getAnimeByRatingService.getAnimeByRating50to60();
                }
                String imgLink = animeService.extractImgLink(unparsedAnime);
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime, imgLink);
                animeId = animeService.animeId;
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(chatId, parsedAnime);
                } else {
                    executeMessage(watchlistService.addAnimeByRatingToWatchListButton(chatId, parsedAnime, animeId, "FIFTY-SIXTY"));
                }
            }
            if (callbackData.equals("SIXTY-EIGHTY")) {
                unparsedAnime = getAnimeByRatingService.getAnimeByRating60to80();
                while (unparsedAnime.equals("{\"data\":{\"Page\":{\"media\":[]}}}") || unparsedAnime.isBlank() || unparsedAnime.isEmpty()) {
                    unparsedAnime = getAnimeByRatingService.getAnimeByRating60to80();
                }
                String imgLink = animeService.extractImgLink(unparsedAnime);
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime, imgLink);
                animeId = animeService.animeId;
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(chatId, parsedAnime);
                } else {
                    executeMessage(watchlistService.addAnimeByRatingToWatchListButton(chatId, parsedAnime, animeId, "SIXTY-EIGHTY"));
                }
            }
            if (callbackData.equals("EIGHTY-HUNDRED")) {
                String unparsedAnime = getAnimeByRatingService.getAnimeByRating80to100();
                while (unparsedAnime.equals("{\"data\":{\"Page\":{\"media\":[]}}}") || unparsedAnime.isBlank() || unparsedAnime.isEmpty()) {
                    unparsedAnime = getAnimeByRatingService.getAnimeByRating80to100();
                }
                String imgLink = animeService.extractImgLink(unparsedAnime);
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime, imgLink);
                animeId = animeService.animeId;
                int animeId = animeService.getAnimeIdFromJSON(unparsedAnime);
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(chatId, parsedAnime);
                } else {
                    executeMessage(watchlistService.addAnimeByRatingToWatchListButton(chatId, parsedAnime, animeId, "EIGHTY-HUNDRED"));
                }
            }

            updateGenreListOnSelect(callbackData, chatId, (int) messageId);


            if (callbackData.equals("CONFIRM_GENRES")) {

                int pageCount = pageCountMap.getOrDefault(chatId, 0);

                ConcurrentHashMap<String, Boolean> genresToSelect = byGenreServiceImpl.getGenreOption(chatId);
                List<String> selectedGenres = new ArrayList<>();

                for (String genre : genresToSelect.keySet()) {
                    if (genresToSelect.get(genre)) {
                        selectedGenres.add(genre);
                    }
                }
                String idForGenreSelection;
                List<Integer> temp;
                List<Integer> id = new ArrayList<>();
                Random random = new Random();
                int decider = random.nextInt(11);
                String sort = "";
                log.info("DECIDER1: " + decider);
                log.info("GENRES: " + selectedGenres);
                if (decider == 1) {
                    sort = "SCORE_DESC";
                } else if (decider == 2) {
                    sort = "FAVOURITES";
                } else if (decider == 3) {
                    sort = "SCORE";
                } else if (decider == 4) {
                    sort = "POPULARITY";
                } else if (decider == 5) {
                    sort = "POPULARITY_DESC";
                } else if (decider == 6) {
                    sort = "TRENDING";
                } else if (decider == 7) {
                    sort = "TRENDING_DESC";
                } else if (decider == 8) {
                    sort = "FAVOURITES_DESC";
                } else if (decider == 9) {
                    sort = "POPULARITY";
                } else if (decider == 10) {
                    sort = "POPULARITY_DESC";
                } else {
                    sort = "EPISODES_DESC";
                }
                if (pageCount == 0) {
                    log.info("SORT1: " + sort);
                    pageCount = 1;
                    idForGenreSelection = byGenreServiceImpl.getAnimeIdForGenreSelection(selectedGenres, pageCount, sort);
                    if (idForGenreSelection.isBlank() || idForGenreSelection.isEmpty() || idForGenreSelection.equals("{\"data\":{\"Page\":{\"pageInfo\":{\"hasNextPage\":false},\"media\":[]}}}")) {
                        String genres = String.join(", ", selectedGenres);
                        prepareAndUpdateMessage(chatId, (int) messageId, "Masaka! There are no anime with such combination of genres: " + genres + "\n\n" + "try again \uD83D\uDC49 /by_genre");
                    }
                    temp = byGenreServiceImpl.getListOfAnimeID(idForGenreSelection);
                    id.addAll(temp);

                    pageCount++;
                    pageCountMap.put(chatId, pageCount);
                    listOfId.put(chatId, id);

                }
                if (decider <= 10) {
                    if (pageCount == 1) {
                        pageCount = 2;
                    }
                    log.info("SORT: " + sort);
                    idForGenreSelection = byGenreServiceImpl.getAnimeIdForGenreSelection(selectedGenres, pageCount, sort);
                    if (animeService.checkForNextPage(idForGenreSelection)) {
                        temp = byGenreServiceImpl.getListOfAnimeID(idForGenreSelection);
                        listOfId.get(chatId).addAll(temp);

                        pageCount++;
                        pageCountMap.put(chatId, pageCount);

                    }
                }

                log.info("ID: " + listOfId.get(chatId).size() + " " + listOfId);
                log.info("PAGE COUNT: " + pageCount);

                Random randomForID = new Random();
                List<Integer> tempId = listOfId.get(chatId);
                int index = randomForID.nextInt(tempId.size());
                int animeId = tempId.get(index);
                String unparsedAnime = animeService.getAnimeByID(animeId);
                String imgLink = animeService.extractImgLink(unparsedAnime);
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime, imgLink);
                executeMessage(watchlistService.addAnimeByRatingToWatchListButton(chatId, parsedAnime, animeId, "CONFIRM_GENRES"));
                log.info("Selected genre: " + selectedGenres);
            }

            if (callbackData.startsWith("ADD_ANIME_TO_WATCHLIST_BUTTON")) {

                animeId = Integer.valueOf(currentCallback.replace("ADD_ANIME_TO_WATCHLIST_BUTTON", ""));
                String titleName = update.getCallbackQuery().getMessage().getText();
                anime = animeService.extractAnimeTitle(titleName);

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
                String imgLink = animeService.extractImgLink(unparsedAnime);
                String parsedAnime = animeService.parseJSONAnime(unparsedAnime, imgLink);
                animeId = animeService.animeId;
                int animeId = animeService.getAnimeIdFromJSON(unparsedAnime);
                if (parsedAnime.equals("Nani?! Something went wrong... Repeat the operation.")) {
                    prepareAndSendMessage(chatId, parsedAnime);
                } else {
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
                    int animeId = Integer.parseInt(animeToGetDescrId.replace("DESCRIPTION", ""));
                    log.info("ID: " + animeId);
                    String rawDescription = animeService.getAnimeDescription(animeId);
                    int animeIdIndex = animeIdList.indexOf(animeId);
                    anime = animeList.get(animeIdIndex);
                    String imgLink = animeService.extractImgLink(rawDescription);
                    executeMessage(watchlistService.parseJSONDescription(chatId, rawDescription, messageId, imgLink));
                    log.info("Retrieved description:" + rawDescription);
                }

            }


        }


    }

    private void updateGenreListOnSelect(String callbackData, long chatId, int messageId) {
        ConcurrentHashMap<String, Boolean> genreOptions = byGenreServiceImpl.getGenreOption(chatId);
        if (genreOptions.keySet().stream().anyMatch(callbackData::contains)) {
            String genre = callbackData;
            boolean isSelected = genreOptions.get(genre);
            genreOptions.put(genre, !isSelected);
            executeMessage(genreService.updateGenreListOnSelect(chatId, messageId, genreOptions));
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
