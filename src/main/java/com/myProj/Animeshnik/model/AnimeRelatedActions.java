package com.myProj.Animeshnik.model;

public interface AnimeRelatedActions {
    void addAnimeToWatchlist(Long chatId, Anime animeTitle);

    void addAnimeToWatchlist(Long chatId, String animeTitle);
}
