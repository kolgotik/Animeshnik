package com.myProj.Animeshnik.service;

import com.myProj.Animeshnik.model.User;
import com.myProj.Animeshnik.model.UserRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.List;

public interface WatchlistService {
    String formatAnimeList(List<String> watchlist);

    SendPhoto addAnimeToWatchListWithImgButton(long chatId, String anime, int animeId, String imgLink);

    SendMessage addAnimeToWatchListButton(long chatId, String anime, int animeId);

    SendMessage addAnimeByRatingToWatchListButton(long chatId, String anime, int animeId, String ratingOption);
    EditMessageText addAnimeToWatchListButton(long chatId, String anime, int animeId ,int messageId);

    SendPhoto animeListWithImg(long chatId, List<String> watchlist, User user, List<Integer> idList);

    SendMessage animeList(long chatId, List<String> watchlist, User user, List<Integer> idList);

    EditMessageText animeList(long chatId, List<String> watchlist, User user, long messageId, UserRepository userRepository);

    EditMessageText animeDetails(long chatId, String anime,  Integer animeId ,int messageId);

    EditMessageText parseJSONDescription(long chatId, String anime, long messageId, String imgLink);

    EditMessageText addYesNoButton(long chatId, String anime, long messageId);
}
