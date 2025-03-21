package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name="words")
@Getter
@Setter
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=1000)
    private String wordKey;
    @Column(length=1000)
    private String wordValue;

    @ManyToOne
    @JoinColumn(name="folder_id", referencedColumnName = "id")
    private Folder folder;
}
