package ru.kaushina.dictionaryBot.service;

import org.springframework.stereotype.Service;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.UserState;
import ru.kaushina.dictionaryBot.repository.FolderRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserService userService;

    public FolderService(FolderRepository folderRepository, UserService userService) {
        this.folderRepository = folderRepository;
        this.userService = userService;
    }

    public List<Folder> findByUser_ChatId(Long chatId) {
        return folderRepository.findByUser_ChatId(chatId);
    }

    public Folder findByUser_ChatIdAndName(Long chatId, String name) {
        return folderRepository.findByUser_ChatIdAndName(chatId, name);
    }

    public void save(Folder folder) {
        folderRepository.save(folder);
    }

    public Folder createFolder(String folderName, Long chatId) {
        User user = userService.findByChatId(chatId);
        if (findByUser_ChatIdAndName(chatId, folderName) != null)
            return null;

        Folder folder = new Folder(); // creating folder
        folder.setName(folderName);

        // connecting to user
        folder.setUser(user);

        // adding words
        folder.setWords(new ArrayList<>());

        // going back to main menu
        userService.setUserState(chatId, UserState.MAIN_MENU);
        userService.save(user);
        // saving
        return folderRepository.save(folder);

    }
}
