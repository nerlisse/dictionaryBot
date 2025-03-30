package ru.kaushina.dictionaryBot.service.updates;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.bot.MessageSender;
import ru.kaushina.dictionaryBot.model.enums.UserState;
import ru.kaushina.dictionaryBot.service.UserService;

import java.io.InputStream;
import java.net.URL;

@Slf4j
@Component
public class FileHandler {

    private final UserService userService;
    @Setter
    private MessageSender messageSender;

    public FileHandler(UserService userService) {
        this.userService = userService;
    }

    public void handleFile(Update update, String token) {
        Long chatId = update.getMessage().getChatId();
        UserState state = userService.getUserState(chatId);
        if (!state.equals(UserState.SEND_DOC)) return;
        Document document = update.getMessage().getDocument();
        String fileId = document.getFileId();

        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);

        try {
            File file = messageSender.executeGetFile(getFile); // Получаем файл
            String filePath = file.getFilePath();
            InputStream inputStream = new URL("https://api.telegram.org/file/bot" +
                    token + "/" + filePath).openStream();

            processFile(inputStream, document.getFileName(), update);

        } catch (Exception e) {
            log.error("Ошибка загрузки файла: {}", e.getMessage());
        }
    }

    private void processFile(InputStream inputStream, String fileName, Update update) {

    }
}
