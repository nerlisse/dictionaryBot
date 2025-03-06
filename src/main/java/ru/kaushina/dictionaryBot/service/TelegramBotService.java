package ru.kaushina.dictionaryBot.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kaushina.dictionaryBot.handlers.MessageBuilder;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.UserState;
import ru.kaushina.dictionaryBot.repository.FolderRepository;
import ru.kaushina.dictionaryBot.repository.UserRepository;
import ru.kaushina.dictionaryBot.repository.WordRepository;
import ru.kaushina.dictionaryBot.service.MessageSender;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.config.BotConfig;

import java.sql.Timestamp;

@Service
public class TelegramBotService {

    @Setter
    private MessageSender messageSender; // бот

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private MessageBuilder messageBuilder;

    private final BotConfig config;


    public TelegramBotService(BotConfig config) {
        this.messageSender = null;
        this.config = config;
    }

    // обработка обновлений
    public void handleUpdate(Update update) throws TelegramApiException {
        //handling update

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (message.equals("/start")) {
                    registerUser(update);
                    SendMessage sendMessage = messageBuilder.getHomeMessage(update);
                    messageSender.executeMessage(sendMessage);
                    return;
            }
            User user = userRepository.findByChatId(chatId);
            if (user.getUserState().equals(UserState.CREATE_FOLDER)) {
                SendMessage sendMessage = messageBuilder.folderCreatedMessage(update);
                messageSender.executeMessage(sendMessage);
                messageSender.executeMessage(messageBuilder.getHomeMessage(update));
            }
        }
        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long messageId = Long.valueOf(update.getCallbackQuery().getMessage().getMessageId());
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("CREATE NEW FOLDER")) {
                SendMessage sendMessage = messageBuilder.createFolderMessage(update);
                messageSender.executeMessage(sendMessage);
            }
        }

        //start command?

    }

    private void registerUser(Update update) {
        Message message = update.getMessage();
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            long chatId = message.getChatId();
            Chat chat = message.getChat();
            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUsername(chat.getUserName());

            user.setUserState(UserState.MAIN_MENU);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            //log.info("registered user: {}", user.getUserName());
        }
    }
}
