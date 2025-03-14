package ru.kaushina.dictionaryBot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kaushina.dictionaryBot.handlers.MessageBuilder;
import ru.kaushina.dictionaryBot.handlers.MessageHandler;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.UserState;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.config.BotConfig;
import ru.kaushina.dictionaryBot.model.Word;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class TelegramBotService {

    @Setter
    private MessageSender messageSender; // бот

    @Autowired
    private UserService userService;

    @Autowired
    private MessageBuilder messageBuilder;

    @Autowired
    private MessageHandler messageHandler;

    private final BotConfig config;

    private final Map<UserState, CheckedConsumer<Update>> stateHandlers;
    private final Map<String, CheckedConsumer<Update>> callbackHandlers;


    public TelegramBotService(BotConfig config) {
        this.messageSender = null;
        this.config = config;

        this.stateHandlers = new HashMap<>();

        stateHandlers.put(UserState.CREATE_FOLDER, this::createFolderHandler);
        stateHandlers.put(UserState.ADD_KEY, this::addKeyHandler);
        stateHandlers.put(UserState.ADD_VALUE, this::addValueHandler);
        stateHandlers.put(UserState.DELETE_WORD, this::deleteWordHandler);

        this.callbackHandlers = new HashMap<>();
        callbackHandlers.put("HOME", this::homeCallbackHandler);
        callbackHandlers.put("CREATE NEW FOLDER", this::createFolderCallbackHandler);
        callbackHandlers.put("DELETE FOLDER", this::deleteFolderCallbackHandler);
        callbackHandlers.put("SHOW FOLDER", this::showFolderCallbackHandler);
        callbackHandlers.put("ADD WORD", this::addWordCallbackHandler);
        callbackHandlers.put("SHOW WORDS", this::showWordsCallbackHandler);
        callbackHandlers.put("DELETE WORD", this::deleteWordCallbackHandler);
    }

    // обработка обновлений
    public void handleUpdate(Update update) throws TelegramApiException {
        //handling update
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);
            } else if (update.hasCallbackQuery()) { // if some button pressed
                handleCallbackQuery(update);
            }
        } catch (TelegramApiException e) {
            log.error("Error handling update: {}", e.getMessage());
        }
    }

    private void executeNewMessage(SendMessage sendMessage) throws TelegramApiException {
        Message sentMessage = messageSender.executeMessage(sendMessage);
        userService.setLastMessageId(sentMessage.getChatId(), sentMessage.getMessageId());
    }

    private void handleTextMessage(Update update) throws TelegramApiException {
        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        log.info("Received message from user {}: '{}'", chatId, message);

        User user = userService.findByChatId(chatId);

        if (message.equals("/start")) { //start command
            messageHandler.startCommandHandler(update); //handle start command
            SendMessage sendMessage = messageBuilder.getHomeMessage(update); //build a message
            executeNewMessage(sendMessage); //execute the message
            return;
        }

        CheckedConsumer<Update> handler = stateHandlers.get(user.getUserState());
        if (handler != null) {
            handler.accept(update);
        } else {
            log.warn("No handler found for user state: {}", user.getUserState());
        }
    }

    private void createFolderHandler(Update update) throws TelegramApiException {
        Folder folder = messageHandler.folderCreationHandler(update);
        SendMessage sendMessage = messageBuilder.folderCreatedMessage(update, folder);
        executeNewMessage(sendMessage);

        sendMessage = messageBuilder.getHomeMessage(update); //send home message
        executeNewMessage(sendMessage);
    }

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

    private void addValueHandler(Update update) throws TelegramApiException {
        Word word = messageHandler.addValueHandler(update);
        SendMessage sendMessage = messageBuilder.WordCreatedMessage(update, word);
        executeNewMessage(sendMessage);

        sendMessage = messageBuilder.folderShowMessage(update);
        executeNewMessage(sendMessage);
    }

    private void deleteWordHandler(Update update) throws TelegramApiException {
        boolean deleted = messageHandler.deleteWordHandler(update);
        SendMessage sendMessage = messageBuilder.WordDeletedMessage(update, deleted);
        executeNewMessage(sendMessage);

        sendMessage = messageBuilder.folderShowMessage(update);
        executeNewMessage(sendMessage);
    }


    private void executeEditMessage(Update update) throws TelegramApiException {

        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(update.getCallbackQuery().getMessage().getChatId());
        messageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        messageText.setText("This message is no longer available :(");
        messageSender.executeEditMessageText(messageText);
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
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

            executeEditMessage(update);

            return;
        }

        //answer for callback (for showing callback is answered)
        //TO_DO: do it after completing action not before
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        answer.setShowAlert(false);
        messageSender.executeCallbackAnswer(answer);

        String callback = callbackData.split("_")[0];
        CheckedConsumer<Update> handler = callbackHandlers.get(callback);

        if (handler != null) {
            handler.accept(update);
        }
        else {
            log.warn("No handler found for callback: {}", callbackData);
        }

    }

    private void homeCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.homeHandler(update);
        SendMessage sendMessage = messageBuilder.getHomeMessage(update); //build a message

        executeNewMessage(sendMessage); //execute the message
    }

    private void createFolderCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.createFolderHandler(update);
        SendMessage sendMessage = messageBuilder.createFolderMessage(update);
        executeNewMessage(sendMessage);
    }

    private void deleteFolderCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.deleteFolderHandler(update);
        SendMessage sendMessage = messageBuilder.getHomeMessage(update);
        executeNewMessage(sendMessage);
    }

    private void showFolderCallbackHandler(Update update) throws TelegramApiException {
        //log.info("");
        messageHandler.showFolderHandler(update);
        SendMessage sendMessage = messageBuilder.folderShowMessage(update);
        executeNewMessage(sendMessage);
    }

    private void addWordCallbackHandler(Update update) throws TelegramApiException {
        //log.info("");
        messageHandler.askToAddWordHandler(update);
        SendMessage sendMessage = messageBuilder.addWordMessage(update);
        executeNewMessage(sendMessage);
    }

    private void showWordsCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.showWordsHandler(update);
        SendMessage sendMessage = messageBuilder.showWordsMessage(update);
        executeNewMessage(sendMessage);

        sendMessage = messageBuilder.folderShowMessage(update); //send home message
        executeNewMessage(sendMessage);
    }

    private void deleteWordCallbackHandler(Update update) throws TelegramApiException {
        messageHandler.askToDeleteWordHandler(update);
        SendMessage sendMessage = messageBuilder.deleteWordMessage(update);
        executeNewMessage(sendMessage);
    }

}
