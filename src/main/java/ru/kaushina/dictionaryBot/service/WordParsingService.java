package ru.kaushina.dictionaryBot.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kaushina.dictionaryBot.model.Word;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для парсинга слов из текстовых данных.
 * Обеспечивает обработку текста с разделителями и сохранение слов в базу данных в рамках транзакции.
 */
@Component
public class WordParsingService {

    private final WordService wordService;
    private final UserService userService;

    public WordParsingService(WordService wordService, UserService userService) {
        this.wordService = wordService;
        this.userService = userService;
    }

    /**
     * Парсит текст на пары "термин-значение" и сохраняет их в базу данных.
     * Выполняется в транзакции - при ошибке все изменения откатываются.
     * @param chatId идентификатор пользователя
     * @param text текст для парсинга
     * @param termSeparator разделитель между парами "термин-значение"
     * @param wordSeparator разделитель между термином и его значением
     * @return List\<Word\> список созданных объектов
     * @throws RuntimeException если:
     *         - текст не содержит ни одной пары (пустой после разделения)
     *         - найдена некорректная пара (не соответствует формату)
     *         - не удалось создать слово
     */
    @Transactional
    public List<Word> parseWordsFromText(Long chatId, String text, String termSeparator, String wordSeparator) {
        String[] pairs = text.split(termSeparator);
        if (pairs.length == 0) {
            throw new RuntimeException();
        }
        List<Word> words = new ArrayList<>();
        for (String pair : pairs) {
            String[] word = pair.split(wordSeparator);
            if (word.length == 0) continue;
            if (word.length != 2) {
                throw new RuntimeException();
            }
            Word newWord = wordService.createWord(word[0], word[1], userService.getCurrentFolderId(chatId));
            if (newWord == null) {
                throw new RuntimeException();
            }
        }
        return words;
    }
}
