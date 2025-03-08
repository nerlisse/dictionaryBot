package ru.kaushina.dictionaryBot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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


    public TelegramBotService(BotConfig config) {
        this.messageSender = null;
        this.config = config;
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

        if (user.getUserState().equals(UserState.CREATE_FOLDER)) { // user entered new folder name

            Folder folder = messageHandler.folderCreationHandler(update);
            SendMessage sendMessage = messageBuilder.folderCreatedMessage(update, folder);
            executeNewMessage(sendMessage);

            sendMessage = messageBuilder.getHomeMessage(update); //send home message
            executeNewMessage(sendMessage);
            return;
        }
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        log.info("Received callback {} from user {}", callbackData, chatId);



        //answer for callback (for showing callback is answered)
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        answer.setShowAlert(false);
        messageSender.executeCallbackAnswer(answer);

        if (callbackData.equals("HOME")) {
            messageHandler.homeHandler(update);
            SendMessage sendMessage = messageBuilder.getHomeMessage(update); //build a message

            executeNewMessage(sendMessage); //execute the message
            return;
        }

        if (callbackData.equals("CREATE NEW FOLDER")) { // create folder pressed
            messageHandler.createFolderHandler(update);
            SendMessage sendMessage = messageBuilder.createFolderMessage(update);
            executeNewMessage(sendMessage);
            return;
        }

        if (callbackData.contains("DELETE FOLDER_")) {
            messageHandler.deleteFolderHandler(update);
            SendMessage sendMessage = messageBuilder.getHomeMessage(update);
            executeNewMessage(sendMessage);
            return;
        }

        if (callbackData.contains("SHOW FOLDER_")) {
            log.info("");
            messageHandler.showFolderHandler(update);
            SendMessage sendMessage = messageBuilder.folderShowMessage(update);
            executeNewMessage(sendMessage);
            return;
        }
    }

}
