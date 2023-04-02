package com.myProj.Animeshnik.service;

import com.myProj.Animeshnik.model.User;

public interface AnimeDBService {
    void addAnimeToWatchlist(Long chatId, String animeTitle, Integer animeId);

    void removeAnimeFromWatchlist(User user, Integer animeTitleId, long chatId);
}
