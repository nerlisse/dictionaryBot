package ru.kaushina.dictionaryBot.service.updates;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.model.*;
import ru.kaushina.dictionaryBot.model.enums.ShowMode;
import ru.kaushina.dictionaryBot.model.enums.UserState;
import ru.kaushina.dictionaryBot.service.*;

import java.util.Optional;

/**
 * Основной обработчик сообщений для бота, управляющий взаимодействиями с пользователем.
 * Обрабатывает входящие обновления от Telegram и делегирует действия соответствующим сервисам.
 * <p>Обработчик координирует работу между несколькими сервисами.
 * @see UserService
 * @see FolderService
 * @see WordService
 * @see TrainingSessionService
 */
@Slf4j
@Component
public class MessageHandler {

    private final UserService userService;
    private final FolderService folderService;
    private final WordService wordService;
    private final TrainingSessionService trainingSessionService;
    private final ReminderService reminderService;

    public MessageHandler(UserService userService, FolderService folderService, WordService wordService, TrainingSessionService trainingSessionService, ReminderService reminderService) {
        this.userService = userService;
        this.folderService = folderService;
        this.wordService = wordService;
        this.trainingSessionService = trainingSessionService;
        this.reminderService = reminderService;
    }

    /**
     * Обрабатывает команду /start от пользователей.
     * Регистрирует пользователя, сбрасывает его состояние в главное меню и очищает все текущие операции.
     * @param update Объект Update от Telegram, содержащий сообщение /start
     */
    //pressing start command
    public void startCommandHandler(Update update) {
        userService.registerUser(update);
        Long chatId = update.getMessage().getChatId();
        userService.setUserState(chatId, UserState.MAIN_MENU);
        userService.setCurrentFolderId(chatId, null);
        wordService.cancelAddingWord(chatId);
        reminderService.cancelReminder(chatId);
        trainingSessionService.endTrainingSession(chatId);
    }

