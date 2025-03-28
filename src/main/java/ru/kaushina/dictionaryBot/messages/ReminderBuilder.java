package ru.kaushina.dictionaryBot.messages;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kaushina.dictionaryBot.model.Reminder;
import ru.kaushina.dictionaryBot.service.ReminderService;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.util.MessageTexts;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReminderBuilder implements IMessageBuilder {


    private final UserService userService;
    private final ReminderService reminderService;

    public ReminderBuilder(UserService userService, UserService userService1, ReminderService reminderService) {
        this.userService = userService1;
        this.reminderService = reminderService;
    }

    /**
     * Вспомогательный метод для создания нового сообщения и присваивания ему chatId получателя.
     * @param update Объект Update с обновлением
     * @return SendMessage - новое сообщение
     */
    public SendMessage setNewMessageChatId(Update update) {
        SendMessage sendMessage = new SendMessage();
        Long chatId = getChatId(update);
        sendMessage.setChatId(chatId);
        return sendMessage;
    }

    /**
     * Вспомогательный метод для нахождения chatId пользователя из текстового сообщения или callback-а.
     * @param update Объект Update с обновлением
     * @return Long - идентификатор чата с пользователем
     */
    public Long getChatId(Update update) {
        Long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else chatId = update.getMessage().getChatId();
        return chatId;
    }

    /**
     * Вспомогательный метод для создания инлайн-кнопки.
     * @param text текст кнопки
     * @param callbackData callback, присваиваемый кнопке
     * @return InlineKeyboardButton готовый объект кнопки
     */
    public InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(callbackData);
        return inlineKeyboardButton;
    }

    public SendMessage reminderMenu(Update update, Reminder reminder) {
        SendMessage message = setNewMessageChatId(update);
        message.setText(textReminderMenu(reminder));
        message.setReplyMarkup(markupReminderMenu(reminder));
        return message;
    }

    private EditMessageText setEditMessageChatId(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        return editMessageText;
    }

    public EditMessageText editReminderMenu(Update update, Reminder reminder) {
        EditMessageText message = setEditMessageChatId(update);
        message.setText(textReminderMenu(reminder));
        message.setReplyMarkup(markupReminderMenu(reminder));
        return message;
    }

    private String textReminderMenu(Reminder reminder) {
        String text = "";
        if (reminder != null) {
            String dayweeks = reminderService.getDays(reminder);
            String time = reminderService.getTime(reminder);
            String enabled = reminderService.isEnabled(reminder);
            text += MessageTexts.getMessage("message.reminder", dayweeks, time, enabled);
        }
        else {
            text += MessageTexts.getMessage("message.no_reminders");
        }
        return text;
    }

    private InlineKeyboardMarkup markupReminderMenu(Reminder reminder) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        if (reminder != null) {
            row.add(createButton(MessageTexts.getMessage("button.edit_reminder"), "REMINDER_EDIT"));
            row.add(createButton(MessageTexts.getMessage(reminder.isEnabled() ?
                    "button.off_reminder" : "button.on_reminder"), "REMINDER_ABLE"));
            row.add(createButton(MessageTexts.getMessage("button.delete_reminder"), "REMINDER_DELETE"));
        }
        else {
            row.add(createButton(MessageTexts.getMessage("button.create_reminder"), "REMINDER_CREATE"));
        }
        rowsInline.add(row);

        row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("button.go_home"), "HOME"));
        rowsInline.add(row);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }
}
