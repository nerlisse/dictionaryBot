package ru.kaushina.dictionaryBot.handlers;


import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.UserState;
import ru.kaushina.dictionaryBot.service.FolderService;
import ru.kaushina.dictionaryBot.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageBuilder {

    private final UserService userService;
    private final FolderService folderService;

    public MessageBuilder(UserService userService, FolderService folderService) {
        this.userService = userService;
        this.folderService = folderService;
    }

    public SendMessage getHomeMessage(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        // set chatid
        message.setChatId(chatId.toString());

        //set text message
        String text = "hello! thx for using this bot. here are your folders: ";
        message.setText(text);

        //set inline markup
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        // adding create folder button
        var button = new InlineKeyboardButton();
        button.setText("Create new folder");
        button.setCallbackData("CREATE NEW FOLDER");
        row.add(button);
        rowsInline.add(row);

        //find all folders of users
        List<Folder> folders = folderService.findByUser_ChatId(chatId);
        //set all of them in inline markup
        for (Folder folder : folders) {
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            String folderName = folder.getName();
            button.setText("folder: " + folderName);
            //need to set callbacks later
            button.setCallbackData("FOLDER_" + folder.getId());
            row.add(button);
            rowsInline.add(row);
        }

        //set markup for message
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;

    }

    public SendMessage createFolderMessage(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId.toString());
        //asking to enter folder name
        String text = "enter new folder name: ";
        message.setText(text);

        //setting state to CREATE_FOLDER
        userService.setUserState(chatId, UserState.CREATE_FOLDER);

        return message;
    }


    public SendMessage folderCreatedMessage(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(chatId.toString());
        String folderName = update.getMessage().getText();

        Folder folder = folderService.createFolder(folderName, chatId);

        if (folder == null) {
            message.setText("folder with that name already exists, how could you forget?");
        } else {
            message.setText("Folder " + folderName + " created");
        }
        return message;

    }
}
