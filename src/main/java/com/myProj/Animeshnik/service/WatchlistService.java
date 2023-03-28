package com.myProj.Animeshnik.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

public interface WatchlistService {
    String formatAnimeList(List<String> watchlist);

    SendMessage addAnimeToWatchListButton(long chatId, String anime);
}
