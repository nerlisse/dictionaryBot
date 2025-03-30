package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import ru.kaushina.dictionaryBot.model.enums.ShowMode;

/**
 * Сущность, представляющая настройки пользователя.
 */
@Entity(name="user_settings")
@Getter
@Setter
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    @OneToOne
    @JoinColumn(name = "user_chat_id", referencedColumnName = "chatId")
    private User user;

    @Enumerated(EnumType.STRING)
    private ShowMode showMode;

    @ColumnDefault(": ")
    private String wordSeparator;
    @ColumnDefault("\n\n")
    private String termValueSeparator;
}
