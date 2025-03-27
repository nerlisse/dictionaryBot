package ru.kaushina.dictionaryBot.service.consumer;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Функциональный интерфейс для операции, которая принимает один аргумент и может выбрасывать исключение TelegramApiException.
 * Аналог java.util.function.Consumer, но с поддержкой проверяемых исключений.
 * @param <T> тип входного параметра
 */
@FunctionalInterface
public interface CheckedConsumer<T> {
    /**
     * Выполняет операцию над заданным аргументом.
     * @param t входной аргумент
     * @throws TelegramApiException если в процессе выполнения операции произошла ошибка Telegram API
     */
    void accept(T t) throws TelegramApiException;
}

