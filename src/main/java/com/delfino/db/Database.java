package com.delfino.db;

import com.delfino.model.QuestionInfo;
import com.delfino.model.QuizInfo;
import com.delfino.model.Room;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Database {

    Map<String, Room> rooms = new HashMap<>();

    ObjectMapper mapper = new ObjectMapper();

    public Room getRoom(String code) {
        return rooms.get(code);
    }

    public String validateUser(String username, String password) {
        String validUsername = "juju";
        String validPass = "quizit";

        if (validPass.equals(password)) {
            return username;
        }
        return null;
    }

    public QuizInfo getQuiz(String hostUser, String quizId) throws IOException {

        return mapper.readValue(getClass().getClassLoader()
                .getResource("questions/" + hostUser + "/" + quizId + ".json"), QuizInfo.class);
    }

    public static QuizInfo getDummyQuiz() {
        QuizInfo quizInfo = new QuizInfo();
        quizInfo.setTitle("Animal quiz");
        quizInfo.setDescription("This is a quiz about animals");

        QuestionInfo q1 = new QuestionInfo();
        q1.setQuestion("Which animal is active at night?");
        q1.setAnswer("bat");
        q1.setOtherOptions(Arrays.asList("cat", "deer", "monkey"));

        QuestionInfo q2 = new QuestionInfo();
        q2.setQuestion("What is the largest mammal in the world?");
        q2.setAnswer("blue whale");
        q2.setOtherOptions(Arrays.asList("hippopotamus", "giraffe", "elephant"));

        QuestionInfo q3 = new QuestionInfo();
        q3.setQuestion("Which bird is a universal symbol of peace?");
        q3.setAnswer("dove");
        q3.setOtherOptions(Arrays.asList("hummingbird", "pigeon", "crow"));

        List<QuestionInfo> questions = Arrays.asList(q1, q2, q3);
        quizInfo.setQuestions(questions);
        return quizInfo;
    }

    public Room addRoom(String newCode, QuizInfo quizInfo) {
        Room room = new Room();
        room.setRoomCode(newCode);
        room.setQuizInfo(quizInfo);
        rooms.put(newCode, room);
        return room;
    }

//    public static void main(String[] args) throws IOException {
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.writeValue(new File("C:\\DataSpark\\IdeaProjects\\quizmaster\\src\\main\\resources\\questions\\juju\\1.json"), getDummyQuiz());
//    }
}