    /**
     * Возвращает пользователя в главное меню из любого состояния.
     * @param update Объект Update от Telegram, содержащий callback или сообщение, отправляющее на главную страницу
     */
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
        wordService.cancelAddingWord(chatId);
        reminderService.cancelReminder(chatId);
    }

    /**
     * Начинает процесс создания папки.
     * @param update Объект Update с callback-ом создания папки
     */
    public void createFolderHandler(Update update) {
        //setting state to CREATE_FOLDER
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        userService.setUserState(chatId, UserState.CREATE_FOLDER);
    }

    /**
     * Обрабатывает создание папки с указанным именем с учетом валидности имени.
     * @param update Объект Update с названием папки
     * @return Созданный объект Folder или null, если создание не удалось
     * @throws IllegalArgumentException если имя папки пустое
     */
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

    /**
     * Показывает содержимое конкретной папки пользователю и устанавливает соответствующее состояние.
     * @param update Объект Update с callback-ом выбора папки или возвращения в ее меню
     */
    public void showFolderHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        userService.setUserState(chatId, UserState.SHOW_FOLDER);
        userService.setCurrentFolderId(chatId, update.getCallbackQuery().getData());
    }

    /**
     * Удаляет текущую выбранную папку.
     * @param update Объект Update с callback-ом на удаление
     */
    public void deleteFolderHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long folderId = userService.getCurrentFolderId(chatId);
        Optional<Folder> folder = folderService.findById(folderId);
        userService.setUserState(chatId, UserState.MAIN_MENU);
        folder.ifPresent(folderService::deleteFolder);
        userService.setCurrentFolderId(chatId, null);
    }

    /**
     * Начинает процесс добавления слова.
     * @param update Объект Update с callback-ом на добавление слова
     */
    public void askToAddWordHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        userService.setUserState(chatId, UserState.ADD_KEY);
    }

    /**
     * Обрабатывает добавление термина
     * @param update Объект Update с ключевым словом
     * @return true если слово успешно добавлено, false в противном случае
     */
    public boolean addKeywordHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        String key = update.getMessage().getText();
        Long folderId = userService.getCurrentFolderId(chatId);
        boolean added = wordService.addKeyword(folderId, key);
        if (added) {
            userService.setUserState(chatId, UserState.ADD_VALUE);
            return true;
        }
        return false;
    }

    /**
     * Обрабатывает значение при добавлении слова и завершает процесс создания.
     * @param update Объект Update со значением слова
     * @return Созданный объект Word или null, если создание не удалось
     */
    public Word addValueHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        String value = update.getMessage().getText();
        Long folderId = userService.getCurrentFolderId(chatId);
        Word word = wordService.addWord(folderId, value);

        if (word != null) {
            log.info("word successfully created for user {}", chatId);
        }
        else {
            log.warn("word was not created for user {}", chatId);
        }
        userService.setUserState(chatId, UserState.SHOW_FOLDER);
        wordService.cancelAddingWord(chatId);
        return word;
    }

    /**
     * Отображает все слова в текущей папке пользователю.
     * @param update Объект Update, инициирующий отображение слов
     */
    public void showWordsHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        log.info("Showing words from folder {} to user {}", userService.getCurrentFolderId(chatId) ,chatId);
    }

    /**
     * Начинает процесс удаления слова.
     * @param update Объект Update с callback-ом на удаление слова
     */
    public void askToDeleteWordHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        userService.setUserState(chatId, UserState.DELETE_WORD);
    }

    /**
     * Обрабатывает удаление слова на основе ввода пользователя.
     * @param update Объект Update со словом для удаления
     * @return true если слово успешно удалено, false в противном случае
     */
    public boolean deleteWordHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        Long folderId = userService.getCurrentFolderId(chatId);
        String word = update.getMessage().getText();
        boolean deleted = wordService.deleteWord(word, folderId);
        userService.setUserState(chatId, UserState.SHOW_FOLDER);
        return deleted;
    }

    /**
     * Начинает тренировку в режиме запоминания или тестирования.
     * @param update Объект Update с выбором режима тренировки
     * @return Созданный объект TrainingSession или null, если сессия не создалась
     */
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

    /**
     * Завершает текущую тренировку и возвращает к просмотру папки.
     * @param update Объект Update с callback-ом или любым сообщением на завершение тренировки
     */
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

    /**
     * Переключает видимость ответов во время тренировки.
     * @param update Объект Update с запросом на переключение
     * @return Обновленный объект TrainingSession
     */
    public TrainingSessionService.TrainingSession changeAnswerHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        TrainingSessionService.TrainingSession session = trainingSessionService.getSession(chatId);
        if (session!=null) session.setShowAnswer(!session.isShowAnswer());
        return session;
    }

    /**
     * Обрабатывает ответ пользователя в режиме запоминания.
     * @param update Объект Update с ответом пользователя
     * @return Обновленный объект TrainingSession
     */
    public TrainingSessionService.TrainingSession answerRememberModeHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        return trainingSessionService.answerRememberMode(chatId, callbackData);
    }

    /**
     * Обрабатывает ответ пользователя в режиме тестирования.
     * @param update Объект Update с ответом пользователя
     * @return Обновленный объект TrainingSession
     */
    public TrainingSessionService.TrainingSession answerTestModeHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        String message = update.getMessage().getText();
        return trainingSessionService.answerTestMode(chatId, message);
    }

    /**
     * Обрабатывает изменения настроек пользователя.
     * @param update Объект Update с изменением настроек
     * @return Новый ShowMode после применения изменений
     */
    public ShowMode settingsHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return userService.changeSetting(chatId, update.getCallbackQuery().getData());
    }

    /**
     * Обрабатывает нажатие на кнопку "Напоминания" (устанавливает состояние в REMINDER).
     * @param update Объект Update с обновлением
     * @return Reminder - напоминание пользователя, если уже создано, {@code null} иначе
     */
    public Reminder getReminderMenu(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        userService.setUserState(chatId, UserState.REMINDER);
        return reminderService.getReminder(chatId);
    }

    /**
     * Удаляет напоминание пользователя.
     * @param update Объект Update с обновлением
     * @return {@code null} в любом случае, необходимо для корректного отображения меню
     */
    public Reminder deleteReminder(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        reminderService.deleteReminder(chatId);
        return null;
    }

    /**
     * Переключатель включения/выключения напоминания.
     * @param update Объект Update с обновлением
     * @return Reminder - обновленное напоминание
     */
    public Reminder toggleReminderHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return reminderService.changeEnabling(chatId);
    }

    /**
     * Обрабатывает ввод времени, проверяет его на валидность, в случае отказа не создает ничего.
     * @param update Объект Update с обновлением
     * @return Reminder - успешно созданное напоминание, иначе {@code null}
     */
    public Reminder addTimeHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        String time = update.getMessage().getText();
        boolean valid = reminderService.checkValidTime(time);
        Reminder reminder = reminderService.getReminder(chatId);
        System.out.println("reminder: " + reminder);
        if (valid) {
            System.out.println("entered valid zone");
            reminder = reminderService.createReminder(chatId, time);
            System.out.println("reminder: " + reminder);
        }
        reminderService.cancelReminder(chatId);
        userService.setUserState(chatId, UserState.REMINDER);
        return reminder;
    }

    /**
     * Обрабатывает начало изменения напоминания.
     * @param update Объект Update с обновлением
     */
    public void editReminderStart(Update update) {
        reminderService.addToPending(update.getCallbackQuery().getMessage().getChatId());
    }
}
