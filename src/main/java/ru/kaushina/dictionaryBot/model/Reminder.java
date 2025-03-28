package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Сущность напоминания для пользователя.
 */
@Entity(name="reminders")
@Getter
@Setter
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_chat_id", referencedColumnName = "chatId")
    private User user;

    private int daysOfWeek; //битовая маска :0
    private LocalTime time;

    private boolean enabled;
}
