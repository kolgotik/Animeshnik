package com.myProj.Animeshnik.serviceImpl;

import com.myProj.Animeshnik.service.AnimeService;
import com.myProj.Animeshnik.service.BotUserCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.ArrayList;
import java.util.List;

public class BotUserCommandServiceImpl implements BotUserCommandService {

    @Autowired
    private AnimeService animeService;
    @Override
    public List<BotCommand> getListOfCommands() {
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "start the bot"));
        botCommandList.add(new BotCommand("/keyboard", "activates command keyboard"));
        botCommandList.add(new BotCommand("/random", "get random anime"));
        botCommandList.add(new BotCommand("/by_genre", "get anime by genre"));
        botCommandList.add(new BotCommand("/by_rating", "get anime by rating"));
        botCommandList.add(new BotCommand("/watchlist", "get anime added to your watchlist"));
        botCommandList.add(new BotCommand("/help", "info on how to use this bot"));
        botCommandList.add(new BotCommand("/settings", "set custom settings"));
        return botCommandList;
    }

    @Override
    public void handleStart(Message message) {

    }

    @Override
    public void handleKeyboard(Message message) {

    }

    @Override
    public void handleRandomAnime(Message message) {

    }

    @Override
    public void handleWatchlist(Message message) {

    }

    @Override
    public void handleUnsupportedCommand(Message message) {

    }
}
