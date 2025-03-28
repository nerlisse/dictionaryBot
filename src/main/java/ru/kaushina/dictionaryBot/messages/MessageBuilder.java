package ru.kaushina.dictionaryBot.messages;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.enums.ShowMode;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.service.FolderService;
import ru.kaushina.dictionaryBot.service.TrainingSessionService;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.service.WordService;
import ru.kaushina.dictionaryBot.util.MessageTexts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Класс, ответственный за постройку сообщений бота.
 */
@Slf4j
@Component
public class MessageBuilder {

    private final UserService userService;
    private final FolderService folderService;
    private final WordService wordService;
    private final TrainingSessionService trainingSessionService;

    public MessageBuilder(UserService userService, FolderService folderService, WordService wordService,
                          TrainingSessionService trainingSessionService) {
        this.userService = userService;
        this.folderService = folderService;
        this.wordService = wordService;
        this.trainingSessionService = trainingSessionService;
    }

}
