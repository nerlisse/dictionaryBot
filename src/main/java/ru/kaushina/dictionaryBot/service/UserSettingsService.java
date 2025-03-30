package ru.kaushina.dictionaryBot.service;

import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.UserSettings;
import ru.kaushina.dictionaryBot.model.enums.ShowMode;
import ru.kaushina.dictionaryBot.repository.UserSettingsRepository;

/**
 * Сервис для работы с настройками для пользователя.
 */
@Service
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    public UserSettingsService(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }

    /**
     * Получает режим показывания термина/значения для пользователя.
     * @param chatId идентификатор пользователя
     * @return ShowMode - текущий режим пользователя
     */
    public ShowMode getShowMode(Long chatId) {
        return userSettingsRepository.findByUser_ChatId(chatId).getShowMode();
    }

    /**
     * Устанавливает режим показывания термина/значения для пользователя.
     *
     */
    public void setShowMode(Long chatId, ShowMode showMode) {
        UserSettings userSettings = userSettingsRepository.findByUser_ChatId(chatId);
        userSettings.setShowMode(showMode);
        userSettingsRepository.save(userSettings);
    }

    /**
     * Устанавливает режим показывания термина/значения для пользователя на противоположный.
     * @param chatId идентификатор пользователя
     * @param callback callback с новым режимом
     * @return UserSettings - текущие настройки пользователя
     */
    public UserSettings changeShowMode(Long chatId, String callback) {
        UserSettings userSettings = userSettingsRepository.findByUser_ChatId(chatId);
        if (callback.equals("SHOW KEY")) {
            userSettings.setShowMode(ShowMode.SHOW_KEY);
        }
        else if (callback.equals("SHOW VALUE")) {
            userSettings.setShowMode(ShowMode.SHOW_VALUE);
        }
        return userSettingsRepository.save(userSettings);
    }

    /**
     * Получает текущий разделитель слов пользователя.
     * @param chatId идентификатор пользователя
     * @return String - разделитель слов
     */
    public String getWordSeparator(Long chatId) {
        return userSettingsRepository.findByUser_ChatId(chatId).getWordSeparator();
    }

    /**
     * Устанавливает разделитель слов для ввода нескольких слов пользователю.
     * @param chatId идентификатор пользователя
     * @param wordSeparator разделитель
     * @return UserSettings - текущие настройки пользователя
     */
    public UserSettings setWordSeparator(Long chatId, String wordSeparator) {
        UserSettings userSettings = userSettingsRepository.findByUser_ChatId(chatId);
        userSettings.setWordSeparator(wordSeparator);
        return userSettingsRepository.save(userSettings);
    }

    /**
     * Получает текущий разделитель термина/значения пользователя.
     * @param chatId идентификатор пользователя
     * @return String - разделитель термина/значения
     */
    public String getTermValueSeparator(Long chatId) {
        return userSettingsRepository.findByUser_ChatId(chatId).getTermValueSeparator();
    }

    /**
     * Устанавливает новый разделитель термина/значения для пользователя.
     * @param chatId идентификатор пользователя
     * @param termValueSeparator новый разделитель
     * @return UserSettings - текущие настройки пользователя
     */
    public UserSettings setTermValueSeparator(Long chatId, String termValueSeparator) {
        UserSettings userSettings = userSettingsRepository.findByUser_ChatId(chatId);
        userSettings.setTermValueSeparator(termValueSeparator);
        return userSettingsRepository.save(userSettings);
    }

    /**
     * Создает настройки для пользователя (разделители установлены по умолчанию).
     * @param user - новый пользователь
     */
    public void createSettings(User user) {
        UserSettings userSettings = new UserSettings();
        userSettings.setUser(user);
        userSettings.setShowMode(ShowMode.SHOW_KEY);
        userSettings.setWordSeparator("\n\n");
        userSettings.setTermValueSeparator(":");
        userSettingsRepository.save(userSettings);
    }
}
