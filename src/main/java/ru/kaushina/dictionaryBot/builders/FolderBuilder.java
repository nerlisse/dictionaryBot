package ru.kaushina.dictionaryBot.builders;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.service.FolderService;
import ru.kaushina.dictionaryBot.util.MessageTexts;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс, ответственный за построение сообщений, связанных с папками.
 */
@Component
public class FolderBuilder {

    private final FolderService folderService;

    public FolderBuilder(FolderService folderService) {
        this.folderService = folderService;
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

}
