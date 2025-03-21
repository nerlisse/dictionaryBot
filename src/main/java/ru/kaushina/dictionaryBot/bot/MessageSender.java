package ru.kaushina.dictionaryBot.bot;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface MessageSender {
    Message executeMessage(SendMessage message) throws TelegramApiException;
    void executeEditMessageText(EditMessageText message) throws TelegramApiException;
    void executeCallbackAnswer(AnswerCallbackQuery callbackQuery) throws TelegramApiException;
}
