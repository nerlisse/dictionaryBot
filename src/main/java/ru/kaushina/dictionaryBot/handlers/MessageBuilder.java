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
import ru.kaushina.dictionaryBot.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageBuilder {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public MessageBuilder(FolderRepository folderRepository, UserRepository userRepository, UserService userService) {
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.userService = userService;
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
        List<Folder> folders = folderRepository.findByUser_ChatId(chatId);
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
        User user = userRepository.findByChatId(chatId);

        if (folderRepository.findByUser_ChatIdAndName(chatId, folderName) != null) {
            message.setText("folder with that name already exists, how could you forget?");
        } else {
            Folder folder = new Folder(); // creating folder
            folder.setName(folderName);

            // connecting to user
            folder.setUser(user);

            // adding words
            folder.setWords(new ArrayList<>());

            // saving
            folderRepository.save(folder);

            message.setText("Folder " + folderName + " created");
        }
        // going back to main menu
        user.setUserState(UserState.MAIN_MENU);
        userRepository.save(user);

        return message;

    }
}
