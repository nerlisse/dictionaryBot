package ru.kaushina.dictionaryBot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.config.BotConfig;
import ru.kaushina.dictionaryBot.service.updates.CallbackQueryHandler;
import ru.kaushina.dictionaryBot.service.updates.TextMessageHandler;

/**
 * Сервис для обработки входящих обновлений Telegram бота.
 * Делегирует обработку сообщений и callback-запросов соответствующим обработчикам.
 * @see BotConfig
 * @see TextMessageHandler
 * @see CallbackQueryHandler
 */
@Slf4j
@Service
public class TelegramBotService {

    private final TextMessageHandler textMessageHandler;

    private final CallbackQueryHandler callbackQueryHandler;

    public TelegramBotService(TextMessageHandler textMessageHandler, CallbackQueryHandler callbackQueryHandler) {
        this.textMessageHandler = textMessageHandler;
        this.callbackQueryHandler = callbackQueryHandler;
    }

    /**
     * Обрабатывает входящее обновление от Telegram API.
     * <p>Определяет тип обновления (текстовое сообщение или callback-запрос)
     * и передает его соответствующему обработчику</p>
     * @param update входящее обновление от Telegram API
     * @throws TelegramApiException если возникает ошибка при обработке обновления
     */
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
