package ru.kaushina.dictionaryBot.service;

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
    private final WordService wordService;

    @Data
    @Builder
    public static class TrainingSession {
        private TrainingMode mode;
        private Long folderId;
        private Long chatId;
        private List<SessionWord> words;
        private SessionWord previousWord;
        private int folderSize;
        private int wordIndex;
        private int successfulCount;
        private boolean showAnswer;
        private boolean isOver;

        @Data
        @Builder
        public static class SessionWord {
            private Long wordId;
            private boolean remembered;
        }
    }

    public TrainingSessionService(FolderService folderService, WordService wordService) {
        sessions = new HashMap<>();
        this.folderService = folderService;
        this.wordService = wordService;
    }

    public TrainingSession createTrainingSession(Long chatId, Long folderId, String callbackData) {

        List<SessionWord> sessionWords = shuffleWords(chatId, folderId);
        if (sessionWords == null) {return null;}
        TrainingSession session = TrainingSession.builder()
                .chatId(chatId)
                .folderId(folderId)
                .mode(callbackData.equals("REMEMBER MODE") ? TrainingMode.REMEMBER_MODE : TrainingMode.TEST_MODE)
                .folderSize(sessionWords.size())
                .words(sessionWords)
                .previousWord(null)
                .wordIndex(0)
                .successfulCount(0)
                .showAnswer(false)
                .isOver(false)
                .build();

        sessions.put(chatId, session);
        return session;
    }

    private List<SessionWord> shuffleWords(Long chatId, Long folderId) {
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
        return sessionWords;
    }

    public void endTrainingSession(Long chatId) {
        sessions.remove(chatId);
    }

    public TrainingSession getSession(Long chatId) {
        return sessions.get(chatId);
    }

    public TrainingSession answerRememberMode(Long chatId, String callbackData) {
        TrainingSession session = getSession(chatId);
        if (session == null) return null;
        if (callbackData.equals("REMEMBER")) {
            session.setSuccessfulCount(session.getSuccessfulCount() + 1);
        }
        session.setWordIndex(session.getWordIndex() + 1);
        if (session.getWordIndex() == session.getFolderSize())
            session.setOver(true);
        session.setShowAnswer(false);
        return session;
    }

    public TrainingSession answerTestMode(Long chatId, String message) {
        TrainingSessionService.TrainingSession session = getSession(chatId);
        if (session == null) return null;
        SessionWord wordId = session.getWords().get(session.getWordIndex());
        Word word = wordService.findById(wordId.getWordId());
        String wordValue = word.getWordValue();
        if (message.equalsIgnoreCase(wordValue)) {
            session.setSuccessfulCount(session.getSuccessfulCount() + 1);
            wordId.setRemembered(true);
        }
        session.setPreviousWord(wordId);
        session.setWordIndex(session.getWordIndex() + 1);
        if (session.getWordIndex() == session.getFolderSize())
            session.setOver(true);
        return session;
    }

    public String getStatistics(TrainingSession session) {
        String text = "";
        text += "Training is over! Your results: ";
        text += "\nWords in total: " + session.getFolderSize();
        text += "\nWords Remembered: " + session.getSuccessfulCount();
        double percentage = (session.getSuccessfulCount() * 100.0) / session.getFolderSize();
        text += "\nPercentage of remembered words: " + String.format("%.2f", percentage) + "%";
        text += "\n\n Good job! Keep it up!";
        return text;
    }
}
