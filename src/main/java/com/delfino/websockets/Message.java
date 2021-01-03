package com.delfino.websockets;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {

    private String from;
    private Object data;
    private String command;
}
