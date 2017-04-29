package com.paperplanes.knowquiz.model;

/**
 * Created by abdularis on 16/04/17.
 */

public class PlayerAnswer {

    private int mAnswer;
    private String mText;

    public PlayerAnswer(int answer, String text) {
        mAnswer = answer;
        mText = text;
    }

    public int getAnswer() {
        return mAnswer;
    }

    public void setAnswer(int answer) {
        mAnswer = answer;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }
}
