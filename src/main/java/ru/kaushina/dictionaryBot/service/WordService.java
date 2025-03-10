package ru.kaushina.dictionaryBot.service;


import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.repository.FolderRepository;
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


}
