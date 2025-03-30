package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.kaushina.dictionaryBot.model.Reminder;

import java.util.List;

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

    /**
     * Находит все активные напоминания с помощью запроса.
     * @return List\<Reminder\> - список активных напоминаний
     */
    @Query("SELECT r FROM reminders r WHERE r.enabled = true")
    List<Reminder> findActiveReminders();
}
