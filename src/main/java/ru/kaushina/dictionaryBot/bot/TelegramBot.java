package ru.kaushina.dictionaryBot.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kaushina.dictionaryBot.config.BotConfig;
import ru.kaushina.dictionaryBot.service.MessageSender;
import ru.kaushina.dictionaryBot.service.TelegramBotService;

@Component
public class TelegramBot extends TelegramLongPollingBot implements MessageSender {

    private final BotConfig config;
    private final TelegramBotService botService;

    public TelegramBot(BotConfig config, TelegramBotService botService) {
        this.config = config;
        this.botService = botService;

        botService.setMessageSender(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        botService.handleUpdate(update);
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
    public void executeMessage(SendMessage message) throws TelegramApiException {

    }

    @Override
    public void executeEditMessageText(EditMessageText message) throws TelegramApiException {

    }
}
