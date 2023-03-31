package com.myProj.Animeshnik.service;

import com.myProj.Animeshnik.model.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.List;

public interface WatchlistService {
    String formatAnimeList(List<String> watchlist);

    SendMessage addAnimeToWatchListButton(long chatId, String anime);

    SendMessage animeList(long chatId, List<String> watchlist, User user);

    EditMessageText animeList(long chatId, List<String> watchlist, User user, long messageId);

    EditMessageText animeDetails(long chatId, String anime, int messageId);

    EditMessageText parseJSONDescription(long chatId, String anime, long messageId);

    EditMessageText addYesNoButton(long chatId, String anime, long messageId);
}
