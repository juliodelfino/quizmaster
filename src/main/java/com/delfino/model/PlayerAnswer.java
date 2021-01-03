package com.delfino.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerAnswer {

    private String username;
    private String answer;
    private boolean correct;
    private long answerTime;
    private long score;
}
