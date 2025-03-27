package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.Folder;

import java.util.List;

/**
 * Репозиторий для работы с папками пользователей.
 * <p>
 * Предоставляет методы для поиска папок по различным критериям.
 * Наследует стандартные CRUD-операции от JpaRepository.
 */
public interface FolderRepository extends JpaRepository<Folder, Long> {
    /**
     * Находит все папки, принадлежащие пользователю с указанным chatId.
     * @param chatId уникальный идентификатор пользователя в Telegram
     * @return список папок пользователя (может быть пустым)
     */
    List<Folder> findByUser_ChatId(Long chatId);

    /**
     * Находит конкретную папку по chatId пользователя и названию папки.
     *
     * @param chatId уникальный идентификатор пользователя в Telegram
     * @param name   название папки
     * @return найденная папка или {@code null}, если не существует
     */
    Folder findByUser_ChatIdAndName(Long chatId, String name);
}
