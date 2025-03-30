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

/**
 * Сервис для работы с папками пользователей.
 * Обеспечивает создание, поиск и удаление папок, а также управление словами в папках.
 * @see Folder
 * @see FolderRepository
 * @see UserService
 */
@Slf4j
@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserService userService;

    public FolderService(FolderRepository folderRepository, UserService userService) {
        this.folderRepository = folderRepository;
        this.userService = userService;
    }

    /**
     * Находит все папки пользователя по chatId.
     * @param chatId уникальный идентификатор чата пользователя
     * @return {@code List<Folder>} - список папок пользователя
     */
    public List<Folder> findByUser_ChatId(Long chatId) {
        List<Folder> folders = folderRepository.findByUser_ChatId(chatId);
        log.info("Found {} folders for user with chatId: {}", folders.size(), chatId);
        return folders;
    }

    /**
     * Находит папку пользователя по chatId и имени папки.
     * @param chatId уникальный идентификатор чата пользователя
     * @param name название папки
     * @return Folder - найденная папка или null, если папка не найдена
     */
    public Folder findByUser_ChatIdAndName(Long chatId, String name) {
        return folderRepository.findByUser_ChatIdAndName(chatId, name);
    }
    /**
     * Находит папку по идентификатору.
     * @param id идентификатор папки
     * @return Optional с найденной папкой или пустой Optional, если папка не найдена
     */
    public Optional<Folder> findById(Long id) {
        return folderRepository.findById(id);
    }

    /**
     * Создает новую папку для пользователя.
     * @param folderName название новой папки
     * @param chatId уникальный идентификатор чата пользователя
     * @return Folder - созданная папка или null, если создать папку не удалось
     */
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

    /**
     * Удаляет папку
     * @param folder папка для удаления
     */
    public void deleteFolder(Folder folder) {
        folderRepository.delete(folder);
    }

    /**
     * Получает все слова из папки.
     * @param folderId идентификатор папки
     * @return список слов в папке или пустой список, если папка не найдена
     */
    @Transactional
    public List<Word> getFolderWords(Long folderId) {
        Folder folder = folderRepository.findById(folderId).orElse(null);
        return new ArrayList<>(folder.getWords());
    }
}
