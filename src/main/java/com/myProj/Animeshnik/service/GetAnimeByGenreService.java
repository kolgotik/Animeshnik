package com.myProj.Animeshnik.service;

import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface GetAnimeByGenreService {

    String getAnimeByGenre();

    EditMessageText updateGenreListOnSelect(long chatId, int messageId, Map<String, Boolean> updatedGenreList);

    SendMessage sendGenreSelection(long chatId);
}
