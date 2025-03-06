package ru.kaushina.dictionaryBot.handlers;


import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.UserState;
import ru.kaushina.dictionaryBot.repository.FolderRepository;
import ru.kaushina.dictionaryBot.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageBuilder {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public MessageBuilder(FolderRepository folderRepository, UserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
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
        button.setCallbackData("create");
        row.add(button);
        rowsInline.add(row);

        //find all folders of users
        List<Folder> folders = folderRepository.findByUser_ChatId(chatId);
        //set all of them in inline markup
        for (Folder folder : folders) {
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            String folderName = folder.getName();
            button.setText("folder: " + folderName);
            //need to set callbacks later
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
        Long chatId = update.getMessage().getChatId();
        message.setChatId(chatId.toString());
        String text = "enter new folder name: ";
        message.setText(text);

        User user = userRepository.findByUsername(update.getMessage().getFrom().getUserName());
        user.setUserState(UserState.CREATE_FOLDER);

        return message;
    }


    public SendMessage folderCreatedMessage(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());
        String folderName = String.valueOf(update.getMessage());

        Folder folder = new Folder(); // creating folder
        folder.setName(folderName);

        // connecting to user
        User user = userRepository.findByChatId(update.getMessage().getChatId());
        folder.setUser(user);

        // adding words
        folder.setWords(new ArrayList<>());

        // saving
        folderRepository.save(folder);

        message.setText("Folder " + folderName + " created");

        user.setUserState(UserState.MAIN_MENU);

        return message;

    }
}
