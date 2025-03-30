package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_chat_id", referencedColumnName = "chatId")
    private User user;

    @Enumerated(EnumType.STRING)
    private ShowMode showMode;

    @Column(columnDefinition = "varchar(10) default ': '")
    private String wordSeparator;
    @Column(columnDefinition = "varchar(10) default '\\n\\n'")
    private String termValueSeparator;
}
