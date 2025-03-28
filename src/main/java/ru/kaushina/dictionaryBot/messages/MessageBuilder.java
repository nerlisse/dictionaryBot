package ru.kaushina.dictionaryBot.messages;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.enums.ShowMode;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.service.FolderService;
import ru.kaushina.dictionaryBot.service.TrainingSessionService;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.service.WordService;
import ru.kaushina.dictionaryBot.util.MessageTexts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Класс, ответственный за постройку сообщений бота.
 */
@Slf4j
@Component
public class MessageBuilder {

    private final UserService userService;
    private final FolderService folderService;
    private final WordService wordService;
    private final TrainingSessionService trainingSessionService;

    public MessageBuilder(UserService userService, FolderService folderService, WordService wordService,
                          TrainingSessionService trainingSessionService) {
        this.userService = userService;
        this.folderService = folderService;
        this.wordService = wordService;
        this.trainingSessionService = trainingSessionService;
    }

    /**
     * Вспомогательный метод для создания нового сообщения и присваивания ему chatId получателя.
     * @param update Объект Update с обновлением
     * @return SendMessage - новое сообщение
     */
    private SendMessage setNewMessageChatId(Update update) {
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
    private Long getChatId(Update update) {
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
    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(callbackData);
        return inlineKeyboardButton;
    }

    /**
     * Составление сообщения с выводом меню папки.
     * @param update Объект Update с обновлением
     * @return SendMessage - новое сообщение с меню папки
     */
    //show folder menu
    public SendMessage folderShowMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);
        Long chatId = getChatId(update);
        Long folderId = userService.getCurrentFolderId(chatId);
        Optional<Folder> folder = folderService.findById(folderId);
        if (folder.isEmpty()) {
            message.setText(MessageTexts.getMessage("message.folder_not_found"));
            return message;
        }

        log.info("showing folder {} for user {}", folder.get().getName(), chatId);

        message.setText(MessageTexts.getMessage("message.folder_screen", folder.get().getName()));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        //button for adding a word
        row.add(createButton(MessageTexts.getMessage("button.add_word"), "ADD WORD"));
        // button for deleting word
        row.add(createButton(MessageTexts.getMessage("button.delete_word"), "DELETE WORD"));
        rowsInline.add(row);

        row = new ArrayList<>();
        //button for showing all words
        row.add(createButton(MessageTexts.getMessage("button.show_words"), "SHOW WORDS"));
        rowsInline.add(row);

