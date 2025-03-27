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


/**
 * Основной класс бота, реализующий long-polling версию бота.
 * Обрабатывает входящие обновления (сообщения и callback-запросы) и делегирует их обработку сервису {@link TelegramBotService}.
 * Реализует интерфейс {@link MessageSender} для отправки сообщений и ответов.
 *
 * @see TelegramLongPollingBot
 * @see MessageSender
 */
@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot implements MessageSender {

    /**Поле конфигурации бота*/
    private final BotConfig config;
    /**Поле сервиса бота*/
    private final TelegramBotService botService;

    /**
     * Конструктор бота. Вживляет себя в обработчики запросов.
     * @param config конфигурация бота (имя и токен)
     * @param botService основной сервис для обработки обновлений
     * @param textMessageHandler обработчик текстовых сообщений
     * @param callbackQueryHandler обработчик callback-запросов
     */
    public TelegramBot(BotConfig config, TelegramBotService botService, TextMessageHandler textMessageHandler,
                       CallbackQueryHandler callbackQueryHandler) {
        this.config = config;
        this.botService = botService;

        //botService.setMessageSender(this);
        textMessageHandler.setMessageSender(this);
        callbackQueryHandler.setMessageSender(this);
    }

    /**
     * Обрабатывает входящее обновление от Telegram API.
     * Делегирует обработку сервису {@link TelegramBotService}.
     * @param update входящее обновление (может содержать сообщение или callback-запрос)
     * @throws RuntimeException если произошла ошибка при обработке обновления
     */
    @Override
    public void onUpdateReceived(Update update) {
        try {
            botService.handleUpdate(update);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Возвращает имя бота из конфигурации.
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    /**
     * Возвращает токен бота из конфигурации.
     * @return токен бота
     */
    @Override
    public String getBotToken() {
        return config.getToken();
    }

    /**
     * Отправляет новое текстовое сообщение пользователю.
     * @param message сообщение для отправки
     * @return отправленное сообщение с серверными данными
     * @throws TelegramApiException если не удалось отправить сообщение
     */
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

    /**
     * Редактирует существующее сообщение.
     * @param message объект с новым текстом и идентификаторами сообщения
     * @throws TelegramApiException если не удалось отредактировать сообщение
     */
    @Override
    public void executeEditMessageText(EditMessageText message) throws TelegramApiException {
        try {
            execute(message);
            log.info("message {} edited for user {}: {}", message.getMessageId(), message.getChatId(), message.getText());
        } catch (TelegramApiException e) {
            log.error("failed to edit message for user {}: {}", message.getChatId(), e.getMessage());
        }
    }

    /**
     * Отправляет ответ на callback-запрос (убирает часики у нажатой кнопки).
     * @param callbackQuery ответ на callback-запрос
     * @throws TelegramApiException если не удалось отправить ответ
     */
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
