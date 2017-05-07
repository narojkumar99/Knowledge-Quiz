package com.paperplanes.knowquiz.model;

import android.graphics.drawable.Drawable;

/**
 * Created by abdularis on 14/04/17.
 */

public class Question {

    private int mId;
    private String mText;
    private Drawable mImage;
    private QuestionAnswer mQuestionAnswer;
    private Category mCategory;

    public Question() {
        this("", null, null, null);
    }

    public Question(String text, Drawable image, QuestionAnswer questionAnswer, Category category) {
        mText = text;
        mImage = image;
        mQuestionAnswer = questionAnswer;
        mCategory = category;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public Drawable getImage() {
        return mImage;
    }

    public void setImage(Drawable image) {
        mImage = image;
    }

    public QuestionAnswer getQuestionAnswer() {
        return mQuestionAnswer;
    }

    public void setQuestionAnswer(QuestionAnswer questionAnswer) {
        mQuestionAnswer = questionAnswer;
    }

    public Category getCategory() {
        return mCategory;
    }

    public void setCategory(Category category) {
        mCategory = category;
    }

}
