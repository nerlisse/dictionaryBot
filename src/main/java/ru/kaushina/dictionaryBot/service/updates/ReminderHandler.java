package ru.kaushina.dictionaryBot.service.updates;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kaushina.dictionaryBot.bot.MessageSender;
import ru.kaushina.dictionaryBot.messages.ReminderBuilder;
import ru.kaushina.dictionaryBot.model.Reminder;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.service.consumer.CheckedConsumer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ReminderHandler {

    @Setter
    private MessageSender messageSender; // бот

    /**Словарь callback-ов и соответствующим их обработчиков */
    private final Map<String, CheckedConsumer<Update>> callbackHandlers;
    private final MessageHandler messageHandler;
    private final ReminderBuilder reminderBuilder;
    private final UserService userService;

    public ReminderHandler(MessageHandler messageHandler, ReminderBuilder reminderBuilder, UserService userService) {
        this.userService = userService;
        this.callbackHandlers = new HashMap<>();
        callbackHandlers.put("REMINDER", this::reminderMenuHandler);
        callbackHandlers.put("REMINDER_CREATE", this::reminderCreateHandler);
        callbackHandlers.put("REMINDER_EDIT", this::reminderEditHandler);
        callbackHandlers.put("REMINDER_ABLE", this::reminderToggleHandler);
        callbackHandlers.put("REMINDER_DELETE", this::reminderDeleteHandler);
        this.messageHandler = messageHandler;
        this.reminderBuilder = reminderBuilder;
    }

    public void handleUpdate(Update update) throws TelegramApiException {
        String callback = update.getCallbackQuery().getData();
        CheckedConsumer<Update> handler = callbackHandlers.get(callback);
        handler.accept(update);
    }

    private void reminderMenuHandler(Update update) throws TelegramApiException {
        Reminder reminder = messageHandler.getReminderMenu(update);
        SendMessage message = reminderBuilder.reminderMenu(update, reminder);
        executeNewMessage(message);
    }

    /**
     * Метод для отправки новых сообщений и присваивания последнего сообщения пользователю.
     * @param sendMessage сообщение для отправки
     * @throws TelegramApiException при ошибке отправки
     */
    private void executeNewMessage(SendMessage sendMessage) throws TelegramApiException {
        Message sentMessage = messageSender.executeMessage(sendMessage);
        userService.setLastMessageId(sentMessage.getChatId(), sentMessage.getMessageId());
    }

    /**
     * Отправка редактированного сообщения.
     * @param editMessage редактированное сообщение
     * @throws TelegramApiException при ошибке отправки
     */
    private void executeEditMessage(EditMessageText editMessage) throws TelegramApiException {
        messageSender.executeEditMessageText(editMessage);
    }


    private void reminderDeleteHandler(Update update) throws TelegramApiException {
        Reminder reminder = messageHandler.deleteReminder(update);
        EditMessageText editMessageText = reminderBuilder.editReminderMenu(update, reminder);
        executeEditMessage(editMessageText);
    }

    private void reminderToggleHandler(Update update) {
    }

    private void reminderEditHandler(Update update) {
    }

    private void reminderCreateHandler(Update update) {
    }


}
