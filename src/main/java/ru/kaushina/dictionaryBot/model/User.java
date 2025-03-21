package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

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

    @Column(length=1000)
    private String currentWordKey;
    private ShowMode setting;

    private Timestamp registeredAt;
}
