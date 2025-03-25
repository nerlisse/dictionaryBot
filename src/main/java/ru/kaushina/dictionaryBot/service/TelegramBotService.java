package ru.kaushina.dictionaryBot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.config.BotConfig;
import ru.kaushina.dictionaryBot.service.updates.CallbackQueryHandler;
import ru.kaushina.dictionaryBot.service.updates.TextMessageHandler;


@Slf4j
@Service
public class TelegramBotService {

    private final BotConfig config;

    @Autowired
    private TextMessageHandler textMessageHandler;

    @Autowired
    private CallbackQueryHandler callbackQueryHandler;


    public TelegramBotService(BotConfig config) {
        this.config = config;
    }

    // обработка обновлений
    public void handleUpdate(Update update) throws TelegramApiException {
        //handling update
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                textMessageHandler.handleTextMessage(update);
            } else if (update.hasCallbackQuery()) { // if some button pressed
                callbackQueryHandler.handleCallbackQuery(update);
            }
        } catch (TelegramApiException e) {
            log.error("Error handling update: {}", e.getMessage());
        }
    }

}
