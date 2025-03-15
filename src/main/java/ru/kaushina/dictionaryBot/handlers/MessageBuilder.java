package ru.kaushina.dictionaryBot.handlers;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.service.FolderService;
import ru.kaushina.dictionaryBot.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
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
        Long chatId;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        }
        else {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }

        // set chatid
        message.setChatId(chatId.toString());

        //set text message
        String text = "hello! thx for using this bot. here are your folders: ";
        message.setText(text);

        //set inline markup
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row;


        //find all folders of users
        List<Folder> folders = folderService.findByUser_ChatId(chatId);
        //set all of them in inline markup
        for (Folder folder : folders) {
            row = new ArrayList<>();
            row.add(createButton("folder: " + folder.getName(), "SHOW FOLDER_" + folder.getId()));
            rowsInline.add(row);
        }

        row = new ArrayList<>();
        // adding create folder button
        row.add(createButton("Create new folder", "CREATE NEW FOLDER"));
        rowsInline.add(row);

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

        return message;
    }


    public SendMessage folderCreatedMessage(Update update, Folder folder) {
        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(chatId.toString());

        if (folder == null) {
            message.setText("folder was not created. Make sure you entered valid name, not empty, too long or already existing");
        } else {
            message.setText("Folder " + folder.getName() + " created");
        }
        return message;

    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(callbackData);
        return inlineKeyboardButton;
    }

    //show folder menu
    public SendMessage folderShowMessage(Update update) {


        SendMessage message = new SendMessage();
        Long chatId;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        }
        else {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }
        message.setChatId(chatId.toString());

        Long folderId = userService.getCurrentFolderId(chatId);
        Optional<Folder> folder = folderService.findById(folderId);
        if (folder.isEmpty()) {
            message.setText("folder with that name does not exist");
            return message;
        }

        log.info("showing folder {} for user {}", folder.get().getName(), chatId);

        message.setText("You are in folder " + folder.get().getName());

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        //button for adding a word
        row.add(createButton("Add word", "ADD WORD"));
        // button for deleting word
        row.add(createButton("Delete word", "DELETE WORD"));
        rowsInline.add(row);

        row = new ArrayList<>();
        //button for showing all words
        row.add(createButton("Show all words", "SHOW WORDS"));
        // button for deleting the folder
        row.add(createButton("Delete folder", "DELETE FOLDER"));
        rowsInline.add(row);

        row = new ArrayList<>();
        row.add(createButton("Remember mode", "REMEMBER MODE"));
        row.add(createButton("Test mode", "TEST MODE"));
        rowsInline.add(row);

        row = new ArrayList<>();
        // button for home screen
        row.add(createButton("Go back", "HOME"));

        rowsInline.add(row);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;

    }

    public SendMessage addWordMessage(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId.toString());
        //asking to enter word
        String text = "enter new word: ";
        message.setText(text);

        return message;
    }

    public SendMessage failedToAddWordMessage(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(chatId.toString());
        String text = "failed to add word, too long. Enter valid name: ";
        message.setText(text);
        return message;
    }

    public SendMessage addValueMessage(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(chatId.toString());
        //asking to enter word
        String text = "enter value: ";
        message.setText(text);

        return message;
    }

    public SendMessage showWordsMessage(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId.toString());
        StringBuilder text = new StringBuilder();
        text.append("Here are your words:\n\n");

        Long folderId = userService.getCurrentFolderId(chatId);
        List<Word> words = folderService.getFolderWords(folderId);
        int i = 1;
        for (Word word : words) {
            text.append(i).append(") ");
            text.append(word.getWordKey()).append(":\n");
            text.append(word.getWordValue()).append("\n");
            i++;
        }
        message.setText(text.toString());

        return message;
    }

    public SendMessage WordCreatedMessage(Update update, Word word) {
        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        sendMessage.setChatId(chatId.toString());

        if (word != null) {
            sendMessage.setText("Word created");
        }
        else {
            sendMessage.setText("Couldn't create word. Perhaps, entered key already exists in this folder or you" +
                    "entered empty values :(");
        }
        return sendMessage;
    }

    public SendMessage deleteWordMessage(Update update) {
        SendMessage message = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        message.setChatId(chatId.toString());
        message.setText("Enter the word to delete");
        return message;
    }

    public SendMessage WordDeletedMessage(Update update, boolean deleted) {
        SendMessage message = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        message.setChatId(chatId.toString());

        if (deleted) {
            message.setText("Word deleted");
        }
        else {
            message.setText("Couldn't delete word: no such key");
        }
        return message;
    }
}
