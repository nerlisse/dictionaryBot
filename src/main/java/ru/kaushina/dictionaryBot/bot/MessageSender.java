package ru.kaushina.dictionaryBot.bot;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Интерфейс для отправки сообщений и обработки callback-запросов в Telegram боте.
 * <p>
 * Реализация интерфейса предоставляет методы для:
 * <ul>
 *   <li>Отправки текстовых сообщений</li>
 *   <li>Редактирования существующих сообщений</li>
 *   <li>Ответа на callback-запросы</li>
 * </ul>
 */
public interface MessageSender {
    /**
     * Отправка нового сообщения.
     * @param message сообщение для отправки (не может быть {@code null})
     * @return отправленное сообщение с заполненными полями
     * @throws TelegramApiException если произошло ошибка при отправке
     */
    Message executeMessage(SendMessage message) throws TelegramApiException;

    /**
     * Отправка редактированного сообщения.
     * @param message отредактированное сообщение
     * @throws TelegramApiException если произошла ошибка при отправке
     */
    void executeEditMessageText(EditMessageText message) throws TelegramApiException;

    /**
     * Ответ на callback-запросы (убирает часики у нажатой кнопки).
     * @param callbackQuery - ответ на callback
     * @throws TelegramApiException если произошла ошибка при ответе
     */
    void executeCallbackAnswer(AnswerCallbackQuery callbackQuery) throws TelegramApiException;
}
