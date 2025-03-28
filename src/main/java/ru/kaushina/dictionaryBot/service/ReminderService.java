package ru.kaushina.dictionaryBot.service;

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

    /**
     * Получает напоминание пользователя по его идентификатору чата.
     * @param chatId идентификатор пользователя
     * @return Reminder - найденное напоминание, {@code null} иначе
     */
    public Reminder getReminder(Long chatId) {
        return reminderRepository.findReminderByUser_ChatId(chatId);
    }

    /**
     * Создает новое напоминание или редактирует существующее с учетом введенных данных.
     * @param chatId идентификатор пользователя
     * @param time значение времени напоминания
     * @return Reminder - созданное напоминание, {@code null} иначе
     */
    public Reminder createReminder(Long chatId, String time) {
        Reminder reminder = reminderRepository.findReminderByUser_ChatId(chatId);
        if (reminder == null)  {
            reminder = new Reminder();
            reminder.setId(chatId);
        }
        Integer days = remindersPending.get(chatId);
        if (days == null) {
            remindersPending.remove(chatId);
            return null;
        }
        reminder.setDaysOfWeek(days);
        reminder.setTime(LocalTime.parse(time));
        reminder.setEnabled(true);
        remindersPending.remove(chatId);
        return reminderRepository.save(reminder);
    }

    /**
     * Метод, проверяющий валидность введенного формата времени с помощью регулярного выражения.
     * @param time - строка с введенным временем
     * @return true, если строка удовлетворяет требованиям, иначе false
     */
    public boolean checkValidTime(String time) {
        String regex = "^(?:[0-1][0-9]|[2][0-3]):(?:[0-5][0-9])$";
        return time.matches(regex);
    }

    /**
     * Получает выбранные дни недели в виде строки, дни разделены запятой.
     * @param reminder напоминание, для которого ищутся дни недели
     * @param chatId идентификатор пользователя
     * @return String - дни недели через запятую или сообщение о том, что не выбран ни один день
     */
    public String getDays(Reminder reminder, Long chatId) {
        List<String> days = getDaysList(reminder, chatId);
        if (!days.isEmpty()) return String.join(", ", days);
        else return MessageTexts.getMessage("message.no_days");
    }

    /**
     * Получает выбранные дни недели в виде списка строк.
     * @param reminder напоминание, для которого ищутся дни недели
     * @param chatId идентификатор пользователя
     * @return List\<String\> - список выбранных дней недели
     */
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

    /**
     * Возвращает состояние напоминания в виде строки.
     * @param reminder напоминание, для которого находится состояние
     * @return String - сообщение о состоянии напоминания
     */
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

    /**
     * Удаляет напоминание.
     * @param chatId идентификатор пользователя
     */
    public void deleteReminder(Long chatId) {
        reminderRepository.delete(reminderRepository.findReminderByUser_ChatId(chatId));
    }

    /**
     * Изменяет состояние напоминания (включен/выключен).
     * @param chatId идентификатор пользователя
     * @return Reminder - обновленное напоминание
     */
    public Reminder changeEnabling(Long chatId) {
        Reminder reminder = reminderRepository.findReminderByUser_ChatId(chatId);
        if (reminder == null) return null;
        reminder.setEnabled(!reminder.isEnabled());
        return reminderRepository.save(reminder);
    }

    /**
     * Изменяет выбранные пользователем дни недели во временной структуре на основе callback-ов.
     * @param callback callback данные
     * @param chatId идентификатор пользователя
     */
    public void changeWeekDays(String callback, Long chatId) {
        Reminder reminder = reminderRepository.findReminderByUser_ChatId(chatId);
        int days;
        if (reminder != null) {
            remindersPending.put(chatId, reminder.getDaysOfWeek());
            days = remindersPending.get(chatId);
        } else if (remindersPending.containsKey(chatId)) {
            days = remindersPending.get(chatId);
        } else {
            remindersPending.put(chatId, 0);
            days = 0;
        }
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
        remindersPending.put(chatId, days);
    }

    /**
     * Проверяет, выбран ли хотя бы один день для перехода на следующий шаг.
     * @param update Объект Update с обновлением
     * @return true, если был выбран день, иначе false
     */
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

    /**
     * Отменяет создание/редактирование напоминания.
     * @param chatId идентификатор пользователя
     */
    public void cancelReminder(Long chatId) {
        remindersPending.remove(chatId);
    }
}
