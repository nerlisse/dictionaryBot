package ru.kaushina.dictionaryBot.service;

import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.repository.TrainingSessionRepository;

@Service
public class TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final UserService userService;
    private final FolderService folderService;
    private final WordService wordService;
    public TrainingSessionService(TrainingSessionRepository trainingSessionRepository, UserService userService,
                                  FolderService folderService, WordService wordService) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.userService = userService;
        this.folderService = folderService;
        this.wordService = wordService;
    }

    public void startRememberSession() {
        //set userstate to REMEMBER_MODE
        //check if there are any words in the folder
        //create new session
        //call a method for checking the word, pobably should return it or smth
    }
}
