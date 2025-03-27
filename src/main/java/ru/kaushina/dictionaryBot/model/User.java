package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.kaushina.dictionaryBot.model.enums.ShowMode;
import ru.kaushina.dictionaryBot.model.enums.UserState;

import java.sql.Timestamp;

/**
 * Сущность пользователя.
 */
@Entity(name="users")
@Getter
@Setter
public class User {

    @Id
    private Long chatId;

    private String username;
    private String firstName;
    private String lastName;

    private Integer lastMessageId;

    @Enumerated(EnumType.STRING)
    private UserState userState;

    private Long currentFolderId;

    @Enumerated(EnumType.STRING)
    private ShowMode setting;

    private Timestamp registeredAt;
}
