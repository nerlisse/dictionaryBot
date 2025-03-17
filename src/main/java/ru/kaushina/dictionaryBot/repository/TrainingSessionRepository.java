package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.TrainingSession;
import ru.kaushina.dictionaryBot.model.User;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    TrainingSession findByUser(User user);
}
