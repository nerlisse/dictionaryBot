package ru.kaushina.dictionaryBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kaushina.dictionaryBot.config.BotConfig;
import ru.kaushina.dictionaryBot.service.TelegramBotService;
import ru.kaushina.dictionaryBot.service.updates.CallbackQueryHandler;
import ru.kaushina.dictionaryBot.service.updates.TextMessageHandler;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot implements MessageSender {

    private final BotConfig config;
    private final TelegramBotService botService;

    public TelegramBot(BotConfig config, TelegramBotService botService, TextMessageHandler textMessageHandler,
                       CallbackQueryHandler callbackQueryHandler) {
        this.config = config;
        this.botService = botService;

        //botService.setMessageSender(this);
        textMessageHandler.setMessageSender(this);
        callbackQueryHandler.setMessageSender(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            botService.handleUpdate(update);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public Message executeMessage(SendMessage message) throws TelegramApiException {
        try {
            Message sentMessage = execute(message);
            log.info("message sent to user {}: {}", message.getChatId(), message.getText());
            return sentMessage;
        } catch (TelegramApiException e) {
            log.error("failed to send message to user {}: {}", message.getChatId(), e.getMessage());
        }
        return null;
    }

    @Override
    public void executeEditMessageText(EditMessageText message) throws TelegramApiException {
        try {
            execute(message);
            log.info("message {} edited for user {}: {}", message.getMessageId(), message.getChatId(), message.getText());
        } catch (TelegramApiException e) {
            log.error("failed to edit message for user {}: {}", message.getChatId(), e.getMessage());
        }
    }

    @Override
    public void executeCallbackAnswer(AnswerCallbackQuery callbackQuery) throws TelegramApiException {
        try {
            execute(callbackQuery);
            log.info("callback query {} answered", callbackQuery.getCallbackQueryId());
        } catch (TelegramApiException e) {
            log.error("failed to answer callback query {}: {}", callbackQuery.getCallbackQueryId(), e.getMessage());
        }
    }
}
