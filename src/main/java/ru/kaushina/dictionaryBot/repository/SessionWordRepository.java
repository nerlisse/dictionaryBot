package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.SessionWord;

public interface SessionWordRepository extends JpaRepository<SessionWord, Long> {
}
