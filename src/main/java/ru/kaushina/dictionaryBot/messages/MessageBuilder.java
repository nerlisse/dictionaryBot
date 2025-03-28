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
