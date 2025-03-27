package ru.kaushina.dictionaryBot.service;


import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.repository.WordRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для работы со словами в папках.
 */
@Service
public class WordService {
    private final FolderService folderService;
    private final WordRepository wordRepository;
    private final Map<Long, String> addedWords;

    public WordService(FolderService folderService, WordRepository wordRepository) {
        this.folderService = folderService;
        this.wordRepository = wordRepository;
        addedWords = new HashMap<>();
    }

    /**
     * Создание слова для папки.
     * @param key термин(слово)
     * @param value значение термина
     * @param folderId идентификатор папки, куда добавляется слово
     * @return Word, если слово было создано, {@code null} при неудаче
     */
    public Word createWord(String key, String value, Long folderId) {
        if (key.isEmpty() || value.isEmpty() || folderId == null) {
            return null;
        }
        if (wordRepository.findByWordKeyAndFolderId(key, folderId) != null) {
            return null;
        }

        Word newWord = new Word();
        newWord.setWordKey(key);
        newWord.setWordValue(value);
        newWord.setFolder(folderService.findById(folderId).orElse(null));

        return wordRepository.save(newWord);
    }

    /**
     * Удаление слова из папки.
     * @param wordKey термин(слово) для удаления
     * @param folderId идентификатор папки, куда добавляется слово
     * @return true, если слово было удалено, иначе false
     */
    public boolean deleteWord(String wordKey, Long folderId) {
        Word word = wordRepository.findByWordKeyAndFolderId(wordKey, folderId);
        if (word == null) {
            return false;
        }
        wordRepository.delete(word);
        return true;
    }

    /**
     * Нахождение слова по идентификатору.
     * @param wordId идентификатор слова
     * @return Word, если слово было найдено, {@code null} иначе
     */
    public Word findById(Long wordId) {
        return wordRepository.findById(wordId).orElse(null);
    }

    /**
     * Добавление термина во временную структуру для хранения, пока пользователь не введет значение.
     * @param folderId идентификатор папки, куда добавляется слово
     * @param key термин(слово)
     * @return true, если термин был добавлен, иначе false
     */
    public boolean addKeyword(Long folderId, String key) {
        System.out.println("contains: " + addedWords.containsKey(folderId) + "\n length: " + key.length());
        if (addedWords.containsKey(folderId) || key.length() > 1000) {
            return false;
        }
        addedWords.put(folderId, key);
        return true;
    }

    /**
     * Добавление слова в папку.
     * @param folderId идентификатор папки, куда добавляется слово
     * @param value введенное значение термина
     * @return Word, если слово было найдено, {@code null} иначе
     */
    public Word addWord(Long folderId, String value) {
        if (addedWords.containsKey(folderId) && value.length() <= 1000) {
            Word word = createWord(addedWords.get(folderId), value, folderId);
            addedWords.remove(folderId);
            return word;
        }
        addedWords.remove(folderId);
        return null;
    }

    /**
     * Отменяет добавление слова.
     * @param chatId идентификатор пользователя, который отменил ввод
     */
    public void cancelAddingWord(Long chatId) {
        addedWords.remove(chatId);
    }

}
