package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name="session_words")
@Getter
@Setter
public class SessionWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private TrainingSession session;

    private Long wordId;
    private int orderIndex;
    private boolean remembered;
}
