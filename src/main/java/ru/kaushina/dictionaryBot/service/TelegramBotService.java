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
    }
}
