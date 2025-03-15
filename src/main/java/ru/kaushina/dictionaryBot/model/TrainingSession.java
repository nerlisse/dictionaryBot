package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity(name="training_sessions")
@Getter
@Setter
public class TrainingSession {

    @Id
    private long chatId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "chat_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @Enumerated(EnumType.STRING)
    private TrainingMode mode;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionWord> sessionWords;

    private Long currentWordId; // id of current word
    private int wordsCount;     // количество слов которое прошли в общем
    private int wordsLength;    // сколько всего слов
    private int successfulWordCount; // на скольких словах нажали помню

}
