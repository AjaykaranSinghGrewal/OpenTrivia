package com.example.springboot_opentrivia.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import jakarta.servlet.http.HttpSession;

@Controller
public class OpenTriviaController {

    @Value("${urlTrivia}")
    private String urlTrivia;

    @GetMapping("/")
    private String getTrivia(@RequestParam(value = "selectedAnswer", required = false) String selectedAnswer,
            @RequestParam(value = "index", required = false) Integer indexParam,
            @RequestParam(value = "action", required = false) String action,
            Model model, HttpSession session) {
        try {
            // Load questions into session on first visit
            String questionsJson = (String) session.getAttribute("questionsJson");
            if (questionsJson == null) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(urlTrivia).get().build();
                Response responses = client.newCall(request).execute();
                String jsonString = responses.body().string();
                JSONArray jsonArray = new JSONObject(jsonString).getJSONArray("results");
                questionsJson = jsonArray.toString();
                session.setAttribute("questionsJson", questionsJson);
                session.setAttribute("index", 0);
                session.setAttribute("score", 0);
            }

            JSONArray questions = new JSONArray((String) session.getAttribute("questionsJson"));
            int index = session.getAttribute("index") == null ? 0 : (Integer) session.getAttribute("index");
            int score = session.getAttribute("score") == null ? 0 : (Integer) session.getAttribute("score");

            if ("next".equals(action)) {
                index = Math.min(index + 1, questions.length());
                session.setAttribute("index", index);
                model.addAttribute("selectedAnswer", null);
            } else if (selectedAnswer != null) {
                JSONObject current = questions.getJSONObject(index);
                String correct = current.getString("correct_answer");
                if (correct.equals(selectedAnswer)) {
                    score++;
                    session.setAttribute("score", score);
                    model.addAttribute("feedback", "Correct!");
                } else {
                    model.addAttribute("feedback", "Incorrect! Correct: " + correct);
                }
                model.addAttribute("selectedAnswer", selectedAnswer);
            }

            model.addAttribute("score", score);

            if (index >= questions.length()) {
                model.addAttribute("finished", true);
                model.addAttribute("finalScore", score);
                // end the HTTP session now that the quiz is complete
                session.invalidate();
            } else {
                JSONObject q = questions.getJSONObject(index);
                model.addAttribute("question", q.getString("question"));

                JSONArray incorrect = q.getJSONArray("incorrect_answers");
                List<String> answers = new ArrayList<>();
                answers.add(q.getString("correct_answer"));
                for (int i = 0; i < incorrect.length(); i++) {
                    answers.add(incorrect.getString(i));
                }
                model.addAttribute("answers", answers);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "trivia";
    }
    

}

