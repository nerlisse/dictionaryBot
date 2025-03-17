package ru.kaushina.dictionaryBot.service;

import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.model.SessionWord;
import ru.kaushina.dictionaryBot.model.TrainingSession;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.repository.SessionWordRepository;

@Service
public class SessionWordService {

    private final SessionWordRepository sessionWordRepository;

    public SessionWordService(SessionWordRepository sessionWordRepository) {
        this.sessionWordRepository = sessionWordRepository;
    }

    public SessionWord createSessionWord(Word word, TrainingSession session, int i) {
        SessionWord sessionWord = new SessionWord();
        sessionWord.setSession(session);
        sessionWord.setOrderIndex(i);
        sessionWord.setWordId(word.getId());
        sessionWord.setRemembered(false);
        return sessionWord;
    }

    public void deleteSessionWord(SessionWord sessionWord) {
        sessionWordRepository.delete(sessionWord);
    }

    public SessionWord getSessionWord(TrainingSession session, int index) {
        return sessionWordRepository.findBySessionAndOrderIndex(session, index);
    }
}
