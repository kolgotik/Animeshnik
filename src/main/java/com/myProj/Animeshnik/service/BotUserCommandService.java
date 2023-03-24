package com.myProj.Animeshnik.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public interface BotUserCommandService {
    List<BotCommand> getListOfCommands();

    void handleStart(Message message);

    void handleKeyboard(Message message);

    void handleRandomAnime(Message message);

    void handleWatchlist(Message message);

    void handleUnsupportedCommand(Message message);
}
