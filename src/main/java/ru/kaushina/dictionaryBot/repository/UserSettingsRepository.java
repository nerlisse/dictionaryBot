package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.UserSettings;

/**
 * Репозиторий для работы с настройками для пользователей.
 * Наследует стандартные CRUD-операции от JpaRepository.
 */
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    /**
     * Находит настройки, принадлежащие пользователю с указанным chatId.
     * @param chatId уникальный идентификатор пользователя
     * @return UserSettings - настройки пользователя
     */
    UserSettings findByUser_ChatId(Long chatId);
}
