package ru.kaushina.dictionaryBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DictionaryBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DictionaryBotApplication.class, args);
	}

}
