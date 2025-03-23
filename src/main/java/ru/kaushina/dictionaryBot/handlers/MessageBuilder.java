package ru.kaushina.dictionaryBot.handlers;


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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private SendMessage setNewMessageChatId(Update update) {
        SendMessage sendMessage = new SendMessage();
        Long chatId = getChatId(update);
        sendMessage.setChatId(chatId);
        return sendMessage;
    }

    private Long getChatId(Update update) {
        Long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else chatId = update.getMessage().getChatId();
        return chatId;
    }

    public SendMessage getHomeMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);

        //set text message
        String text = MessageTexts.getMessage("message.hello");
        message.setText(text);

        //set inline markup
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row;



        //find all folders of users
        List<Folder> folders = folderService.findByUser_ChatId(getChatId(update));
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
        SendMessage message = setNewMessageChatId(update);
        String text = "enter new folder name: "; //asking to enter folder name
        message.setText(text);
        return message;
    }


    public SendMessage folderCreatedMessage(Update update, Folder folder) {
        SendMessage message = setNewMessageChatId(update);
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
        SendMessage message = setNewMessageChatId(update);
        Long chatId = getChatId(update);
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
        rowsInline.add(row);

        row = new ArrayList<>();
        row.add(createButton("Remember mode", "REMEMBER MODE"));
        row.add(createButton("Test mode", "TEST MODE"));
        rowsInline.add(row);

        row = new ArrayList<>();
        row.add(createButton("Settings", "SETTINGS"));
        // button for deleting the folder
        row.add(createButton("Delete folder", "DELETE FOLDER"));
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
        SendMessage message = setNewMessageChatId(update);
        //asking to enter word
        String text = "enter new word: ";
        message.setText(text);

        return message;
    }

    public SendMessage failedToAddWordMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);
        String text = "failed to add word, too long. Enter valid value: ";
        message.setText(text);
        return message;
    }

    public SendMessage addValueMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);
        String text = "enter value: "; //asking to enter word
        message.setText(text);
        return message;
    }

    public SendMessage showWordsMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);
        Long chatId = getChatId(update);
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
        SendMessage sendMessage = setNewMessageChatId(update);

        if (word != null) {
            sendMessage.setText("Word created");
        }
        else {
            sendMessage.setText("Couldn't create word. Check if the value's aren't too long, are not empty, or" +
                    "session was not over :(");
        }
        return sendMessage;
    }

    public SendMessage deleteWordMessage(Update update) {
        SendMessage message = setNewMessageChatId(update);
        message.setText("Enter the word to delete");
        return message;
    }

    public SendMessage WordDeletedMessage(Update update, boolean deleted) {
        SendMessage message = setNewMessageChatId(update);
        if (deleted) {
            message.setText("Word deleted");
        }
        else {
            message.setText("Couldn't delete word: no such key");
        }
        return message;
    }

    public SendMessage startRememberModeMessage(Update update,
                                                TrainingSessionService.TrainingSession session) {
        SendMessage message = setNewMessageChatId(update);
        message.setText(textRememberMode(session));
        message.setReplyMarkup(markupRememberMode(session));
        return message;
    }

    private String textRememberMode(TrainingSessionService.TrainingSession session) {
        Word word = wordService.findById(session.getWords().get(session.getWordIndex()).getWordId());

        String wordKey, wordValue;
        if (session.getShowMode().equals(ShowMode.SHOW_KEY)) {
            wordKey = word.getWordKey();
            wordValue = word.getWordValue();
        }
        else {
            wordKey = word.getWordValue();
            wordValue = word.getWordKey();
        }
        String text = "Do you remember that word?\n\n" + wordKey + "\n\n";

        if (session.isShowAnswer()) {
            text += "Answer:\n" + wordValue + "\n\n";
        }

        text += "Progress: " + (session.getWordIndex()+1) + " out of " + session.getFolderSize();

        return text;
    }

    private InlineKeyboardMarkup markupRememberMode(TrainingSessionService.TrainingSession session) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton("I remember", "REMEMBER"));
        row.add(createButton("I do not remember", "DO NOT REMEMBER"));
        rowsInLine.add(row);
        row = new ArrayList<>();
        if (session.isShowAnswer()) {
            row.add(createButton("Hide the answer", "SHOW ANSWER"));
        }
        else {
            row.add(createButton("Show the answer", "HIDE ANSWER"));
        }
        rowsInLine.add(row);

        row = new ArrayList<>();
        row.add(createButton("End the practice", "END PLAY"));
        rowsInLine.add(row);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }


    public SendMessage failedPlayModeMessage(Update update) {
        SendMessage sendMessage = setNewMessageChatId(update);
        sendMessage.setText("failed to start play mode, make sure folder is not empty");
        return sendMessage;
    }

    private String showPreviousAnswer(TrainingSessionService.TrainingSession session) {
        String text = "";
        if (session.getPreviousWord() != null) {
            //show the answer for previous word
            Word prevWord = wordService.findById(session.getPreviousWord().getWordId());
            if (session.getPreviousWord().isRemembered()) {
                text += "Correct!\n";
            }
            else text+= "Incorrect!\n";
            text += "Answer: \n";
            text += (session.getShowMode().equals(ShowMode.SHOW_KEY) ?
                    prevWord.getWordValue() : prevWord.getWordKey()) + "\n\n";
        }
        return text;
    }

    private String endRememberModeMessage(TrainingSessionService.TrainingSession session) {
        String text = showPreviousAnswer(session);
        text += trainingSessionService.getStatistics(session);
        return text;
    }

    private EditMessageText setEditMessageChatId(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(getChatId(update));
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        return editMessageText;
    }

    public EditMessageText showRememberModeMessage(Update update,
                                                   TrainingSessionService.TrainingSession session) {
        EditMessageText editMessageText = setEditMessageChatId(update);
        if (!session.isOver()) {
            editMessageText.setText(textRememberMode(session));
            editMessageText.setReplyMarkup(markupRememberMode(session));
        }
        else {
            editMessageText.setText(endRememberModeMessage(session));
        }
        return editMessageText;
    }

    public EditMessageText failedSessionMessage(Update update) {
        EditMessageText editMessageText = setEditMessageChatId(update);
        editMessageText.setText("Sorry, this session is no longer available:(");
        editMessageText.setReplyMarkup(null);
        return editMessageText;

    }


    public SendMessage failedSessionNewMessage(Update update) {
        SendMessage sendMessage = setNewMessageChatId(update);
        sendMessage.setText("Sorry, this session is no longer available:(");
        return sendMessage;
    }

    public SendMessage showTestModeMessage(Update update,
                                           TrainingSessionService.TrainingSession session) {
        SendMessage sendMessage = setNewMessageChatId(update);
        if (!session.isOver()) {
            sendMessage.setText(textTestMode(session));
            sendMessage.setReplyMarkup(markupTestMode(session));
        } else {
            sendMessage.setText(endRememberModeMessage(session));
        }

        return sendMessage;
    }

    private String textTestMode(TrainingSessionService.TrainingSession session) {
        String text = showPreviousAnswer(session);

        Word word = wordService.findById(session.getWords().get(session.getWordIndex()).getWordId());

        if (session.getShowMode().equals(ShowMode.SHOW_KEY)) {
            text += "Try to remember what that term means and type it down:" +
                    "\n\n" + word.getWordKey() + "\n\n";
        } else {
            text += "Try to remember what that definition refers to and type it down:" +
                    "\n\n" + word.getWordValue() + "\n\n";
        }

        text += "\nKeep in mind that you have to type in exactly how it was " +
        "submitted (case-insensitive) \nIf you can't remember, send any message (don't give up tho)\n\n";

        text += "Progress: " + (session.getWordIndex()+1) + " out of " + session.getFolderSize();
        return text;
    }

    private InlineKeyboardMarkup markupTestMode(TrainingSessionService.TrainingSession session) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(createButton("End the practice", "END PLAY"));
        rowsInLine.add(row);

        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }

    public EditMessageText settingsMessage(ShowMode setting, Update update) {
        EditMessageText editMessageText = setEditMessageChatId(update);

        String text = "Here you can choose whether you want terms to show in play modes or their meanings.\n";
        text += "Current setting: " + (setting.equals(ShowMode.SHOW_KEY) ? "term" : "meaning") + "\n";

        text += "Click on what you would like to see.\n";
        editMessageText.setText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(createButton("Change to term", "SHOW KEY"));
        row.add(createButton("Change to meaning", "SHOW VALUE"));
        rowsInLine.add(row);

        row = new ArrayList<>();
        row.add(createButton("Go back", "SHOW FOLDER"));
        rowsInLine.add(row);

        markup.setKeyboard(rowsInLine);
        editMessageText.setReplyMarkup(markup);
        return editMessageText;
    }

    public SendMessage getEasterEggMessage(Update update) {
        SendMessage sendMessage = setNewMessageChatId(update);
        sendMessage.setText("Хотите, чтобы ваши продажи росли как на дрожжах? Для этого нужно" +
                " всего лишь перед сном... чиmamь в uсточнuкe... https://github.com/Ira11111/FlexCRM");
        return sendMessage;
    }
}
