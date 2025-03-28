package ru.kaushina.dictionaryBot.service.updates;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kaushina.dictionaryBot.bot.MessageSender;
import ru.kaushina.dictionaryBot.messages.MessageBuilderFacade;
import ru.kaushina.dictionaryBot.messages.ReminderBuilder;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.Reminder;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.model.enums.UserState;
import ru.kaushina.dictionaryBot.service.TrainingSessionService;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.service.consumer.CheckedConsumer;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис обработки текстовых сообщений от пользователя.
 */
@Slf4j
@Service
public class TextMessageHandler {

    @Setter
    private MessageSender messageSender; // бот

    @Autowired
    private UserService userService;

    @Autowired
    private MessageBuilderFacade messageBuilder;

    @Autowired
    private MessageHandler messageHandler;

    private final Map<UserState, CheckedConsumer<Update>> stateHandlers;
    private ReminderBuilder reminderBuilder;

    public TextMessageHandler() {

        this.stateHandlers = new HashMap<>();

        stateHandlers.put(UserState.CREATE_FOLDER, this::createFolderHandler);
        stateHandlers.put(UserState.ADD_KEY, this::addKeyHandler);
        stateHandlers.put(UserState.ADD_VALUE, this::addValueHandler);
        stateHandlers.put(UserState.DELETE_WORD, this::deleteWordHandler);
        stateHandlers.put(UserState.TEST_MODE, this::answerTestModeHandler);
        stateHandlers.put(UserState.REMINDER_EDIT, this::enterTimeHandler);
    }

    /**
     * Отправляет новое сообщение и регистрирует идентификатор последнего сообщения
     * @param sendMessage Объект SendMessage для отправки
     * @throws TelegramApiException при ошибке отправки
     */
    private void executeNewMessage(SendMessage sendMessage) throws TelegramApiException {
        Message sentMessage = messageSender.executeMessage(sendMessage);
        userService.setLastMessageId(sentMessage.getChatId(), sentMessage.getMessageId());
    }

    /**
     * Обрабатывает текстовое сообщение от пользователя.
     * @param update Объект Update с обновлением
     * @throws TelegramApiException при ошибке обработки
     */
    public void handleTextMessage(Update update) throws TelegramApiException {
        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        log.info("Received message from user {}: '{}'", chatId, message);

        User user = userService.findByChatId(chatId);

        if (message.equals("/start")) { //start command
            startCommand(update);
            return;
        }

        if (message.equals("/crm") || message.equals("/flex")) {
            easterCommand(update);
            return;
        }

        CheckedConsumer<Update> handler = stateHandlers.get(user.getUserState());
        if (handler != null) {
            handler.accept(update);
        } else {
            log.warn("No handler found for user state: {}", user.getUserState());
        }
    }

    /**
     * Обработка команды /start.
     * @param update Объект Update с обновлением
     * @throws TelegramApiException при ошибке обработки
     */
    private void startCommand(Update update) throws TelegramApiException {
        messageHandler.startCommandHandler(update); //handle start command
        SendMessage sendMessage = messageBuilder.getHomeMessage(update); //build a message
        executeNewMessage(sendMessage); //execute the message
    }

    /**
     * Обработка пасхальных команд ;)
     * @param update Объект Update с обновлением
     * @throws TelegramApiException при ошибке обработки
     */
    private void easterCommand(Update update) throws TelegramApiException {
        SendMessage sendMessage;
        if (update.getMessage().getText().equals("/crm")) {
            sendMessage = messageBuilder.getEasterEggMessage(update);
            executeNewMessage(sendMessage);
        }
        else if (update.getMessage().getText().equals("/flex")) {
            sendMessage = messageBuilder.getEasterEgg2Message(update);
            executeNewMessage(sendMessage);
        }
        messageHandler.startCommandHandler(update); //handle start command
        sendMessage = messageBuilder.getHomeMessage(update); //build a message
        executeNewMessage(sendMessage); //execute the message
    }

    /**
     * Обработчик добавления папки.
     * @param update Объект Update с обновлением
     * @throws TelegramApiException при ошибке отправки ответа
     */
    private void createFolderHandler(Update update) throws TelegramApiException {
        Folder folder = messageHandler.folderCreationHandler(update);
        SendMessage sendMessage = messageBuilder.folderCreatedMessage(update, folder);
        executeNewMessage(sendMessage);

        sendMessage = messageBuilder.getHomeMessage(update); //send home message
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик добавления термина(ключа) для пользователя.
     * @param update Объект Update с обновлением
     * @throws TelegramApiException при ошибке отправки ответа
     */
    private void addKeyHandler(Update update) throws TelegramApiException {
        boolean created = messageHandler.addKeywordHandler(update);
        SendMessage sendMessage;
        if (created) {
            sendMessage = messageBuilder.addValueMessage(update);
        }
        else
            sendMessage = messageBuilder.failedToAddWordMessage(update);
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик добавления значения и создания слова.
     * @param update Объект Update с обновлением
     * @throws TelegramApiException при ошибке отправки ответа
     */
    private void addValueHandler(Update update) throws TelegramApiException {
        Word word = messageHandler.addValueHandler(update);
        SendMessage sendMessage = messageBuilder.WordCreatedMessage(update, word);
        executeNewMessage(sendMessage);

        sendMessage = messageBuilder.folderShowMessage(update);
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик удаления слова из папки.
     * @param update Объект Update с обновлением
     * @throws TelegramApiException при ошибке отправки ответа
     */
    private void deleteWordHandler(Update update) throws TelegramApiException {
        boolean deleted = messageHandler.deleteWordHandler(update);
        SendMessage sendMessage = messageBuilder.WordDeletedMessage(update, deleted);
        executeNewMessage(sendMessage);

        sendMessage = messageBuilder.folderShowMessage(update);
        executeNewMessage(sendMessage);
    }

    /**
     * Обработчик ответов на режим теста.
     * @param update Объект Update с обновлением
     * @throws TelegramApiException при ошибке отправки ответа
     */
    private void answerTestModeHandler(Update update) throws TelegramApiException {
        TrainingSessionService.TrainingSession session = messageHandler.answerTestModeHandler(update);
        if (session == null) {
            failedMessageSessionHandler(update);
        }
        else {
            SendMessage sendMessage = messageBuilder.showTestModeMessage(update, session);
            executeNewMessage(sendMessage);
            if (session.isOver()) {
                messageHandler.endPlayModeHandler(update);
                sendMessage = messageBuilder.folderShowMessage(update);
                executeNewMessage(sendMessage);
            }
        }
    }

    /**
     * Обработчик ответов в случае несуществующей/недоступной сессии.
     * @param update Объект Update с обновлением
     * @throws TelegramApiException при ошибке отправки ответа
     */
    private void failedMessageSessionHandler(Update update) throws TelegramApiException {
        SendMessage message = messageBuilder.failedSessionNewMessage(update);
        executeNewMessage(message);

        SendMessage sendMessage = messageBuilder.folderShowMessage(update);
        executeNewMessage(sendMessage);
    }


    /**
     * Обработчик ввода времени для напоминания.
     * @param update Объект Update с обновлением
     * @throws TelegramApiException при ошибке отправки ответа
     */
    private void enterTimeHandler(Update update) throws TelegramApiException {
        Reminder reminder = messageHandler.addTimeHandler(update);
        SendMessage message = reminderBuilder.reminderMenu(update, reminder);
        executeNewMessage(message);
    }

}
