package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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

    @Enumerated(EnumType.STRING)
    private UserState userState;

    private Timestamp registeredAt;
}
