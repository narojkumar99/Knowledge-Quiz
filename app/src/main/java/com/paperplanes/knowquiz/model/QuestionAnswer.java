package com.paperplanes.knowquiz.model;

import java.util.ArrayList;

/**
 * Created by abdularis on 16/04/17.
 */

public class QuestionAnswer {

    public static final int MAX_CHOICES = 4;

    private ArrayList<String> mChoices;
    private int mCorrect;

    public QuestionAnswer() {
        this(new ArrayList<String>(), 0);
    }

    public QuestionAnswer(ArrayList<String> choices, int correct) {
        mChoices = choices;
        mCorrect = correct;
    }

    public String getChoice(int index) {
        return mChoices.get(index);
    }

    public ArrayList<String> getChoices() {
        return mChoices;
    }

    public void setChoices(ArrayList<String> choices) {
        mChoices = choices;
    }

    public int getCorrect() {
        return mCorrect;
    }

    public void setCorrect(int correct) {
        mCorrect = correct;
    }
}
