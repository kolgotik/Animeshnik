package com.myProj.Animeshnik.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface VirtualKeyboardService {
    void sendGeneralVirtualCommandKeyboardWithMessage(SendMessage sendMessage);
}
