package com.myProj.Animeshnik.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myProj.Animeshnik.model.User;
import com.myProj.Animeshnik.model.UserRepository;
import com.myProj.Animeshnik.service.WatchlistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Component
@Service
@Slf4j
public class WatchlistServiceImpl implements WatchlistService {
    @Override
    public String formatAnimeList(List<String> watchlist) {

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < watchlist.size(); i++) {
            String animeTitle = watchlist.get(i);
            output.append("/");
            output.append(i + 1);
            output.append(" ");
            output.append(animeTitle);
            output.append("\n");
        }
        return output.toString();
    }

    @Override
    public SendMessage addAnimeToWatchListButton(long chatId, String anime, int animeId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var addAnimeToWatchlistButton = new InlineKeyboardButton();
        addAnimeToWatchlistButton.setText("Add anime to watchlist");
        String callbackData = "ADD_ANIME_TO_WATCHLIST_BUTTON" + animeId;
        addAnimeToWatchlistButton.setCallbackData(callbackData);

        rowInline.add(addAnimeToWatchlistButton);

        var rollButton = new InlineKeyboardButton();
        rollButton.setText("Continue rolling");
        rollButton.setCallbackData("/random");

        rowInline.add(rollButton);

        rowsInline.add(rowInline);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setText(anime);
        return sendMessage;
    }

    @Override
    public SendMessage addAnimeByRatingToWatchListButton(long chatId, String anime, int animeId, String ratingOption) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var addAnimeToWatchlistButton = new InlineKeyboardButton();
        addAnimeToWatchlistButton.setText("Add anime to watchlist");
        String callbackData = "ADD_ANIME_TO_WATCHLIST_BUTTON" + animeId;
        addAnimeToWatchlistButton.setCallbackData(callbackData);

        rowInline.add(addAnimeToWatchlistButton);

        var rollButton = new InlineKeyboardButton();
        rollButton.setText("Continue rolling");
        rollButton.setCallbackData(ratingOption);

        rowInline.add(rollButton);

        rowsInline.add(rowInline);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setText(anime);
        return sendMessage;
    }

    @Override
    public EditMessageText addAnimeToWatchListButton(long chatId, String anime, int animeId,int messageId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var addAnimeToWatchlistButton = new InlineKeyboardButton();
        addAnimeToWatchlistButton.setText("Add anime to watchlist");
        String callbackData = "ADD_ANIME_TO_WATCHLIST_BUTTON" + animeId;
        addAnimeToWatchlistButton.setCallbackData(callbackData);

        rowInline.add(addAnimeToWatchlistButton);

        rowsInline.add(rowInline);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        editMessageText.setText(anime);
        return editMessageText;

    }

    @Override
    public SendMessage animeList(long chatId, List<String> watchlist, User user, List<Integer> idList) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Your list:");

        watchlist = user.getAnimeList();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        idList = user.getAnimeIdList();



        for (Integer id : idList) {
            if (idList.contains(id)) {
                int indexOfAnime = idList.indexOf(id);
                String anime = watchlist.get(indexOfAnime);
                if (anime.getBytes().length > 64) {
                    anime = anime.substring(0, 61) + "...";
                }
                var animeTitleButton = new InlineKeyboardButton();
                animeTitleButton.setText(anime);
                keyboard.add(List.of(animeTitleButton));
                animeTitleButton.setCallbackData(String.valueOf(id));
            }
        }


        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        return message;
    }

    @Override
    public EditMessageText animeList(long chatId, List<String> watchlist, User user, long messageId, UserRepository userRepository) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setMessageId((int) messageId);
        message.setText("Your list:");

        try {
            user = userRepository.findById(chatId).orElseThrow(() -> new UserPrincipalNotFoundException("User not found"));
        } catch (UserPrincipalNotFoundException e) {
            log.error("no user found: " + e.getMessage());
        }

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        watchlist = user.getAnimeList();
        List<Integer> idList = user.getAnimeIdList();

        if (watchlist.isEmpty()) {
            message.setText("There are no anime in your list.");
        }

        for (Integer id : idList) {
            if (idList.contains(id)) {
                int indexOfAnime = idList.indexOf(id);
                if (watchlist.contains(watchlist.get(indexOfAnime))){
                    String anime = watchlist.get(indexOfAnime);
                    if (anime.getBytes().length > 64) {
                        anime = anime.substring(0, 61) + "...";
                    }
                    var animeTitleButton = new InlineKeyboardButton();
                    animeTitleButton.setText(anime);
                    keyboard.add(List.of(animeTitleButton));
                    animeTitleButton.setCallbackData(String.valueOf(id));
                }

            }
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        return message;
    }

    @Override
    public EditMessageText animeDetails(long chatId, String anime, Integer animeId, int messageId) {
        EditMessageText message = new EditMessageText();
        if (anime.getBytes().length > 64) {
            anime = anime.substring(0, 61) + "...";
        }
        message.setChatId(String.valueOf(chatId));
        message.setText("Choose an option for anime: " + anime);
        message.setMessageId(messageId);

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        var descriptionButton = new InlineKeyboardButton();
        descriptionButton.setText("Anime Info");
        String desc = "DESCRIPTION";
        descriptionButton.setCallbackData(desc + animeId); // URL-encode the anime variable
        row1.add(descriptionButton);

        var removeButton = new InlineKeyboardButton();
        removeButton.setText("Remove");
        String remove = "REMOVE";
        removeButton.setCallbackData(remove + animeId); // URL-encode the anime variable
        row1.add(removeButton);

        var backButton = new InlineKeyboardButton();
        backButton.setText("Back to list");
        backButton.setCallbackData("BACK_TO_LIST"); // This value does not need to be URL-encoded
        row1.add(backButton);

        buttons.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();


        buttons.add(row2);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(buttons);

        message.setReplyMarkup(markup);
        return message;
    }

    public EditMessageText parseJSONDescription(long chatId, String anime, long messageId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId((int) messageId);

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        var backButton = new InlineKeyboardButton();
        backButton.setText("Back to list");
        backButton.setCallbackData("BACK_TO_LIST");


        var backToOptionsButton = new InlineKeyboardButton();
        backToOptionsButton.setText("Back to options");
        backToOptionsButton.setCallbackData("BACK_TO_OPTIONS");

        buttons.add(backButton);
        buttons.add(backToOptionsButton);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        AnimeServiceImpl animeService = new AnimeServiceImpl();

        int id;
        String description;
        String title;
        int episodes;
        String result = "";
        String averageScore;
        String genres;
        String startDate;
        String endDate;

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(anime);
            JsonNode mediaNode = rootNode.path("data").path("Media");
            id = mediaNode.path("id").asInt();

            description = mediaNode.path("description").asText()
                    .replaceAll("<br>", "")
                    .replaceAll("<i>", "")
                    .replaceAll("</i>", "")
                    .replaceAll("</br>", "")
                    .replaceAll("<b>", "")
                    .replaceAll("</b>", "")
                    .replaceAll("<a href=\"", "")
                    .replaceAll("\">", "")
                    .replaceAll("&rsquo;", "")
                    .replaceAll("&ldquo;", "")
                    .replaceAll("&rdquo;", "")
                    .replaceAll("&amp;", "")
                    .replaceAll("<\\/I>", "")
                    .replaceAll("</a>", "");

            averageScore = String.valueOf(mediaNode.path("averageScore").asInt());
            episodes = mediaNode.path("episodes").asInt();

            JsonNode titleNode = mediaNode.path("title");

            endDate = animeService.formatDate(mediaNode.path("endDate"));
            startDate = animeService.formatDate(mediaNode.path("startDate"));


            if (titleNode.hasNonNull("english")) {
                title = mediaNode.path("title").path("english").asText();
            } else {
                title = mediaNode.path("title").path("romaji").asText();
            }
            if (!mediaNode.hasNonNull("description")) {
                description = "Description is not available";
            }
            if (!mediaNode.hasNonNull("genres") || mediaNode.path("genres").isEmpty() || mediaNode.path("genres").isNull()) {
                genres = "Genres are not available";
            } else {
                JsonNode genresNode = mediaNode.path("genres");
                StringBuilder stringBuilder = new StringBuilder();
                for (JsonNode genreNode : genresNode) {
                    stringBuilder.append(genreNode.asText());
                    stringBuilder.append(", ");
                }
                genres = stringBuilder.toString().replaceAll(", $", "");
            }
            if (mediaNode.hasNonNull("averageScore")) {
                averageScore = averageScore + " / 100";
            } else {
                averageScore = "Score is not available";

            }

            result = "Anime title: " + title + "\n"
                    + "\n" + "Genres: " + genres + "\n"
                    + "\n" + "Start Date: " + startDate + "\n"
                    + "\n" + "End Date: " + endDate + "\n"
                    + "\n" + "Average Score: " + averageScore + "\n"
                    + "\n" + "Episodes: " + episodes + "\n"
                    + "\n"
                    + "Description: " + description;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            result = "Nani?! Something went wrong... Repeat the operation.";
            log.error("Error occurred on parsing API response: " + e.getMessage());
        }


        editMessageText.setText(result);
        markup.setKeyboard(List.of(buttons));

        editMessageText.setReplyMarkup(markup);
        return editMessageText;
    }


    @Override
    public EditMessageText addYesNoButton(long chatId, String anime, long messageId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setText("Are sure you want to disintegrate " + anime + " from your list?");
        editMessageText.setMessageId((int) messageId);

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData("YES");

        var noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData("NO");

        buttons.add(yesButton);
        buttons.add(noButton);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(buttons));
        editMessageText.setReplyMarkup(markup);

        return editMessageText;
    }
}
