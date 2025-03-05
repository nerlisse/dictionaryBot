package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.*;

@Entity(name="folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "user_chat_id", referencedColumnName = "chatId")
    private User user;


    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Word> words;

}
