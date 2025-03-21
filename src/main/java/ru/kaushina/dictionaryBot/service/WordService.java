package ru.kaushina.dictionaryBot.service;


import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.repository.WordRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class WordService {
    private final FolderService folderService;
    private final UserService userService;
    private final WordRepository wordRepository;
    private final Map<Long, String> addedWords;

    public WordService(FolderService folderService, UserService userService, WordRepository wordRepository) {
        this.folderService = folderService;
        this.userService = userService;
        this.wordRepository = wordRepository;
        addedWords = new HashMap<>();
    }

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

    public boolean deleteWord(String wordKey, Long folderId) {
        Word word = wordRepository.findByWordKeyAndFolderId(wordKey, folderId);
        if (word == null) {
            return false;
        }
        wordRepository.delete(word);
        return true;
    }

    public Word findById(Long wordId) {
        return wordRepository.findById(wordId).orElse(null);
    }

    public boolean addKeyword(Long folderId, String key) {
        System.out.println("contains: " + addedWords.containsKey(folderId) + "\n length: " + key.length());
        if (addedWords.containsKey(folderId) || key.length() > 1000) {
            return false;
        }
        addedWords.put(folderId, key);
        return true;
    }

    public String getKeyword(Long folderId) {
        if (addedWords.containsKey(folderId)) {
            return addedWords.get(folderId);
        }
        return null;
    }

    public Word addWord(Long folderId, String value) {
        if (addedWords.containsKey(folderId) && value.length() <= 1000) {
            return createWord(addedWords.get(folderId), value, folderId);
        }
        addedWords.remove(folderId);
        return null;
    }

}
