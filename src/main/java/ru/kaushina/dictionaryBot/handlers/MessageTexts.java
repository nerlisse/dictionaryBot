package ru.kaushina.dictionaryBot.handlers;


import java.util.ResourceBundle;

public class MessageTexts {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("message");

    public static String getMessage(String key, Object... args) {
        return String.format(bundle.getString(key), args);
    }

}
