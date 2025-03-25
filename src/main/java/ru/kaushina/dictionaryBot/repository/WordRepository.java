package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.Word;

/**
 * Репозиторий для работы со словами в папках.
 * <p>
 * Предоставляет методы для поиска слов по различным критериям.
 * Наследует стандартные CRUD-операции от JpaRepository.
 */
public interface WordRepository extends JpaRepository<Word, Long> {
    /**
     * Находит слово по его значению (термину) и идентификатору папки.
     *
     * @param word     термин
     * @param folderId идентификатор папки, в которой осуществляется поиск
     * @return найденное слово или {@code null}, если не существует
     */
    Word findByWordKeyAndFolderId(String word, Long folderId);
}
