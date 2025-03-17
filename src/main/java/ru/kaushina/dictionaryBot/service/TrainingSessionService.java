package ru.kaushina.dictionaryBot.service;

import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.model.*;
import ru.kaushina.dictionaryBot.repository.TrainingSessionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final UserService userService;
    private final FolderService folderService;
    private final SessionWordService sessionWordService;
    public TrainingSessionService(TrainingSessionRepository trainingSessionRepository, UserService userService,
                                  FolderService folderService, SessionWordService sessionWordService) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.userService = userService;
        this.folderService = folderService;
        this.sessionWordService = sessionWordService;
    }

    public boolean startRememberSession(Long chatId, Long folderId) {
        //check if there are any words in the folder
        List<Word> words = folderService.getFolderWords(folderId);
        if (words.isEmpty()) {
            //return smth like couldn't enter the mode and go back
            return false;
        }
        //set userstate to REMEMBER_MODE
        userService.setUserState(chatId, UserState.REMEMBER_MODE);
        //create new session
        TrainingSession session = new TrainingSession();
        session.setUser(userService.findByChatId(chatId));
        session.setFolder(folderService.findById(folderId).orElse(null));
        session.setMode(TrainingMode.REMEMBER);

        //set words in random order
        Collections.shuffle(words);
        List<SessionWord> sessionWords = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            //create sessionword
            SessionWord sessionWord = sessionWordService.createSessionWord(words.get(i), session, i);
            sessionWords.add(sessionWord);
        }
        session.setSessionWords(sessionWords);
        session.setWordsCount(0);
        session.setSuccessfulWordCount(0);
        session.setWordsLength(words.size());

        trainingSessionRepository.save(session);
        return true;
    }

    public void endRememberSession(Long chatId, Long folderId) {
        TrainingSession session = trainingSessionRepository.findByChatId(chatId);
        List<SessionWord> sessionWords = session.getSessionWords();
        for (SessionWord sessionWord : sessionWords) {
            sessionWordService.deleteSessionWord(sessionWord);
        }
        trainingSessionRepository.delete(session);
    }
}
