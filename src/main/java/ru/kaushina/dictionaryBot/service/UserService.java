package ru.kaushina.dictionaryBot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.UserState;
import ru.kaushina.dictionaryBot.repository.UserRepository;

import java.sql.Timestamp;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByChatId(Long chatId) {
        return userRepository.findByChatId(chatId);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void setUserState(Long chatId, UserState userState) {
        User user = userRepository.findByChatId(chatId);
        user.setUserState(userState);
        userRepository.save(user);
    }

    public User registerUser(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();

        if (userRepository.findById(chatId).isPresent()) {
            return null;
        }

        Chat chat = message.getChat();
        User user = new User();

        user.setChatId(chatId);
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setUsername(chat.getUserName());

        user.setUserState(UserState.MAIN_MENU);
        user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

        return userRepository.save(user);
        //log.info("registered user: {}", user.getUserName());
    }

    public void save(User user) {
        userRepository.save(user);
    }

}
