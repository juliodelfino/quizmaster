package com.delfino.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuestionInfo {

    private String question;
    private String answer;
    private List<String> otherOptions;

    public List<String> getChoices() {
        List<String> choices = new ArrayList<>(otherOptions);
        choices.add(answer);
        return choices;
    }
}
