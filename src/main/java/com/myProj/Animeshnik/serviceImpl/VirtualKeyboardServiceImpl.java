package com.myProj.Animeshnik.serviceImpl;

import com.myProj.Animeshnik.service.VirtualKeyboardService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class VirtualKeyboardServiceImpl implements VirtualKeyboardService {
    @Override
    public void sendGeneralVirtualCommandKeyboardWithMessage(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add("\uD83C\uDFB2 random");
        row.add("\uD83C\uDFAD by genre");
        row.add("⭐ by rating");

        rowList.add(row);

        row = new KeyboardRow();

        row.add("\uD83D\uDCDD watchlist");

        rowList.add(row);

        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
    }

    @Override
    public SendMessage sendMessageWithVirtualKeyboard(long chatId, String textToSend, VirtualKeyboardService virtualKeyboardService) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        virtualKeyboardService.sendGeneralVirtualCommandKeyboardWithMessage(sendMessage);
        return sendMessage;
    }

    @Override
    public SendMessage sendMessageNoVirtualKeyboard(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        return sendMessage;
    }
}
