package com.myProj.Animeshnik.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.List;
import java.util.Map;

public interface GetAnimeByGenreService {

    String getAnimeIdForGenreSelection(List<String> selectedGenres, int page, String sort);

    List<Integer> getListOfAnimeID(String response);

    EditMessageText updateGenreListOnSelect(long chatId, int messageId, Map<String, Boolean> updatedGenreList);

    SendMessage sendGenreSelection(long chatId);
}
