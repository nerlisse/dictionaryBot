package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.User;

/**
 * Репозиторий для работы с пользователями бота.
 * <p>
 * Предоставляет методы для поиска пользователей по различным критериям.
 * Наследует стандартные CRUD-операции от JpaRepository.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Находит пользователя по его chatId в Telegram.
     *
     * @param chatId уникальный идентификатор пользователя в Telegram
     * @return найденный пользователь или {@code null}, если не существует
     */
    User findByChatId(Long chatId);
}
