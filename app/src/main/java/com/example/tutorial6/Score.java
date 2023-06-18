package com.example.tutorial6;

public class Score {
    private String name;
    private String email;
    private int final_score;

    public Score(String name, String email, int final_score) {
        this.name = name;
        this.email = email;
        this.final_score = final_score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getFinal_score() {
        return final_score;
    }

    public void setFinal_score(int final_score) {
        this.final_score = final_score;
    }
}
