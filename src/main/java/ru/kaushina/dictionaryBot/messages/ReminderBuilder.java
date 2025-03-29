package ru.kaushina.dictionaryBot.messages;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kaushina.dictionaryBot.model.Reminder;
import ru.kaushina.dictionaryBot.service.ReminderService;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.util.MessageTexts;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReminderBuilder implements IMessageBuilder {

    private final ReminderService reminderService;

    public ReminderBuilder(ReminderService reminderService) {
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

    /**
     * Составление сообщения меню напоминаний (при нажатии на кнопку Напоминания).
     * @param update Объект Update с обновлением
     * @param reminder напоминание пользователя, {@code null} если не создано
     * @return SendMessage - новое сообщение с меню напоминаний
     */
    public SendMessage reminderMenu(Update update, Reminder reminder) {
        SendMessage message = setNewMessageChatId(update);
        message.setText(textReminderMenu(reminder, getChatId(update), update));
        message.setReplyMarkup(markupReminderMenu(reminder));
        return message;
    }

    /**
     * Вспомогательный метод для создания редактированного сообщения и присваивания ему айди чата и сообщения.
     * @param update Объект Update с обновлением
     * @return EditMessageText редактированное сообщение с необходимыми полями
     */
    private EditMessageText setEditMessageChatId(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        return editMessageText;
    }

    /**
     * Показывает меню напоминаний в предыдущем сообщении.
     * @param update Объект Update с обновлением
     * @param reminder напоминание пользователя, {@code null} если не создано
     * @return EditMessageText редактированное сообщение с необходимыми полями
     */
    public EditMessageText editReminderMenu(Update update, Reminder reminder) {
        EditMessageText message = setEditMessageChatId(update);
        message.setText(textReminderMenu(reminder, getChatId(update), update));
        message.setReplyMarkup(markupReminderMenu(reminder));
        return message;
    }

    /**
     * Составление текста сообщения с меню напоминаний.
     * @param reminder напоминание пользователя, {@code null} если не создано
     * @param chatId идентификатор пользователя
     * @param update Объект Update с обновлением
     * @return String - текст сообщения
     */
    private String textReminderMenu(Reminder reminder, Long chatId, Update update) {
        String text = "";
        if (update.hasMessage() && !reminderService.checkValidTime(update.getMessage().getText())) {
            text += MessageTexts.getMessage("message.failed_reminder");
        }
        if (reminder != null) {
            String dayweeks = reminderService.getDays(reminder, chatId);
            String time = reminderService.getTime(reminder);
            String enabled = reminderService.isEnabled(reminder);
            text += MessageTexts.getMessage("message.reminder", dayweeks, time, enabled);
        }
        else {
            text += MessageTexts.getMessage("message.no_reminders");
        }
        return text;
    }

    /**
     * Создает кнопки-ответы для меню напоминаний.
     * @param reminder напоминание пользователя, {@code null} если не создано
     * @return InlineKeyboardMarkup - разметка кнопок
     */
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

    /**
     * Редактирует сообщение с выбором дней недели при создании напоминания.
     * @param update Объект Update с обновлением
     * @param reminder напоминание пользователя, {@code null} если не создано
     * @return EditMessageText - редактированное сообщение
     */
    public EditMessageText weekDaysAdder(Update update, Reminder reminder) {
        EditMessageText editMessageText = setEditMessageChatId(update);
        editMessageText.setText(textWeekDaysAdder(reminder, getChatId(update)));
        editMessageText.setReplyMarkup(markupWeekDaysAdder(reminder, getChatId(update)));
        return editMessageText;
    }

    /**
     * Создает текст сообщения с выбором дней недели при создании напоминания.
     * @param reminder напоминание пользователя, {@code null} если не создано
     * @param chatId идентификатор пользователя
     * @return String - текст сообщения
     */
    private String textWeekDaysAdder(Reminder reminder, Long chatId) {
        return MessageTexts.getMessage("message.days_adder", reminderService.getDays(reminder, chatId));

    }

    /**
     * Создает разметку кнопок (ответы на сообщение) для выбора дней недели при создании напоминания.
     * @param reminder напоминание пользователя, {@code null} если не создано
     * @param chatId идентификатор пользователя
     * @return InlineKeyboardMarkup - разметка кнопок
     */
    private InlineKeyboardMarkup markupWeekDaysAdder(Reminder reminder, Long chatId) {
        List<String> days = reminderService.getDaysList(reminder, chatId);
        if (days==null) days = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("button.weekday_monday",
                days.contains("Пн") ? "✅" : ""), "REMINDER_MONDAY"));
        row.add(createButton(MessageTexts.getMessage("button.weekday_tuesday",
                days.contains("Вт") ? "✅" : ""), "REMINDER_TUESDAY"));
        rowsInline.add(row);

        row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("button.weekday_wednesday",
                days.contains("Ср") ? "✅" : ""), "REMINDER_WEDNESDAY"));
        row.add(createButton(MessageTexts.getMessage("button.weekday_thursday",
                days.contains("Чт") ? "✅" : ""), "REMINDER_THURSDAY"));
        rowsInline.add(row);

        row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("button.weekday_friday",
                days.contains("Пт") ? "✅" : ""), "REMINDER_FRIDAY"));
        row.add(createButton(MessageTexts.getMessage("button.weekday_saturday",
                days.contains("Сб") ? "✅" : ""), "REMINDER_SATURDAY"));
        rowsInline.add(row);

        row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("button.weekday_sunday",
                days.contains("Вс") ? "✅" : ""), "REMINDER_SUNDAY"));
        rowsInline.add(row);

        row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("button.weekday_done"), "REMINDER_DAYDONE"));
        rowsInline.add(row);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }

    /**
     * Создает сообщение, приглашающее к вводу времени для напоминания.
     * @param update Объект Update с обновлением
     * @return SendMessage - сообщение с приглашением к вводу
     */
    public SendMessage askToEnterTime(Update update) {
        SendMessage message = setNewMessageChatId(update);
        message.setText(MessageTexts.getMessage("message.enter_time"));
        return message;
    }

}