        row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("button.remember_mode"), "REMEMBER MODE"));
        row.add(createButton(MessageTexts.getMessage("button.test_mode"), "TEST MODE"));
        rowsInline.add(row);

        row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("button.settings"), "SETTINGS"));
        // button for deleting the folder
        row.add(createButton(MessageTexts.getMessage("button.delete_folder"), "DELETE FOLDER"));
        rowsInline.add(row);


        row = new ArrayList<>();
        // button for home screen
        row.add(createButton(MessageTexts.getMessage("button.go_home"), "HOME"));

        rowsInline.add(row);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
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

    /**
     * Составление нового сообщения с режимом запоминания.
     * @param update Объект Update с обновлением
     * @param session Объект TrainingSession с состоянием сессии
     * @return SendMessage - новое сообщение с началом режима запоминания
     */
    public SendMessage startRememberModeMessage(Update update,
                                                TrainingSessionService.TrainingSession session) {
        SendMessage message = setNewMessageChatId(update);
        message.setText(textRememberMode(session));
        message.setReplyMarkup(markupRememberMode(session));
        return message;
    }

    /**
     * Составление текста сообщения в режиме запоминания.
     * @param session Объект TrainingSession с состоянием текущей сессии
     * @return String - текст сообщения
     */
    private String textRememberMode(TrainingSessionService.TrainingSession session) {
        Word word = wordService.findById(session.getWords().get(session.getWordIndex()).getWordId());

        String wordKey = (session.getShowMode().equals(ShowMode.SHOW_KEY) ? word.getWordKey() : word.getWordValue());
        String wordValue = (session.getShowMode().equals(ShowMode.SHOW_KEY) ? word.getWordValue() : word.getWordKey());
        String text = MessageTexts.getMessage("message.remember_word", wordKey);
        if (session.isShowAnswer()) {
            text += MessageTexts.getMessage("message.remember_answer", wordValue);
        }
        text += MessageTexts.getMessage("message.progress", (session.getWordIndex()+1), session.getFolderSize());

        return text;
    }

    /**
     * Составление инлайна (кнопок сообщения) для режима запоминания.
     * @param session Объект TrainingSession с состоянием текущей сессии
     * @return InlineKeyboardMarkup объект разметки кнопок
     */
    private InlineKeyboardMarkup markupRememberMode(TrainingSessionService.TrainingSession session) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("message.remember"), "REMEMBER"));
        row.add(createButton(MessageTexts.getMessage("message.not_remember"), "DO NOT REMEMBER"));
        rowsInLine.add(row);

        row = new ArrayList<>();
        if (session.isShowAnswer()) {
            row.add(createButton(MessageTexts.getMessage("message.hide_answer"), "SHOW ANSWER"));
        }
        else {
            row.add(createButton(MessageTexts.getMessage("message.show_answer"), "HIDE ANSWER"));
        }
        rowsInLine.add(row);

        row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("message.end_practice"), "END PLAY"));
        rowsInLine.add(row);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    /**
     * Составление сообщения о неудаче начала игрового режима.
     * @param update Объект Update с обновлением
     * @return SendMessage - новое сообщение, уведомляющее о неудаче игрового режима
     */
    public SendMessage failedPlayModeMessage(Update update) {
        SendMessage sendMessage = setNewMessageChatId(update);
        sendMessage.setText(MessageTexts.getMessage("message.failed_start_mode"));
        return sendMessage;
    }

    /**
     * Составление части текста сообщения с отображением ответа на предыдущее слово.
     * @param session Объект TrainingSession с состоянием текущей сессии
     * @return String - строка, содержащая ответ на вопрос
     */
    private String showPreviousAnswer(TrainingSessionService.TrainingSession session) {
        String text = "";
        if (session.getPreviousWord() != null) {
            //show the answer for previous word
            Word prevWord = wordService.findById(session.getPreviousWord().getWordId());
            if (session.getPreviousWord().isRemembered()) {
                text += MessageTexts.getMessage("message.right_answer",
                        session.getShowMode().equals(ShowMode.SHOW_KEY) ?
                                prevWord.getWordValue() : prevWord.getWordKey());
            }
            else
                text += MessageTexts.getMessage("message.wrong_answer",
                        session.getShowMode().equals(ShowMode.SHOW_KEY) ?
                                prevWord.getWordValue() : prevWord.getWordKey());
        }
        return text;
    }

    /**
     * Составление сообщения о завершении игрового режима и отображении статистики.
     * @param session Объект TrainingSession с состоянием текущей сессии
     * @return String - текст сообщения со статистикой
     */
    private String endPlayModeMessage(TrainingSessionService.TrainingSession session) {
        String text = showPreviousAnswer(session);
        text += trainingSessionService.getStatistics(session);
        return text;
    }

    /**
     * Вспомогательный метод для создания редактированного сообщения и присваивания ему айди чата и сообщения.
     * @param update Объект Update с обновлением
     * @return EditMessageText редактированное сообщение с необходимыми полямиы
     */
    private EditMessageText setEditMessageChatId(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        return editMessageText;
    }

    /**
     * Составление редактированного сообщения для режима запоминания.
     * @param update Объект Update с обновлением
     * @param session Объект TrainingSession с состоянием текущей сессии
     * @return EditMessageText редактированное сообщение режима запоминания
     */
    public EditMessageText showRememberModeMessage(Update update,
                                                   TrainingSessionService.TrainingSession session) {
        EditMessageText editMessageText = setEditMessageChatId(update);
        if (!session.isOver()) {
            editMessageText.setText(textRememberMode(session));
            editMessageText.setReplyMarkup(markupRememberMode(session));
        }
        else {
            editMessageText.setText(endPlayModeMessage(session));
        }
        return editMessageText;
    }

    /**
     * Составление редактированного сообщения о несуществующей/недоступной сессии
     * @param update Объект Update с обновлением
     * @return EditMessageText редактированное сообщение недоступной сессии
     */
    public EditMessageText failedSessionMessage(Update update) {
        EditMessageText editMessageText = setEditMessageChatId(update);
        editMessageText.setText(MessageTexts.getMessage("message.wrong_session"));
        editMessageText.setReplyMarkup(null);
        return editMessageText;

    }

    /**
     * Составление нового сообщения о несуществующей/недоступной сессии
     * @param update Объект Update с обновлением
     * @return новое сообщение недоступной сессии
     */
    public SendMessage failedSessionNewMessage(Update update) {
        SendMessage sendMessage = setNewMessageChatId(update);
        sendMessage.setText(MessageTexts.getMessage("message.wrong_session"));
        return sendMessage;
    }

    /**
     * Составление нового сообщения с режимом теста
     * @param update Объект Update с обновлением
     * @param session Объект TrainingSession с состоянием текущей сессии
     * @return SendMessage - новое сообщение режима теста
     */
    public SendMessage showTestModeMessage(Update update,
                                           TrainingSessionService.TrainingSession session) {
        SendMessage sendMessage = setNewMessageChatId(update);
        if (!session.isOver()) {
            sendMessage.setText(textTestMode(session));
            sendMessage.setReplyMarkup(markupTestMode());
        } else {
            sendMessage.setText(endPlayModeMessage(session));
        }

        return sendMessage;
    }

    /**
     * Составление текста сообщения режима теста.
     * @param session Объект TrainingSession с состоянием текущей сессии
     * @return String - текст сообщения режима теста
     */
    private String textTestMode(TrainingSessionService.TrainingSession session) {
        String text = showPreviousAnswer(session);

        Word word = wordService.findById(session.getWords().get(session.getWordIndex()).getWordId());

        if (session.getShowMode().equals(ShowMode.SHOW_KEY)) {
            text += MessageTexts.getMessage("message.test_mode_word", word.getWordKey());
        } else {
            text += MessageTexts.getMessage("message.test_mode_value", word.getWordValue());
        }
        text += MessageTexts.getMessage("message.test_mode_warning");
        text += MessageTexts.getMessage("message.progress", (session.getWordIndex()+1), session.getFolderSize());
        return text;
    }

    /**
     * Составление инлайна (кнопок сообщения) для режима теста
     * @return InlineKeyboardMarkup - инлайн режима теста
     */
    private InlineKeyboardMarkup markupTestMode() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("message.end_practice"), "END PLAY"));
        rowsInLine.add(row);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    /**
     * Составление редактированного сообщения с настройками.
     * @param setting Объект ShowMode с текущей настройкой
     * @param update Объект Update с обновлением
     * @return EditMessageText - редактированное сообщение с настройками
     */
    public EditMessageText settingsMessage(ShowMode setting, Update update) {
        EditMessageText editMessageText = setEditMessageChatId(update);
        String text = MessageTexts.getMessage("message.settings",
                (setting.equals(ShowMode.SHOW_KEY) ? "термин" : "значение"));
        editMessageText.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(createButton(MessageTexts.getMessage("message.change_term"), "SHOW KEY"));
        row.add(createButton(MessageTexts.getMessage("message.change_meaning"), "SHOW VALUE"));
        rowsInLine.add(row);

        row = new ArrayList<>();
        row.add(createButton(MessageTexts.getMessage("button.go_home"), "SHOW FOLDER"));
        rowsInLine.add(row);

        markup.setKeyboard(rowsInLine);
        editMessageText.setReplyMarkup(markup);
        return editMessageText;
    }

    public SendMessage getEasterEggMessage(Update update) {
        SendMessage sendMessage = setNewMessageChatId(update);
        sendMessage.setText(MessageTexts.getMessage("message.easter_egg"));
        return sendMessage;
    }

    public SendMessage getEasterEgg2Message(Update update) {
        SendMessage sendMessage = setNewMessageChatId(update);
        sendMessage.setText(MessageTexts.getMessage("message.easter_egg2"));
        return sendMessage;
    }
}
