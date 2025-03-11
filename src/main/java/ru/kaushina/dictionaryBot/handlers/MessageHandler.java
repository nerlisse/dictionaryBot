package ru.kaushina.dictionaryBot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.UserState;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.service.FolderService;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.service.WordService;

import java.util.Optional;

@Slf4j
@Component
public class MessageHandler {

    private final UserService userService;
    private final FolderService folderService;
    private final WordService wordService;

    public MessageHandler(UserService userService, FolderService folderService, WordService wordService) {
        this.userService = userService;
        this.folderService = folderService;
        this.wordService = wordService;
    }

    //pressing start command
    public void startCommandHandler(Update update) {
        userService.registerUser(update);
        userService.setUserState(update.getMessage().getChatId(), UserState.MAIN_MENU);
    }

    public void homeHandler(Update update) {
        Long chatId;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        }
        else {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }
        userService.setUserState(chatId, UserState.MAIN_MENU);
    }

    //pressing create folder
    public void createFolderHandler(Update update) {
        //setting state to CREATE_FOLDER
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        userService.setUserState(chatId, UserState.CREATE_FOLDER);
    }

    // folder creating
    public Folder folderCreationHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        String folderName = update.getMessage().getText();

        if (folderName == null || folderName.trim().isEmpty()) {
            log.warn("user {} tried to create a folder with an empty name", chatId);
            throw new IllegalArgumentException("Folder name cannot be empty");
        }

        Folder folder = folderService.createFolder(folderName, chatId);
        if (folder != null) {
            log.info("folder {} successfully created for user {}", folderName, chatId);
        }
        else {
            log.warn("folder {} was not created for user {}", folderName, chatId);
        }

        User user = userService.findByChatId(chatId);
        userService.setUserState(chatId, UserState.MAIN_MENU);


        return folder;
    }

    public void showFolderHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        log.info("Showing folder {} to user {}", update.getCallbackQuery().getData().substring(12) ,chatId);
        userService.setUserState(chatId, UserState.SHOW_FOLDER);
        userService.setCurrentFolderId(chatId, Long.valueOf(update.getCallbackQuery().getData().substring(12)));
    }


    public void deleteFolderHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long folderId = Long.valueOf(update.getCallbackQuery().getData().substring(14));
        Optional<Folder> folder = folderService.findById(folderId);
        User user = userService.findByChatId(chatId);
        userService.setUserState(chatId, UserState.MAIN_MENU);
        folder.ifPresent(folderService::deleteFolder);

    }

    //user asked to add word, can't resist
    public void askToAddWordHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long folderId = Long.valueOf(update.getCallbackQuery().getData().substring(19));
        userService.setCurrentFolderId(chatId, folderId);
        userService.setUserState(chatId, UserState.ADD_KEY);
    }

    public void addKeywordHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long folderId = userService.getCurrentFolderId(chatId);
        folderService.findById(folderId).ifPresentOrElse(
                folder -> {
                    Word word = wordService.createWord(update.getMessage().getText(), folder);
                    userService.setUserState(chatId, UserState.ADD_VALUE);
                },
                () -> {
                    log.info("No such folder {} for user {}, returning to main menu", folderId, chatId);
                    userService.setUserState(chatId, UserState.MAIN_MENU);
                    userService.setCurrentFolderId(chatId, null);
                }
        );
    }

    public void addValueHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        User user = userService.findByChatId(chatId);
        String value = update.getMessage().getText();

        Long folderId = userService.getCurrentFolderId(chatId);
        //Word word = wordService.createWord(value, folderId);
    }

    public void showWordsHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        log.info("Showing words from folder {} to user {}", update.getCallbackQuery().getData().substring(23) ,chatId);
    }
}
