package ru.kaushina.dictionaryBot.service.updates;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kaushina.dictionaryBot.bot.MessageSender;
import ru.kaushina.dictionaryBot.handlers.MessageBuilder;
import ru.kaushina.dictionaryBot.handlers.MessageHandler;
import ru.kaushina.dictionaryBot.util.MessageTexts;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.enums.ShowMode;
import ru.kaushina.dictionaryBot.service.TrainingSessionService;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.service.consumer.CheckedConsumer;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис обработки callback-ов от пользователя.
 */
@Slf4j
@Service
public class CallbackQueryHandler {
    @Autowired
    private MessageBuilder messageBuilder;

    @Autowired
    private MessageHandler messageHandler;
    @Setter
    private MessageSender messageSender; // бот

    @Autowired
    private UserService userService;

    /**Словарь callback-ов и соответствующим их обработчиков */
    private final Map<String, CheckedConsumer<Update>> callbackHandlers;
    public CallbackQueryHandler() {

        this.callbackHandlers = new HashMap<>();
        callbackHandlers.put("HOME", this::homeCallbackHandler);
        callbackHandlers.put("CREATE NEW FOLDER", this::createFolderCallbackHandler);
        callbackHandlers.put("DELETE FOLDER", this::deleteFolderCallbackHandler);
        callbackHandlers.put("SHOW FOLDER", this::showFolderCallbackHandler);
        callbackHandlers.put("ADD WORD", this::addWordCallbackHandler);
        callbackHandlers.put("SHOW WORDS", this::showWordsCallbackHandler);
        callbackHandlers.put("DELETE WORD", this::deleteWordCallbackHandler);
        callbackHandlers.put("REMEMBER MODE", this::startRememberModeCallbackHandler);
        callbackHandlers.put("END PLAY", this::endPlayModeCallbackHandler);
        callbackHandlers.put("SHOW ANSWER", this::changeAnswerVisibilityCallbackHandler);
        callbackHandlers.put("HIDE ANSWER", this::changeAnswerVisibilityCallbackHandler);
        callbackHandlers.put("REMEMBER", this::answerRememberModeHandler);
        callbackHandlers.put("DO NOT REMEMBER", this::answerRememberModeHandler);
        callbackHandlers.put("TEST MODE", this::startTestModeCallbackHandler);
        callbackHandlers.put("SETTINGS", this::settingsCallbackHandler);
        callbackHandlers.put("SHOW KEY", this::settingsCallbackHandler);
        callbackHandlers.put("SHOW VALUE", this::settingsCallbackHandler);
    }

    /**
     * Отправка редактированного сообщения.
     * @param editMessage редактированное сообщение
     * @throws TelegramApiException при ошибке отправки
     */
    private void executeEditMessage(EditMessageText editMessage) throws TelegramApiException {
        messageSender.executeEditMessageText(editMessage);
    }

