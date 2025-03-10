package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.Word;

public interface WordRepository extends JpaRepository<Word, Long> {
    Word findByWordKeyAndFolder(String word, Long folderId);
}
