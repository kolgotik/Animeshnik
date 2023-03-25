package com.myProj.Animeshnik.service;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public interface BotCommandService {

    EditMessageText updateMessageText(long chatId, int messageId, String updatedText);

}
