package com.myProj.Animeshnik.serviceImpl;

import com.myProj.Animeshnik.service.WatchlistService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Service
public class WatchlistServiceImpl implements WatchlistService {
    @Override
    public String formatAnimeList(List<String> watchlist) {

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < watchlist.size(); i++) {
            String animeTitle = watchlist.get(i);
            output.append("/");
            output.append(i+1);
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
}