    /**
     * Редактирование неактуального (не последнего сообщения) на недоступность.
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void executeFailedEditMessage(Update update) throws TelegramApiException {
        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(update.getCallbackQuery().getMessage().getChatId());
        messageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        messageText.setText(MessageTexts.getMessage("message.unavailable"));
        messageSender.executeEditMessageText(messageText);
    }

    /**
     * Обработчик получения callback-а и передача соответствующему методу.
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    public void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        log.info("Received callback {} from user {}", callbackData, chatId);

        User user = userService.findByChatId(chatId);
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        if (user == null || user.getLastMessageId() == null || !user.getLastMessageId().equals(messageId)) {
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            answer.setShowAlert(false);
            messageSender.executeCallbackAnswer(answer);

            executeFailedEditMessage(update);
            return;
        }

        String callback = callbackData.split("_")[0];
        CheckedConsumer<Update> handler = callbackHandlers.get(callback);

        if (handler != null) {
            handler.accept(update);
        }
        else {
            log.warn("No handler found for callback: {}", callbackData);
        }
        //answer for callback (for showing callback is answered)
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        answer.setShowAlert(false);
        messageSender.executeCallbackAnswer(answer);

    }

    /**
     * Обработчик попадания на главный экран и вывод соответствующего сообщения.
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void homeCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.homeHandler(update);
        SendMessage sendMessage = messageBuilder.getHomeMessage(update); //build a message
        executeNewMessage(sendMessage); //execute the message
    }

    /**
     * Обработчик начала создания папки (нажата кнопка "создать новую папку").
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void createFolderCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.createFolderHandler(update);
        SendMessage sendMessage = messageBuilder.createFolderMessage(update);
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик удаления папки (нажата кнопка "удалить папку").
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void deleteFolderCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.deleteFolderHandler(update);
        SendMessage sendMessage = messageBuilder.getHomeMessage(update);
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик показа меню папки (нажата кнопка какой-то папки).
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void showFolderCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.showFolderHandler(update);
        SendMessage sendMessage = messageBuilder.folderShowMessage(update);
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик начала добавления слова (нажата кнопка "добавить слово").
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void addWordCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.askToAddWordHandler(update);
        SendMessage sendMessage = messageBuilder.addWordMessage(update);
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик показа слов в папке (нажата кнопка "показать все слова").
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void showWordsCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.showWordsHandler(update);
        SendMessage sendMessage = messageBuilder.showWordsMessage(update);
        executeNewMessage(sendMessage);

        sendMessage = messageBuilder.folderShowMessage(update); //send home message
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик начала удаления слова из папки (нажата кнопка "удалить папку").
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void deleteWordCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.askToDeleteWordHandler(update);
        SendMessage sendMessage = messageBuilder.deleteWordMessage(update);
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик начала режима запоминания (нажата кнопка "режим запоминания").
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void startRememberModeCallbackHandler(Update update) throws TelegramApiException {
        TrainingSessionService.TrainingSession started = messageHandler.startPlayModeHandler(update);
        SendMessage sendMessage;
        if (started != null) {
            sendMessage = messageBuilder.startRememberModeMessage(update, started);
        }
        else {
            sendMessage = messageBuilder.failedPlayModeMessage(update);
            executeNewMessage(sendMessage);
            sendMessage = messageBuilder.folderShowMessage(update);
        }
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик досрочного завершения игрового режима (нажата кнопку "закончить игру").
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void endPlayModeCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.endPlayModeHandler(update);
        SendMessage sendMessage = messageBuilder.folderShowMessage(update);
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик получения callback- на недоступную сессию
     * (нажата любая из кнопок игрового режима для недоступной сессии).
     * @param update
     * @throws TelegramApiException
     */
    private void failedCallbackSessionHandler(Update update) throws TelegramApiException {
        messageHandler.endPlayModeHandler(update);
        EditMessageText message = messageBuilder.failedSessionMessage(update);
        executeEditMessage(message);
        //System.out.println("failed session");
        SendMessage sendMessage = messageBuilder.folderShowMessage(update);
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик ответа на режим запоминания (нажата кнопка "показать ответ" в режиме запоминания").
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void changeAnswerVisibilityCallbackHandler(Update update) throws TelegramApiException {
        TrainingSessionService.TrainingSession session = messageHandler.changeAnswerHandler(update);
        if (session != null) {
            EditMessageText message = messageBuilder.showRememberModeMessage(update, session);
            executeEditMessage(message);
        }
        else {
            failedCallbackSessionHandler(update);
        }
    }

    /**
     * Обработчик ответов в режиме запоминания (нажата кнопка "помню"/"не помню").
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void answerRememberModeHandler(Update update) throws TelegramApiException {
        TrainingSessionService.TrainingSession session = messageHandler.answerRememberModeHandler(update);
        if (session != null) {
            EditMessageText message = messageBuilder.showRememberModeMessage(update, session);
            executeEditMessage(message);
            if (session.isOver()) {
                messageHandler.endPlayModeHandler(update);
                SendMessage sendMessage = messageBuilder.folderShowMessage(update);
                executeNewMessage(sendMessage);
            }
        }
        else {
            failedCallbackSessionHandler(update);
        }

    }

    /**
     * Обработчик старта тестового режима (нажата кнопка "режим теста").
     * @param update Объект Update c обновлением
     * @throws TelegramApiException при ошибке отправки
     */
    private void startTestModeCallbackHandler(Update update) throws TelegramApiException {
        TrainingSessionService.TrainingSession started = messageHandler.startPlayModeHandler(update);
        SendMessage sendMessage;
        if (started != null) {
            sendMessage = messageBuilder.showTestModeMessage(update, started);
        }
        else {
            sendMessage = messageBuilder.failedPlayModeMessage(update);
            executeNewMessage(sendMessage);
            sendMessage = messageBuilder.folderShowMessage(update);
        }
        executeNewMessage(sendMessage);
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
     * Обработчик ответов в меню настроек (нажата кнопка "настройки"/"поменять значение настроек").
     * @param update
     * @throws TelegramApiException
     */
    private void settingsCallbackHandler(Update update) throws TelegramApiException {
        ShowMode setting = messageHandler.settingsHandler(update);
        if (update.getCallbackQuery().getData().equals("SHOW FOLDER")) {
            SendMessage message = messageBuilder.folderShowMessage(update);
            executeNewMessage(message);
        }
        else {
            EditMessageText text = messageBuilder.settingsMessage(setting, update);
            executeEditMessage(text);
        }
    }

}
