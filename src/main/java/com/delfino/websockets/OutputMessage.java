package com.delfino.websockets;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
public class OutputMessage extends Message {

    private String time;

    public OutputMessage(Message message) {
        this(message.getFrom(), message.getCommand(), message.getData(),
                new SimpleDateFormat("HH:mm").format(new Date()));
    }

    public OutputMessage(final String from, String command, final Object data, final String time) {

        this.setFrom(from);
        this.setCommand(command);
        this.setData(data);
        this.time = time;
    }
}
