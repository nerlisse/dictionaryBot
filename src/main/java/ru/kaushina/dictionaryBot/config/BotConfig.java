package ru.kaushina.dictionaryBot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Конфигурационный класс для настроек Telegram бота.
 * <p>
 * Загружает свойства бота (имя и токен) из файла {@code application.properties}
 * и предоставляет к ним доступ через геттеры.
 *
 * @see Configuration
 * @see PropertySource
 */
@Configuration
@PropertySource("classpath:application.properties")
@Getter
public class BotConfig {

    /**Имя бота, загружаемое из свойства {@code bot.name} в файле конфигурации.*/
    @Value("${bot.name}")
    private String botName;

    /**Токен бота, загружаемый из свойства {@code bot.token} в файле конфигурации.
     * * <b>Внимание:</b> Токен должен храниться в секрете и не попадать в публичные репозитории.*/
    @Value("${bot.token}")
    private String token;
}
