package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.*;

@Entity(name="words")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String key;
    private String value;

    @ManyToOne
    @JoinColumn(name="folder_id", referencedColumnName = "id")
    private Folder folder;
}
