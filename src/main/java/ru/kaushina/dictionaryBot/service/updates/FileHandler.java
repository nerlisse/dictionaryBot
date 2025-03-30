package ru.kaushina.dictionaryBot.service.updates;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kaushina.dictionaryBot.bot.MessageSender;
import ru.kaushina.dictionaryBot.messages.MessageBuilderFacade;
import ru.kaushina.dictionaryBot.model.UserSettings;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.model.enums.UserState;
import ru.kaushina.dictionaryBot.service.UserService;
import ru.kaushina.dictionaryBot.service.UserSettingsService;
import ru.kaushina.dictionaryBot.service.WordParsingService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Обработчик файлов со словами.
 */
@Slf4j
@Component
public class FileHandler {

    private final UserService userService;
    private final UserSettingsService userSettingsService;
    private final WordParsingService wordParsingService;
    private final MessageBuilderFacade messageBuilderFacade;
    @Setter
    private MessageSender messageSender;

    public FileHandler(UserService userService, UserSettingsService userSettingsService,
                       WordParsingService wordParsingService, MessageBuilderFacade messageBuilderFacade) {
        this.userService = userService;
        this.userSettingsService = userSettingsService;
        this.wordParsingService = wordParsingService;
        this.messageBuilderFacade = messageBuilderFacade;
    }

    /**
     * Обрабатывает файл, отправленный пользователем.
     * @param update Объект Update с обновлением
     * @param token токен бота
     */
    public void handleFile(Update update, String token) {
        Long chatId = update.getMessage().getChatId();
        UserState state = userService.getUserState(chatId);
        if (!state.equals(UserState.SEND_DOC)) return;
        Document document = update.getMessage().getDocument();
        String fileId = document.getFileId();

        UserSettings settings = userSettingsService.getSettings(chatId);
        String termSeparator = settings.getTermValueSeparator();
        String wordSeparator = settings.getWordSeparator();

        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);

        try {
            File file = messageSender.executeGetFile(getFile); // Получаем файл
            String filePath = file.getFilePath();
            InputStream inputStream = new URL("https://api.telegram.org/file/bot" +
                    token + "/" + filePath).openStream();

            processFile(chatId, inputStream, document.getFileName(), update, termSeparator, wordSeparator);
        } catch (Exception e) {
            log.error("Ошибка загрузки файла: {}", e.getMessage());
        }
    }

    /**
     * Обрабатывает загруженный файл с терминами, парсит содержимое в зависимости от формата файла
     * и сохраняет полученные слова. Отправляет пользователю результат операции.
     * <p>Поддерживаемые форматы:
     * <ul>
     *   <li>TXT - текстовый файл с разделителями</li>
     *   <li>DOCX - документ Microsoft Word</li>
     * </ul>
     * @param chatId идентификатор чата пользователя
     * @param inputStream поток данных загруженного файла
     * @param fileName имя файла (определяет формат обработки)
     * @param update объект Update с обновлениями
     * @param termSeparator разделитель между термином и значением
     * @param wordSeparator разделитель между словами
     * @throws IOException если произошла ошибка чтения файла
     * @throws TelegramApiException если не удалось отправить сообщение пользователю
     *
     * @see WordParsingService
     */
    private void processFile(Long chatId, InputStream inputStream, String fileName, Update update,
                             String termSeparator, String wordSeparator) throws IOException, TelegramApiException {
        List<Word> words;
        SendMessage sendMessage;
        if (fileName.endsWith(".txt")) {
            words = parseTXT(chatId, inputStream, termSeparator, wordSeparator);
            sendMessage = messageBuilderFacade.wordImportMessage(update, words);
        } else if (fileName.endsWith(".docx")) {
            words = parseDocs(chatId, inputStream, termSeparator, wordSeparator);
            sendMessage = messageBuilderFacade.wordImportMessage(update, words);
        } else {
            sendMessage = messageBuilderFacade.failedFileImport(update);
        }
        messageSender.executeMessage(sendMessage);
        userService.setUserState(chatId, UserState.SHOW_FOLDER);

        sendMessage = messageBuilderFacade.folderShowMessage(update);
        messageSender.executeMessage(sendMessage);
    }

    /**
     * Парсит все слова из txt-файла. Обрабатывает некорректность данных.
     * @param chatId идентификатор пользователя
     * @param inputStream поток данных файла
     * @param termSeparator разделитель между термином и значением
     * @param wordSeparator разделитель между словами
     * @return List\<Word\> список созданных слов, {@code null} при ошибке
     * @throws IOException при ошибке в чтении файла
     */
    private List<Word> parseTXT(Long chatId, InputStream inputStream, String termSeparator,
                                String wordSeparator) throws IOException {
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        try {
            return wordParsingService.parseWordsFromText(chatId, text, termSeparator, wordSeparator);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Обрабатывает все слова из docx-файла, обрабатывает некорректность данных.
     * @param chatId идентификатор пользователя
     * @param inputStream поток данных файла
     * @param termSeparator разделитель между термином и значением
     * @param wordSeparator разделитель между словами
     * @return List\<Word\> список созданных слов, {@code null} при ошибке
     * @throws IOException при ошибке в чтении файла
     */
    private List<Word> parseDocs(Long chatId, InputStream inputStream,
                                 String termSeparator, String wordSeparator) throws IOException {
        XWPFDocument doc = new XWPFDocument(inputStream);
        StringBuilder text = new StringBuilder();
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            text.append(paragraph.getText()).append("\n");
        }
        doc.close();
        try {
            return wordParsingService.parseWordsFromText(chatId, text.toString(), termSeparator, wordSeparator);
        }
        catch (RuntimeException e) {
            return null;
        }
    }
}
