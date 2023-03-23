package com.myProj.Animeshnik.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class VirtualKeyboardServiceImpl implements VirtualKeyboardService{
    @Override
    public void sendGeneralVirtualCommandKeyboardWithMessage(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add("/random");
        row.add("/by_genre");
        row.add("/by_rating");

        rowList.add(row);

        row = new KeyboardRow();

        row.add("/watchlist");

        rowList.add(row);

        replyKeyboardMarkup.setKeyboard(rowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
    }
}
