package com.example.springboot_opentrivia.controller;

import java.io.IOException;
import java.util.Arrays;

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

@Controller
public class OpenTriviaController {
    
    @Value("${urlTrivia}")
    private String urlTrivia;
    @GetMapping("/")
    private String getTrivia(Model model) {
        try {
            //Using OkHttp library, you have to create a request and feed it to the HttpClient to get a jsonString from url
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(urlTrivia).get().build();
            Response responses = null;
            try {
               responses = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String jsonString = responses.body().string();
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            model.addAttribute("jsonArray", jsonArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "trivia";
    }
}

