package ru.kaushina.dictionaryBot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Сущность, представляющая папку со словами.
 */
@Entity(name="folders")
@Getter
@Setter
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
