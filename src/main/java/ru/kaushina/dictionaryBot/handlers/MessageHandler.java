package ru.kaushina.dictionaryBot.handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.UserState;
import ru.kaushina.dictionaryBot.service.FolderService;
import ru.kaushina.dictionaryBot.service.UserService;

@Component
public class MessageHandler {

    private final UserService userService;
    private final FolderService folderService;

    public MessageHandler(UserService userService, FolderService folderService) {
        this.userService = userService;
        this.folderService = folderService;
    }

    //pressing start command
    public void startCommandHandler(Update update) {
        userService.registerUser(update);
    }

    //pressing create folder
    public void createFolderHandler(Update update) {
        //setting state to CREATE_FOLDER
        userService.setUserState(update.getCallbackQuery().getMessage().getChatId(), UserState.CREATE_FOLDER);
    }

    // folder creating
    public Folder folderCreationHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        String folderName = update.getMessage().getText();

        if (folderName == null || folderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Folder name cannot be empty");
        }

        return folderService.createFolder(folderName, chatId);
    }
}
