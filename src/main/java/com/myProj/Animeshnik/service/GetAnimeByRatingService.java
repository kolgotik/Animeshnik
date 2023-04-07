package com.myProj.Animeshnik.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface GetAnimeByRatingService {
    String getAnimeByRating50();
    String getAnimeByRating50to60();
    String getAnimeByRating60to80();
    String getAnimeByRating80to100();
    SendMessage getAnimeByRatingOptions(long chatId);
}
