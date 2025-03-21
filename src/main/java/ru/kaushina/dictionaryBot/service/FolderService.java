package ru.kaushina.dictionaryBot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kaushina.dictionaryBot.model.Folder;
import ru.kaushina.dictionaryBot.model.User;
import ru.kaushina.dictionaryBot.model.enums.UserState;
import ru.kaushina.dictionaryBot.model.Word;
import ru.kaushina.dictionaryBot.repository.FolderRepository;

import java.util.*;

@Slf4j
@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserService userService;

    public FolderService(FolderRepository folderRepository, UserService userService) {
        this.folderRepository = folderRepository;
        this.userService = userService;
    }

    public List<Folder> findByUser_ChatId(Long chatId) {
        List<Folder> folders = folderRepository.findByUser_ChatId(chatId);
        log.info("Found {} folders for user with chatId: {}", folders.size(), chatId);
        return folders;
    }

    public Folder findByUser_ChatIdAndName(Long chatId, String name) {
        return folderRepository.findByUser_ChatIdAndName(chatId, name);
    }

    public Optional<Folder> findById(Long id) {
        return folderRepository.findById(id);
    }

    public void save(Folder folder) {
        folderRepository.save(folder);
    }

    public Folder createFolder(String folderName, Long chatId) {
        User user = userService.findByChatId(chatId);

        if (user == null) {
            log.error("can't create folder {}: user with chatId {} not found", folderName, chatId);
            return null;
        }

        if (findByUser_ChatIdAndName(chatId, folderName) != null) {
            log.warn("folder with name {} for user {} already exists", folderName, chatId);
            return null;
        }

        try {
            Folder folder = new Folder(); // creating folder
            folder.setName(folderName);

            // connecting to user
            folder.setUser(user);

            // adding words
            folder.setWords(new ArrayList<>());

            Folder savedFolder = folderRepository.save(folder);

            // going back to main menu
            userService.setUserState(chatId, UserState.MAIN_MENU);
            userService.save(user);

            return savedFolder;
        } catch (Exception e) {
            log.error("Error creating folder {}: {}" , folderName, e.getMessage());
        }
        return null;

    }

    public void deleteFolder(Folder folder) {
        folderRepository.delete(folder);
    }

    @Transactional
    public List<Word> getFolderWords(Long folderId) {
        Folder folder = folderRepository.findById(folderId).orElse(null);
        return new ArrayList<>(folder.getWords());
    }

}
