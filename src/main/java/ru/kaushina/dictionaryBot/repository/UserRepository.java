package ru.kaushina.dictionaryBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kaushina.dictionaryBot.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
