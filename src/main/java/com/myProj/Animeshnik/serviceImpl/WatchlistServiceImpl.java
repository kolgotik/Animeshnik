package com.myProj.Animeshnik.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myProj.Animeshnik.model.User;
import com.myProj.Animeshnik.service.WatchlistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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
    public SendMessage addAnimeToWatchListButton(long chatId, String anime) {
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
        return sendMessage;
    }

    @Override
    public EditMessageText addAnimeToWatchListButton(long chatId, String anime, int messageId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var addAnimeToWatchlistButton = new InlineKeyboardButton();
        addAnimeToWatchlistButton.setText("Add anime to watchlist");
        addAnimeToWatchlistButton.setCallbackData("ADD_ANIME_TO_WATCHLIST_BUTTON");

        rowInline.add(addAnimeToWatchlistButton);

        rowsInline.add(rowInline);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        editMessageText.setText(anime);
        return editMessageText;

    }

    @Override
    public SendMessage animeList(long chatId, List<String> watchlist, User user) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Your list:");

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (String anime : watchlist) {
            var animeTitleButton = new InlineKeyboardButton();
            animeTitleButton.setText(anime);
            animeTitleButton.setCallbackData(anime);
            keyboard.add(List.of(animeTitleButton));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        return message;
    }

    @Override
    public EditMessageText animeList(long chatId, List<String> watchlist, User user, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setMessageId((int) messageId);
        message.setText("Your list:");

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        watchlist = user.getAnimeList();

        if (user.getAnimeList().isEmpty()) {
            message.setText("There are no anime in your list.");
        }

        for (String anime : watchlist) {
            var animeTitleButton = new InlineKeyboardButton();
            animeTitleButton.setText(anime);
            animeTitleButton.setCallbackData(anime);
            keyboard.add(List.of(animeTitleButton));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        return message;
    }

    @Override
    public EditMessageText animeDetails(long chatId, String anime, Integer animeId, int messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText("Choose an option for anime: " + anime);
        message.setMessageId(messageId);

        List<InlineKeyboardButton> buttons = new ArrayList<>();

        var descriptionButton = new InlineKeyboardButton();
        descriptionButton.setText("Description");
        String desc = "DESCRIPTION";
        descriptionButton.setCallbackData(desc + animeId); // URL-encode the anime variable
        buttons.add(descriptionButton);

        var removeButton = new InlineKeyboardButton();
        removeButton.setText("Remove");
        String remove = "REMOVE";
        removeButton.setCallbackData(remove + animeId); // URL-encode the anime variable
        buttons.add(removeButton);

        var backButton = new InlineKeyboardButton();
        backButton.setText("Back to list");
        backButton.setCallbackData("BACK_TO_LIST"); // This value does not need to be URL-encoded
        buttons.add(backButton);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(buttons));

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

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        String description = null;
        try {
            jsonNode = objectMapper.readTree(anime);

            description = jsonNode.get("data").get("Media").get("description").asText()
                    .replaceAll("<br>", "")
                    .replaceAll("<i>", "")
                    .replaceAll("</i>", "")
                    .replaceAll("</br>", "")
                    .replaceAll("<b>", "")
                    .replaceAll("</b>", "")
                    .replaceAll("<a href=\"", "")
                    .replaceAll("\">", "")
                    .replaceAll("</a>", "");


        } catch (JsonProcessingException e) {
            log.error("Error occurred during parsing JSON Description " +
                    "Place: WatchlistServiceImpl method: parseJSONDescription" + e.getMessage());
        }
        if (description.equals("null")) {
            description = "No description available";
            editMessageText.setText(description);
            markup.setKeyboard(List.of(buttons));

            editMessageText.setReplyMarkup(markup);
        } else{
            editMessageText.setText(description);
            markup.setKeyboard(List.of(buttons));

            editMessageText.setReplyMarkup(markup);
        }


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
