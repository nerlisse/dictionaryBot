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
import ru.kaushina.dictionaryBot.service.FolderService;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.util.MessageTexts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Класс, ответственный за построение сообщений, связанных с папками.
 */
@Slf4j
@Component
public class FolderBuilder implements IMessageBuilder {

    private final FolderService folderService;
    private final UserService userService;

    public FolderBuilder(FolderService folderService, UserService userService) {
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
     * Получение сообщения "главного меню" - сообщение с папками и созданием новой папки.
     * @param update Объект Update с обновлением
     * @return SendMessage - новое сообщение с главным меню
     */
    public SendMessage getHomeMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);

        //set text message
        String text = MessageTexts.getMessage("message.hello");
        message.setText(text);

        //set inline markup
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row;

        //find all folders of users
        List<Folder> folders = folderService.findByUser_ChatId(getChatId(update));
        //set all of them in inline markup
        for (Folder folder : folders) {
            row = new ArrayList<>();
            row.add(createButton(MessageTexts.getMessage("button.folder", folder.getName()),
                    "SHOW FOLDER_" + folder.getId()));
            rowsInline.add(row);
        }

        row = new ArrayList<>();
        // adding create folder button
        row.add(createButton(MessageTexts.getMessage("button.create_folder"), "CREATE NEW FOLDER"));
        rowsInline.add(row);

        //set markup for message
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;

    }

    /**
     * Составление сообщения с приглашением ввести имя новой папки.
     * @param update Объект Update с обновлением
     * @return SendMessage - сообщение ввода новой папки
     */
    public SendMessage createFolderMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);
        String text = MessageTexts.getMessage("message.enter_folder_name"); //asking to enter folder name
        message.setText(text);
        return message;
    }

    /**
     * Составление сообщения успешного/неуспешного создания папки.
     * @param update Объект Update с обновлением
     * @param folder Объект Folder, содержащий объект, если папка была создана, и{@code null}
     * @return SendMessage - сообщение, уведомляющее пользователя о создании папки
     */
    public SendMessage folderCreatedMessage(Update update, Folder folder) {
        SendMessage message = setNewMessageChatId(update);
        if (folder == null) {
            message.setText(MessageTexts.getMessage("message.folder_not_created"));
        } else {
            message.setText(MessageTexts.getMessage("message.folder_created", folder.getName()));
        }
        return message;
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

    private EditMessageText setEditMessageChatId(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        return editMessageText;
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
