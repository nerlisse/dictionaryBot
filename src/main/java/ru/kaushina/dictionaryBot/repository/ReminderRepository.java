package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.Reminder;

/**
 * Репозиторий для работы с напоминаниями для пользователей.
 * <p>
 * Наследует стандартные CRUD-операции от JpaRepository.
 */
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    /**
     * Находит напоминание, принадлежащее пользователю с указанным chatId.
     * @param chatId уникальный идентификатор пользователя в Telegram
     * @return напоминание пользователя
     */
    Reminder findReminderByUser_ChatId(long chatId);
}
