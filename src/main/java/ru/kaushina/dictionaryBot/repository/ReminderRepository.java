package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kaushina.dictionaryBot.model.Reminder;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    Reminder findReminderById(long id);
}
