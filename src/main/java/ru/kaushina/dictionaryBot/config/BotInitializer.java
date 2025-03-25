package ru.kaushina.dictionaryBot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.meta.generics.TelegramBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Компонент для инициализации и регистрации Telegram бота при старте приложения.
 * Регистрирует бота в Telegram API после полной инициализации контекста Spring.
 * Обрабатывает ошибки регистрации бота.
 */
@Component
public class BotInitializer {

    TelegramBot bot;

    /**
     * Конструктор для внедрения зависимости Telegram бота.
     * @param bot экземпляр Telegram бота, который нужно зарегистрировать
     */
    public BotInitializer(TelegramBot bot) {
        this.bot = bot;
    }

    /**
     * Регистрирует бота в Telegram API при событии обновления контекста Spring.
     * Метод вызывается автоматически после полной инициализации контекста Spring.
     * В случае ошибки регистрации выводит stacktrace в консоль.
     * @throws TelegramApiException если произошла ошибка при регистрации бота
     */
    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot((LongPollingBot) bot);
        } catch (TelegramApiException e) {
            //log.error("Error occured: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
