package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.TrainingSession;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
}
