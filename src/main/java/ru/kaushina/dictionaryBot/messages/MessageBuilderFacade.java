package ru.kaushina.dictionaryBot.messages;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.UserSettings;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.model.enums.ShowMode;
import ru.kaushina.dictionaryBot.service.TrainingSessionService;

/**
 * Класс, получающий запросы на постройку сообщения и делегирующий их соответствующим классам.
 * Методы описаны в соответствующих классах.
 * @see FolderBuilder
 * @see WordBuilder
 * @see GameBuilder
 */
@Slf4j
@Component
public class MessageBuilderFacade {

    private final FolderBuilder folderBuilder;
    private final WordBuilder wordBuilder;
    private final GameBuilder gameBuilder;

    public MessageBuilderFacade(FolderBuilder folderBuilder, WordBuilder wordBuilder, GameBuilder gameBuilder) {
        this.folderBuilder = folderBuilder;
        this.wordBuilder = wordBuilder;
        this.gameBuilder = gameBuilder;
    }

    public SendMessage getHomeMessage(Update update) {
        return folderBuilder.getHomeMessage(update);
    }

    public SendMessage createFolderMessage(Update update) {
        return folderBuilder.createFolderMessage(update);
    }

    public SendMessage folderCreatedMessage(Update update, Folder folder) {
        return folderBuilder.folderCreatedMessage(update, folder);
    }

    public SendMessage folderShowMessage(Update update) {
        return folderBuilder.folderShowMessage(update);
    }

    public EditMessageText settingsMessage(UserSettings setting, Update update) {
        return folderBuilder.settingsMessage(setting, update);
    }

    public SendMessage getEasterEggMessage(Update update) {
        return folderBuilder.getEasterEggMessage(update);
    }

    public SendMessage getEasterEgg2Message(Update update) {
        return folderBuilder.getEasterEgg2Message(update);
    }

    public SendMessage addWordMessage(Update update) {
        return wordBuilder.addWordMessage(update);
    }

    public SendMessage failedToAddWordMessage(Update update) {
        return wordBuilder.failedToAddWordMessage(update);
    }

    public SendMessage addValueMessage(Update update) {
        return wordBuilder.addValueMessage(update);
    }

    public SendMessage showWordsMessage(Update update) {
        return wordBuilder.showWordsMessage(update);
    }

    public SendMessage WordCreatedMessage(Update update, Word word) {
        return wordBuilder.WordCreatedMessage(update, word);
    }

    public SendMessage deleteWordMessage(Update update) {
        return wordBuilder.deleteWordMessage(update);
    }

    public SendMessage WordDeletedMessage(Update update, boolean deleted) {
        return wordBuilder.WordDeletedMessage(update, deleted);
    }


    public SendMessage startRememberModeMessage(Update update,
                                                TrainingSessionService.TrainingSession session) {
        return gameBuilder.startRememberModeMessage(update, session);
    }

    public SendMessage failedPlayModeMessage(Update update) {
        return gameBuilder.failedPlayModeMessage(update);
    }

    public EditMessageText showRememberModeMessage(Update update,
                                                   TrainingSessionService.TrainingSession session) {
        return gameBuilder.showRememberModeMessage(update, session);
    }

    public EditMessageText failedSessionMessage(Update update) {
        return gameBuilder.failedSessionMessage(update);

    }

    public SendMessage failedSessionNewMessage(Update update) {
        return gameBuilder.failedSessionNewMessage(update);
    }

    public SendMessage showTestModeMessage(Update update,
                                           TrainingSessionService.TrainingSession session) {
        return gameBuilder.showTestModeMessage(update, session);
    }

    public EditMessageText enterSeparator(Update update) {
        return folderBuilder.enterSeparator(update);
    }

    public SendMessage newSettingsMenu(UserSettings setting, Update update) {
        return folderBuilder.newSettingsMenu(setting, update);
    }
}
