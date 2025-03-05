package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.Folder;

public interface FolderRepository extends JpaRepository<Folder, Long> {
}
