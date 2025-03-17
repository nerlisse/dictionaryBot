package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.SessionWord;
import ru.kaushina.dictionaryBot.model.TrainingSession;

public interface SessionWordRepository extends JpaRepository<SessionWord, Long> {
    SessionWord findBySessionAndOrderIndex(TrainingSession session, int orderIndex);
}
