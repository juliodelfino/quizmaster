package com.delfino.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Room {

    private String roomCode;
    private String hostUser;
    private QuizInfo quizInfo;

    private List<String> players = new ArrayList<>();

    private Map<Integer, List<PlayerAnswer>> questionPlayerAnswerMap = new ConcurrentHashMap<>();
}
