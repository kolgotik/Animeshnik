package com.myProj.Animeshnik.serviceImpl;

import com.myProj.Animeshnik.service.BotCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

@Service
@Slf4j
public class BotCommandServiceImpl implements BotCommandService {
    @Override
    public EditMessageText updateMessageText(long chatId, int messageId, String updatedText) {

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId(messageId);
        editMessageText.setText(updatedText);

        return editMessageText;
    }
}
