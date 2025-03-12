package ru.kaushina.dictionaryBot.service;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@FunctionalInterface
public interface CheckedConsumer<T> {
    void accept(T t) throws TelegramApiException;
}

