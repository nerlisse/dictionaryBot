package ru.kaushina.dictionaryBot.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public interface IMessageBuilder {
    SendMessage setNewMessageChatId(Update update);
    Long getChatId(Update update);
    InlineKeyboardButton createButton(String text, String callbackData);

}
