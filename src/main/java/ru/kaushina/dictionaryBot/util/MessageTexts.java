package ru.kaushina.dictionaryBot.util;


import java.util.ResourceBundle;

/**
 * Класс для работы с локализованными текстовыми сообщениями.
 * <p>
 * Загружает тексты из property-файлов и предоставляет методы для их форматирования.
 * Для работы требуется файл сообщений {@code message.properties}
 */
public class MessageTexts {
    /**
     * Загружает текстовые сообщения из property-файлов.
     * Ищет файлы по базовому имени {@code message} в classpath.
     */
    private static final ResourceBundle bundle = ResourceBundle.getBundle("message");

    /**
     * Возвращает локализованное сообщение по ключу с подстановкой аргументов.
     * @param key  ключ сообщения в property-файле
     * @param args аргументы для подстановки в сообщение
     * @return отформатированное сообщение
     */
    public static String getMessage(String key, Object... args) {
        return String.format(bundle.getString(key), args);
    }

}
