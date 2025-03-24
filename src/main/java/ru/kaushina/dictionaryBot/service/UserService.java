package ru.kaushina.dictionaryBot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.model.enums.ShowMode;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.enums.UserState;
import ru.kaushina.dictionaryBot.repository.UserRepository;

import java.sql.Timestamp;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value="users", key="#chatId")
    public User findByChatId(Long chatId) {
        return userRepository.findByChatId(chatId);
    }

    public void setUserState(Long chatId, UserState userState) {
        User user = userRepository.findByChatId(chatId);
        if (user != null) {
            log.info("setting user {} state to {}", chatId, userState);
            user.setUserState(userState);
            userRepository.save(user);
            log.info("User state updated successfully: {} -> {}", chatId, userState);
        }
        else {
            log.warn("failed to update state: user with chatId {} not found", chatId);
        }
    }

    public User registerUser(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();

        if (userRepository.findById(chatId).isPresent()) {
            log.warn("User with chatId {} already exists, skip reg", chatId);
            return null;
        }

        Chat chat = message.getChat();
        User user = new User();

        user.setChatId(chatId);
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setUsername(chat.getUserName());
        user.setSetting(ShowMode.SHOW_KEY);

        user.setUserState(UserState.MAIN_MENU);
        user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

        log.info("registered user: {}, chatId: {}", user.getUsername(), chatId);
        return userRepository.save(user);

    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void setLastMessageId(Long chatId, Integer messageId) {
        User user = userRepository.findByChatId(chatId);
        user.setLastMessageId(messageId);
        userRepository.save(user);
    }

    public void setCurrentFolderId(Long chatId, String callback) {
        User user = userRepository.findByChatId(chatId);
        if (user.getCurrentFolderId() == null && callback != null) {
            user.setCurrentFolderId(Long.valueOf(callback.substring(12)));
        }
        log.info("Showing folder {} to user {}", user.getCurrentFolderId(), chatId);
        userRepository.save(user);
    }

    public Long getCurrentFolderId(Long chatId) {
        User user = userRepository.findByChatId(chatId);
        return user.getCurrentFolderId();
    }

    public ShowMode changeSetting(Long chatId, String callback) {
        User user = userRepository.findByChatId(chatId);
        if (callback.equals("SHOW KEY")) {
            user.setSetting(ShowMode.SHOW_KEY);
        }
        else if (callback.equals("SHOW VALUE")) {
            user.setSetting(ShowMode.SHOW_VALUE);
        }
        userRepository.save(user);
        return user.getSetting();
    }

    public ShowMode getShowMode(Long chatId) {
        User user = userRepository.findByChatId(chatId);
        return user.getSetting();
    }
}
