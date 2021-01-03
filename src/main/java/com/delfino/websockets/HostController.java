package com.delfino.websockets;

import com.delfino.db.Database;
import com.delfino.model.QuizInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@Controller
public class HostController {

    private SimpMessagingTemplate template;

    @Autowired
    private Database db;

    @Autowired
    public HostController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @GetMapping("/host")
    public String host() {
        return "host";
    }

    @PostMapping("/host/login")
    public ModelAndView join(@RequestParam(name="username") String username,
                             @RequestParam(name="password") String password, HttpSession session) {

        String status = (db.validateUser(username, password) != null) ? "success" : "fail";
        if ("success".equals(status)) {
            session.setAttribute("currentUsername", username);
        }
        ModelAndView mav = new ModelAndView("host_login_status.json");
        mav.addObject("username", username);
        mav.addObject("status", status);
        return mav;
    }

    @GetMapping("/dashboard")
    public ModelAndView dashboard(HttpSession session) {

        ModelAndView mav = new ModelAndView("host_dashboard");
        mav.addObject("fragmentPage", "fragments/host/quizzes");
        mav.addObject("fragmentSection", "quizzes");
        mav.addObject("username", session.getAttribute("currentUsername"));
        return mav;
    }

    @GetMapping("/create_quiz")
    public ModelAndView showCreateQuiz(HttpSession session) {

        ModelAndView mav = new ModelAndView("host_dashboard");
        mav.addObject("fragmentPage", "fragments/host/create_quiz");
        mav.addObject("fragmentSection", "create_quiz");
        mav.addObject("username", session.getAttribute("currentUsername"));
        return mav;
    }

    @GetMapping("/host_quiz")
    public ModelAndView startGame(@RequestParam(name="id") String quizId, HttpSession session) throws IOException {

        String username = (String)session.getAttribute("currentUsername");
        ObjectMapper mapper = new ObjectMapper();
        ModelAndView mav = new ModelAndView("host_quiz");
        QuizInfo quizInfo = db.getQuiz(username, quizId);
        mav.addObject("quiz", mapper.writeValueAsString(quizInfo));
        String newCode = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        db.addRoom(newCode, quizInfo);
        mav.addObject("code", newCode);
        mav.addObject("username", username);
        return mav;
    }

}
