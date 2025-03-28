package ru.kaushina.dictionaryBot.service;

import org.apache.tomcat.util.codec.binary.StringUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.model.Reminder;
import ru.kaushina.dictionaryBot.repository.ReminderRepository;
import ru.kaushina.dictionaryBot.util.MessageTexts;

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

    public String getDays(Reminder reminder, Long chatId) {
        List<String> days = getDaysList(reminder, chatId);
        if (!days.isEmpty()) return String.join(", ", days);
        else return MessageTexts.getMessage("message.no_days");
    }

    public List<String> getDaysList(Reminder reminder, Long chatId) {
        int reminderDays;
        if (reminder != null)
            reminderDays = reminder.getDaysOfWeek();
        else if (remindersPending.containsKey(chatId)) {
            reminderDays = remindersPending.get(chatId);
        } else return null;
        List<String> days = new ArrayList<>();
        if ((reminderDays & 1) != 0) days.add("Пн");
        if ((reminderDays & (1 << 1)) != 0) days.add("Вт");
        if ((reminderDays & (1 << 2)) != 0) days.add("Ср");
        if ((reminderDays & (1 << 3)) != 0) days.add("Чт");
        if ((reminderDays & (1 << 4)) != 0) days.add("Пт");
        if ((reminderDays & (1 << 5)) != 0) days.add("Сб");
        if ((reminderDays & (1 << 6)) != 0) days.add("Вс");
        return days;
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

    public Reminder changeEnabling(Long chatId) {
        Reminder reminder = reminderRepository.findReminderByUser_ChatId(chatId);
        if (reminder == null) return null;
        reminder.setEnabled(!reminder.isEnabled());
        return reminderRepository.save(reminder);
    }

    public Integer getPendingReminders(Long chatId) {
        return remindersPending.get(chatId);
    }

    public Reminder changeWeekDays(String callback, Long chatId) {
        Reminder reminder = reminderRepository.findReminderByUser_ChatId(chatId);
        int days;
        if (reminder != null) {
            days = reminder.getDaysOfWeek();
        } else if (remindersPending.containsKey(chatId)) {
            days = remindersPending.get(chatId);
        } else return null;
        switch (callback) {
            case "MONDAY":
                if ((days & 1) == 0) days |= 1;
                else days &= ~1;
                break;
            case "TUESDAY":
                if ((days & (1 << 1)) == 0) days |= (1 << 1);
                else days &= ~(1 << 1);
                break;
            case "WEDNESDAY":
                if ((days & (1 << 2)) == 0) days |= (1 << 2);
                else days &= ~(1 << 2);
                break;
            case "THURSDAY":
                if ((days & (1 << 3)) == 0) days |= (1 << 3);
                else days &= ~(1 << 3);
                break;
            case "FRIDAY":
                if ((days & (1 << 4)) == 0) days |= (1 << 4);
                else days &= ~(1 << 4);
                break;
            case "SATURDAY":
                if ((days & (1 << 5)) == 0) days |= (1 << 5);
                else days &= ~(1 << 5);
                break;
            case "SUNDAY":
                if ((days & (1 << 6)) == 0) days |= (1 << 6);
                else days &= ~(1 << 6);
                break;
            default:
                break;
        }
        if (reminder != null) {
            reminder.setDaysOfWeek(days);
            return reminderRepository.save(reminder);
        }
        else remindersPending.put(chatId, days);
        return null;
    }

    public boolean checkValidDays(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Reminder reminder = reminderRepository.findReminderByUser_ChatId(chatId);
        if (reminder != null) {
            return (reminder.getDaysOfWeek() != 0);
        } else if (remindersPending.containsKey(chatId)) {
            return (remindersPending.get(chatId) != 0);
        }
        return false;
    }
}
