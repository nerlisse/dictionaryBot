package ru.kaushina.dictionaryBot.service;


import ru.kaushina.dictionaryBot.service.MessageSender;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.config.BotConfig;

@Service
public class TelegramBotService {

    @Setter
    private MessageSender messageSender; // бот

    private final BotConfig config;


    public TelegramBotService(BotConfig config) {
        this.messageSender = null;
        this.config = config;
    }

    // обработка обновлений
    public void handleUpdate(Update update) {
        //handling update
        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        if (update.hasMessage() && update.getMessage().hasText()) {
            switch (message) {
                case "/start":

                    break;
            }
        }
        else if (update.hasCallbackQuery()) {

        }

        //start command?

    }
}
