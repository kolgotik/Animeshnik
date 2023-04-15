package com.myProj.Animeshnik.serviceImpl;

import com.myProj.Animeshnik.service.GetAnimeByGenreService;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Setter
public class GetAnimeByGenreServiceImpl implements GetAnimeByGenreService {

    ConcurrentHashMap<Long, ConcurrentHashMap<String, Boolean>> userGenreOptions = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, Boolean> getGenreOption(long chatId) {
        return userGenreOptions.computeIfAbsent(chatId, k -> {
            ConcurrentHashMap<String, Boolean> genreOptions = new ConcurrentHashMap<>();
            genreOptions.put("Action", false);
            genreOptions.put("Adventure", false);
            genreOptions.put("Comedy", false);
            genreOptions.put("Drama", false);
            genreOptions.put("Ecchi", false);
            genreOptions.put("Fantasy", false);
            genreOptions.put("Harem", false);
            genreOptions.put("Historical", false);
            genreOptions.put("Horror", false);
            genreOptions.put("Mecha", false);
            genreOptions.put("Music", false);
            genreOptions.put("Mystery", false);
            genreOptions.put("Psychological", false);
            genreOptions.put("Romance", false);
            genreOptions.put("School", false);
            genreOptions.put("Sci-Fi", false);
            genreOptions.put("Slice of Life", false);
            genreOptions.put("Sports", false);
            genreOptions.put("Supernatural", false);
            genreOptions.put("Thriller", false);
            return genreOptions;
        });
    }

    @Override
    public String getAnimeByGenre() {
        return null;
    }

    @Override
    public EditMessageText updateGenreListOnSelect(long chatId, int messageId, Map<String, Boolean> updatedGenreList) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setMessageId(messageId);
        message.setText("Select genres:");

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (String genre : updatedGenreList.keySet()) {
            if (updatedGenreList.get(genre)) {
                var genreButton = new InlineKeyboardButton();
                genreButton.setText("✅ " + genre);
                genreButton.setCallbackData(genre);
                keyboard.add(List.of(genreButton));
            } else {
                var genreButton = new InlineKeyboardButton();
                genreButton.setText(genre);
                genreButton.setCallbackData(genre);
                keyboard.add(List.of(genreButton));
            }
        }
        if (updatedGenreList.containsValue(true)){
            var confirmButton = new InlineKeyboardButton();
            confirmButton.setText("Confirm");
            confirmButton.setCallbackData("CONFIRM_GENRES");
            keyboard.add(List.of(confirmButton));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        userGenreOptions.put(chatId, new ConcurrentHashMap<>(updatedGenreList));

        return message;
    }

    @Override
    public SendMessage sendGenreSelection(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select genres:");

        Map<String, Boolean> genresList = getGenreOption(chatId);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (String genre : genresList.keySet()) {
            var genreButton = new InlineKeyboardButton();
            genreButton.setText(genre);
            genreButton.setCallbackData(genre);
            keyboard.add(List.of(genreButton));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        return message;
    }
}
