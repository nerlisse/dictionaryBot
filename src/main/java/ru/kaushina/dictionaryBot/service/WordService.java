package ru.kaushina.dictionaryBot.service;


import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.repository.WordRepository;

@Service
public class WordService {
    private final FolderService folderService;
    private final UserService userService;
    private final WordRepository wordRepository;

    public WordService(FolderService folderService, UserService userService, WordRepository wordRepository) {
        this.folderService = folderService;
        this.userService = userService;
        this.wordRepository = wordRepository;
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

    public Word getWordById(Long wordId) {
        return wordRepository.findById(wordId).orElse(null);
    }

}
