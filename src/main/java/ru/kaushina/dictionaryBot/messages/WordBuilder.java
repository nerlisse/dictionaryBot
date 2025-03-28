package ru.kaushina.dictionaryBot.messages;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.service.FolderService;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.util.MessageTexts;

import java.util.List;

/**
 * Класс, отвечающий за постройку сообщений для добавления, удаления, вывода слов.
 */
@Component
public class WordBuilder implements IMessageBuilder {

    private final FolderService folderService;
    private final UserService userService;

    public WordBuilder(FolderService folderService, UserService userService) {
        this.folderService = folderService;
        this.userService = userService;
    }

    /**
     * Вспомогательный метод для создания нового сообщения и присваивания ему chatId получателя.
     * @param update Объект Update с обновлением
     * @return SendMessage - новое сообщение
     */
    public SendMessage setNewMessageChatId(Update update) {
        SendMessage sendMessage = new SendMessage();
        Long chatId = getChatId(update);
        sendMessage.setChatId(chatId);
        return sendMessage;
    }

    /**
     * Вспомогательный метод для нахождения chatId пользователя из текстового сообщения или callback-а.
     * @param update Объект Update с обновлением
     * @return Long - идентификатор чата с пользователем
     */
    public Long getChatId(Update update) {
        Long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else chatId = update.getMessage().getChatId();
        return chatId;
    }

    /**
     * Вспомогательный метод для создания инлайн-кнопки.
     * @param text текст кнопки
     * @param callbackData callback, присваиваемый кнопке
     * @return InlineKeyboardButton готовый объект кнопки
     */
    public InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(callbackData);
        return inlineKeyboardButton;
    }

    /**
     * Составление сообщения с приглашением ввести термин.
     * @param update Объект Update с обновлением
     * @return SendMessage - новое сообщение о вводе термина
     */
    public SendMessage addWordMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);
        message.setText(MessageTexts.getMessage("message.add_word"));
        return message;
    }

    /**
     * Составление сообщения о неудачном добавлении слова.
     * @param update Объект Update с обновлением
     * @return SendMessage - новое сообщение, уведомляющее о неудаче термина
     */
    public SendMessage failedToAddWordMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);
        message.setText(MessageTexts.getMessage("message.add_word_again"));
        return message;
    }

    /**
     * Составления сообщения о приглашении к добавлению значения для термина.
     * @param update Объект Update с обновлением
     * @return SendMessage - новое сообщение о вводе значения термина
     */
    public SendMessage addValueMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);
        message.setText(MessageTexts.getMessage("message.add_value"));
        return message;
    }

    /**
     * Составление сообщения со всеми словами в выбранной папке.
     * @param update Объект Update с обновлением
     * @return SendMessage - новое сообщение со словами
     */
    public SendMessage showWordsMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);
        Long chatId = getChatId(update);
        StringBuilder text = new StringBuilder();

        Long folderId = userService.getCurrentFolderId(chatId);
        List<Word> words = folderService.getFolderWords(folderId);
        if (words.isEmpty()) {
            text.append(MessageTexts.getMessage("message.words_not_found"));
        }
        else {
            text.append(MessageTexts.getMessage("message.show_words"));
            int i = 1;
            for (Word word : words) {
                text.append(i).append(") ");
                text.append(word.getWordKey()).append(":\n");
                text.append(word.getWordValue()).append("\n");
                i++;
            }
        }
        message.setText(text.toString());

        return message;
    }

    /**
     * Составление сообщения об успешном добавлении/неудаче при добавлении слова.
     * @param update Объект Update с обновлением
     * @param word Объект Word с созданным словом, {@code null} если слово не было создано
     * @return SendMessage - новое сообщение с результатом создания слова
     */
    public SendMessage WordCreatedMessage(Update update, Word word) {
        SendMessage sendMessage = setNewMessageChatId(update);
        if (word != null) {
            sendMessage.setText(MessageTexts.getMessage("message.word_created"));
        }
        else {
            sendMessage.setText(MessageTexts.getMessage("message.word_not_created"));
        }
        return sendMessage;
    }

    /**
     * Составление сообщения для приглашения к удалению слова
     * @param update Объект Update с обновлением
     * @return SendMessage - новое сообщение с приглашением к удалению
     */
    public SendMessage deleteWordMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);
        message.setText(MessageTexts.getMessage("message.delete_word"));
        return message;
    }

    /**
     * Составление сообщение об успешном/неуспешном удалении слова
     * @param update Объект Update с обновлением
     * @param deleted флаг, показывающий, было ли удалено слово
     * @return SendMessage - новое сообщение с результатом удаления
     */
    public SendMessage WordDeletedMessage(Update update, boolean deleted) {
        SendMessage message = setNewMessageChatId(update);
        if (deleted) {
            message.setText(MessageTexts.getMessage("message.word_deleted"));
        }
        else {
            message.setText(MessageTexts.getMessage("message.word_not_deleted"));
        }
        return message;
    }

}
