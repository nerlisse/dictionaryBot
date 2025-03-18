package ru.kaushina.dictionaryBot.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TrainingSessionService {

    private final Map<Long, TrainingSession> sessions;

    static class TrainingSession {

    }

    public TrainingSessionService() {
        sessions = new HashMap<>();
    }
}
