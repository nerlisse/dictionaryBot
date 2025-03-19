package ru.kaushina.dictionaryBot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.UserState;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.service.FolderService;
import ru.kaushina.dictionaryBot.service.TrainingSessionService;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.service.WordService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class MessageHandler {

    private final UserService userService;
    private final FolderService folderService;
    private final WordService wordService;
    private final TrainingSessionService trainingSessionService;

    public MessageHandler(UserService userService, FolderService folderService, WordService wordService, TrainingSessionService trainingSessionService) {
        this.userService = userService;
        this.folderService = folderService;
        this.wordService = wordService;
        this.trainingSessionService = trainingSessionService;
    }

    //pressing start command
    public void startCommandHandler(Update update) {
        userService.registerUser(update);
        Long chatId = update.getMessage().getChatId();
        userService.setUserState(chatId, UserState.MAIN_MENU);
        userService.setCurrentFolderId(chatId, null);
        userService.setCurrentWordKey(chatId, null);
        trainingSessionService.endTrainingSession(chatId);
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
        userService.setCurrentFolderId(chatId, null);
        userService.setCurrentWordKey(chatId, null);
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

        if (folderName.length() > 100) {
            log.warn("user {} tried to create a folder with too long name", chatId);
            return null;
        }

        Folder folder = folderService.createFolder(folderName, chatId);
        if (folder != null) {
            log.info("folder {} successfully created for user {}", folderName, chatId);
        }
        else {
            log.warn("folder {} was not created for user {}", folderName, chatId);
        }

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
        Long folderId = userService.getCurrentFolderId(chatId);
        Optional<Folder> folder = folderService.findById(folderId);
        User user = userService.findByChatId(chatId);
        userService.setUserState(chatId, UserState.MAIN_MENU);
        folder.ifPresent(folderService::deleteFolder);
        userService.setCurrentFolderId(chatId, null);
    }

    //user asked to add word, can't resist
    public void askToAddWordHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long folderId = userService.getCurrentFolderId(chatId);
        userService.setCurrentFolderId(chatId, folderId);
        userService.setUserState(chatId, UserState.ADD_KEY);
    }

    public boolean addKeywordHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        Long folderId = userService.getCurrentFolderId(chatId);
        User user = userService.findByChatId(chatId);
        String key = update.getMessage().getText();
        if (key.length()>1000) {
            return false;
        }
        userService.setCurrentWordKey(chatId, key);
        userService.setUserState(chatId, UserState.ADD_VALUE);
        return true;
    }

    public Word addValueHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        String value = update.getMessage().getText();
        String key = userService.getCurrentWordKey(chatId);
        Long folderId = userService.getCurrentFolderId(chatId);
        Word word = wordService.createWord(key, value, folderId);

        if (word != null) {
            log.info("word {} successfully created for user {}", key, chatId);
        }
        else {
            log.warn("word {} was not created for user {}", key, chatId);
        }
        userService.setUserState(chatId, UserState.SHOW_FOLDER);
        userService.setCurrentWordKey(chatId, null);
        return word;
    }

    public void showWordsHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        log.info("Showing words from folder {} to user {}", userService.getCurrentFolderId(chatId) ,chatId);
    }

    public void askToDeleteWordHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        userService.setUserState(chatId, UserState.DELETE_WORD);
    }

    public boolean deleteWordHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        Long folderId = userService.getCurrentFolderId(chatId);
        String word = update.getMessage().getText();
        boolean deleted = wordService.deleteWord(word, folderId);
        userService.setUserState(chatId, UserState.SHOW_FOLDER);
        return deleted;
    }

    public TrainingSessionService.TrainingSession startPlayModeHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long folderId = userService.getCurrentFolderId(chatId);
        String callbackData = update.getCallbackQuery().getData();
        TrainingSessionService.TrainingSession started = trainingSessionService
                .createTrainingSession(chatId, folderId, callbackData);
        if (started != null) {
            userService.setUserState(chatId,
                    (callbackData.equals("REMEMBER MODE") ? UserState.REMEMBER_MODE : UserState.TEST_MODE));
        }
        return started;
    }

    public void endPlayModeHandler(Update update) {
        Long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }
        else {
            chatId = update.getMessage().getChatId();
        }
        trainingSessionService.endTrainingSession(chatId);
        userService.setUserState(chatId, UserState.SHOW_FOLDER);
    }

    public TrainingSessionService.TrainingSession changeAnswerHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        TrainingSessionService.TrainingSession session = trainingSessionService.getSession(chatId);
        if (session!=null) session.setShowAnswer(!session.isShowAnswer());
        return session;
    }

    public TrainingSessionService.TrainingSession answerRememberModeHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        TrainingSessionService.TrainingSession session = trainingSessionService.getSession(chatId);
        if (session == null) return null;
        String callbackData = update.getCallbackQuery().getData();
        if (callbackData.equals("REMEMBER")) {
            session.setSuccessfulCount(session.getSuccessfulCount() + 1);
        }
        session.setWordIndex(session.getWordIndex() + 1);
        if (session.getWordIndex() == session.getFolderSize())
            session.setOver(true);
        session.setShowAnswer(false);
        return session;
    }

    public TrainingSessionService.TrainingSession answerTestModeHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        TrainingSessionService.TrainingSession session = trainingSessionService.getSession(chatId);
        if (session == null) return null;
        String message = update.getMessage().getText();
        TrainingSessionService.TrainingSession.SessionWord wordId = session.getWords().get(session.getWordIndex());
        Word word = wordService.findById(wordId.getWordId());
        String wordValue = word.getWordValue();
        if (message.equalsIgnoreCase(wordValue)) {
            session.setSuccessfulCount(session.getSuccessfulCount() + 1);
            wordId.setRemembered(true);
        }
        session.setPreviousWord(wordId);
        session.setWordIndex(session.getWordIndex() + 1);
        if (session.getWordIndex() == session.getFolderSize())
            session.setOver(true);

        return session;
    }
}
