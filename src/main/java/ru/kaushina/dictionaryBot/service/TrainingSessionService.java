package ru.kaushina.dictionaryBot.service;

import com.mysql.cj.Session;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.service.TrainingSessionService.TrainingSession.SessionWord;

import java.util.*;

@Service
public class TrainingSessionService {

    private final Map<Long, TrainingSession> sessions;
    private final FolderService folderService;

    @Data
    @Builder
    public static class TrainingSession {
        private TrainingMode mode;
        private Long folderId;
        private Long chatId;
        private List<SessionWord> words;
        private int folderSize;
        private int wordIndex;
        private int successfulCount;
        private boolean showAnswer;

        @Data
        @Builder
        public static class SessionWord {
            private Long wordId;
            private boolean remembered;
        }
    }

    public TrainingSessionService(FolderService folderService) {
        sessions = new HashMap<>();
        this.folderService = folderService;
    }

    public SessionWord createRememberSession(Long chatId, Long folderId, String callbackData) {
        List<Word> words = folderService.getFolderWords(folderId);
        if (sessions.containsKey(chatId) || words.isEmpty()) {
            return null;
        }
        List<Word> shuffledWords = new ArrayList<>(words);
        Collections.shuffle(shuffledWords);

        List<SessionWord> sessionWords = shuffledWords.stream()
                .map(word -> SessionWord.builder()
                        .wordId(word.getId())
                        .remembered(false)
                        .build())
                .toList();

        TrainingSession session = TrainingSession.builder()
                .chatId(chatId)
                .folderId(folderId)
                .mode(callbackData.equals("REMEMBER MODE") ? TrainingMode.REMEMBER_MODE : TrainingMode.TEST_MODE)
                .folderSize(sessionWords.size())
                .words(sessionWords)
                .wordIndex(0)
                .successfulCount(0)
                .showAnswer(false)
                .build();

        sessions.put(chatId, session);
        return session.getWords().getFirst();
    }



    public void endRememberSession(Long chatId, Long folderId, String callbackData) {
        sessions.remove(chatId);
    }



}
