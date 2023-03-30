package com.myProj.Animeshnik.service;

import com.myProj.Animeshnik.model.User;

public interface AnimeDBService {
    void addAnimeToWatchlist(Long chatId, String animeTitle);

    void removeAnimeFromWatchlist(User user, String animeTitle, long chatId);
}
