package ru.kaushina.dictionaryBot.service;


import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.User;
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

    public Word createWord(String word, Folder folder) {
        if (word.isEmpty()) {
            //vsyo ploho
        }

        Word newWord = new Word();
        newWord.setWordKey(word);
        newWord.setFolder(folder);

        return wordRepository.save(newWord);
    }

}
