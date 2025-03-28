package ru.kaushina.dictionaryBot.service;

import org.apache.tomcat.util.codec.binary.StringUtils;
import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.model.Reminder;
import ru.kaushina.dictionaryBot.repository.ReminderRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с напоминаниями.
 */
@Service
public class ReminderService {

    /**Словарь для сохранения создаваемых запоминаний*/
    private final Map<Long, Integer> remindersPending;
    private final ReminderRepository reminderRepository;

    public ReminderService(ReminderRepository reminderRepository) {
        this.remindersPending = new HashMap<>();
        this.reminderRepository = reminderRepository;
    }

    public Reminder getReminder(Long chatId) {
        return reminderRepository.findReminderByUser_ChatId(chatId);
    }

    public Reminder createReminder(Long chatId) {
        Reminder reminder = new Reminder();
        reminder.setId(chatId);
        Integer days = remindersPending.get(chatId);
        if (days == null) return null;
        reminder.setDaysOfWeek(days);
        reminder.setTime(LocalTime.now()); //change later idk how to create em yet
        reminder.setEnabled(true);
        remindersPending.remove(chatId);
        return reminderRepository.save(reminder);
    }

    public String getDays(Reminder reminder) {
        int reminderDays = reminder.getDaysOfWeek();
        List<String> days = new ArrayList<>();
        if ((reminderDays & 1) != 0) days.add("Пн");
        if ((reminderDays & (1 << 1)) != 0) days.add("Вт");
        if ((reminderDays & (1 << 2)) != 0) days.add("Ср");
        if ((reminderDays & (1 << 3)) != 0) days.add("Чт");
        if ((reminderDays & (1 << 4)) != 0) days.add("Пт");
        if ((reminderDays & (1 << 5)) != 0) days.add("Сб");
        if ((reminderDays & (1 << 6)) != 0) days.add("Вс");
        return String.join(", ", days);
    }

    public String isEnabled(Reminder reminder) {
        return (reminder.isEnabled() ? "Включен✅" : "Выключен❌");
    }

    /**
     * Форматирует время напоминания в строку формата ЧЧ:ММ.
     * @param reminder напоминание, содержащее время
     * @return String - строка времени в формате "08:05" или "23:59"
     */
    public String getTime(Reminder reminder) {
        LocalTime time = reminder.getTime();
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    public void deleteReminder(Long chatId) {
        reminderRepository.delete(reminderRepository.findReminderByUser_ChatId(chatId));
    }
}
