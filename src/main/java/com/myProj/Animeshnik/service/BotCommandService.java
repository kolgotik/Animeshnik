package com.myProj.Animeshnik.service;

import com.myProj.Animeshnik.model.UserRepository;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface BotCommandService {

    EditMessageText updateMessageText(long chatId, int messageId, String updatedText);

    void registerUser(Message message, UserRepository userRepository);
}
