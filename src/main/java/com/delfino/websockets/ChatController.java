package com.delfino.websockets;

import com.delfino.db.Database;
import com.delfino.model.PlayerAnswer;
import com.delfino.model.QuestionInfo;
import com.delfino.model.QuizInfo;
import com.delfino.model.Room;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    private SimpMessagingTemplate template;

    @Autowired
    private Database db;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ChatController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public OutputMessage send(final Message message) {

        return new OutputMessage(message);
    }

    @MessageMapping("/room/{roomCode}")
    public OutputMessage greet(@DestinationVariable String roomCode, Message message) throws IOException {
        OutputMessage outMsg = new OutputMessage(message);
        Room room = db.getRoom(roomCode);
        if (room == null) {
            outMsg.setCommand("ERROR");
            outMsg.setData("Room code not available.");
        }
        else if ("JOIN".equals(message.getCommand())) {
            room.getPlayers().add(message.getFrom());
        } else if ("SHOW_USERS".equals(message.getCommand())) {
            outMsg.setData(mapper.writeValueAsString(room.getPlayers()));
        } else if ("SHOW_QUESTION".equals(message.getCommand())) {
            buildQuestionInfo(outMsg, room);
        } else if ("SHOW_PLAYER_ANSWERS".equals(message.getCommand())) {
            buildPlayerAnswers(outMsg, room);
        } else if ("SHOW_LEADERBOARD".equals(message.getCommand())) {
            buildLeaderboard(outMsg, room);
        } else if ("SHOW_OVERALL_LEADERBOARD".equals(message.getCommand())) {
            buildLeaderboardOverall(outMsg, room);
        }
        return outMsg;
    }

    private void buildLeaderboard(OutputMessage outMsg, Room room) throws JsonProcessingException {
        QuizInfo quiz = room.getQuizInfo();
        List currentAnswerPlayerStats = room.getQuestionPlayerAnswerMap().get(room.getQuestionPlayerAnswerMap().size());
        outMsg.setData(mapper.writeValueAsString(currentAnswerPlayerStats));
    }

    private void buildLeaderboardOverall(OutputMessage outMsg, Room room) throws JsonProcessingException {
        QuizInfo quiz = room.getQuizInfo();
        Map<String, Long> playerCumulativeScores = room.getQuestionPlayerAnswerMap().values().stream().flatMap(Collection::stream)
                .collect(Collectors.groupingBy(p -> p.getUsername(), Collectors.summingLong(p -> p.getScore())));
        List<Map> finalPlayerStats = playerCumulativeScores.entrySet().stream().map(e -> {
            Map uStats = new HashMap();
            uStats.put("username", e.getKey());
            uStats.put("score", e.getValue());
            return uStats;
        }).sorted((p1, p2) -> Long.compare((long)p2.get("score"), (long)p1.get("score")))
                .collect(Collectors.toList());
        outMsg.setData(mapper.writeValueAsString(finalPlayerStats));
    }

    private void buildPlayerAnswers(OutputMessage outMsg, Room room) throws JsonProcessingException {
        QuizInfo quiz = room.getQuizInfo();
        int questionNo = (Integer)outMsg.getData();
        QuestionInfo questionInfo = quiz.getQuestions().get(questionNo-1);
        List<PlayerAnswer> playerAnswers = room.getQuestionPlayerAnswerMap().get(questionNo);
        Map<String, Long> map = playerAnswers.stream().collect(Collectors.groupingBy(PlayerAnswer::getAnswer, Collectors.counting()));
        List finalStats = questionInfo.getChoices().stream().map(k -> {
            Map aStats = new HashMap();
            aStats.put("answer", k);
            aStats.put("correct", questionInfo.getAnswer().equals(k));
            aStats.put("count", map.getOrDefault(k, 0L));
            return aStats;
        }).collect(Collectors.toList());
        outMsg.setData(mapper.writeValueAsString(finalStats));
    }

    private void buildQuestionInfo(OutputMessage outMsg, Room room) throws JsonProcessingException {
        QuizInfo quiz = room.getQuizInfo();
        int questionNo = (Integer)outMsg.getData();
        QuestionInfo qInfo = quiz.getQuestions().get(questionNo-1);
        Map map = new HashMap<>();
        map.put("number", questionNo);
        map.put("questionText", qInfo.getQuestion());
        List choices = qInfo.getChoices();
        Collections.shuffle(choices);
        map.put("choices", choices);
        room.getQuestionPlayerAnswerMap().put(questionNo, new ArrayList<>());
        outMsg.setData(mapper.writeValueAsString(map));
    }

    @GetMapping("/join")
    public String join() {
        return "join";
    }

    @PostMapping("/verify")
    public ModelAndView  verifyRoom(@RequestParam(name="code") String code) {

        String status = (db.getRoom(code) != null) ? "success" : "fail";

        ModelAndView mav = new ModelAndView("login_status.json");
        mav.addObject("username", null);
        mav.addObject("code", code);
        mav.addObject("status", status);
        return mav;
    }

    @PostMapping("/login")
    public ModelAndView  join(@RequestParam(name="username") String username,
                       @RequestParam(name="code") String code) {

        String status = (db.getRoom(code) != null) ? "success" : "fail";

        ModelAndView mav = new ModelAndView("login_status.json");
        mav.addObject("username", username);
        mav.addObject("code", code);
        mav.addObject("status", status);
        return mav;
    }

    @PostMapping("/room")
    public ModelAndView enterRoom(@RequestParam(name="username") String username,
                              @RequestParam(name="code") String code) {

        ModelAndView mav = new ModelAndView("room");
        mav.addObject("username", username);
        mav.addObject("code", code);
        return mav;
    }

    @PostMapping("/post_answer")
    public ModelAndView postAnswer(@RequestParam(name="answer") String answer,
                                   @RequestParam(name="duration") int duration,
                                   @RequestParam(name="questionNumber") int questionNo,
                                   @RequestParam(name="username") String username,
                                   @RequestParam(name="roomCode") String roomCode) {

        Room room = db.getRoom(roomCode);
        Map<Integer, List<PlayerAnswer>> map = room.getQuestionPlayerAnswerMap();
        List playerAnswers = map.getOrDefault(questionNo, new ArrayList<>());
        map.put(questionNo, playerAnswers);
        PlayerAnswer playerAnswer = new PlayerAnswer();
        playerAnswer.setAnswer(answer);
        playerAnswer.setScore(30000 - duration);
        playerAnswer.setUsername(username);
        QuestionInfo questionInfo = room.getQuizInfo().getQuestions().get(questionNo-1);
        if (questionInfo.getAnswer().equals(answer)) {
            playerAnswer.setCorrect(true);
            playerAnswer.setAnswerTime(System.currentTimeMillis());
        }
        playerAnswers.add(playerAnswer);

        ModelAndView mav = new ModelAndView("login_status.json");
        mav.addObject("username", null);
        mav.addObject("code", roomCode);
        mav.addObject("status", playerAnswer.isCorrect() ? "correct" : "wrong");
        return mav;
    }

}
