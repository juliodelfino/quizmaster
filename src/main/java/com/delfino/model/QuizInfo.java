package com.delfino.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizInfo {

    private String title;
    private String description;
    private List<QuestionInfo> questions;
}
