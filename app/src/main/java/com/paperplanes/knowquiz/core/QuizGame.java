package com.paperplanes.knowquiz.core;

import android.content.Context;
import android.util.Log;

import com.paperplanes.knowquiz.database.QuizDatabase;
import com.paperplanes.knowquiz.model.Category;
import com.paperplanes.knowquiz.model.PlayerAnswer;
import com.paperplanes.knowquiz.model.Question;
import com.paperplanes.knowquiz.util.Timer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abdularis on 16/04/17.
 */

public class QuizGame {
    public static final int MAX_DURATION = 20; // 20 secs

    private static final int MAX_LIFES = 4;
    private static final int MAX_SCORE_PER_QUESTION = 100;

    private int mLifes;
    private int mScore;

    private List<QuestionTimeoutListener> mQuestionTimeoutListeners;
    private List<CurrentQuestionChangedListener> mCurrentQuestionChangedListeners;
    private List<LifeChangeListener> mLifeListeners;
    private List<ScoreChangeListener> mScoreListeners;
    private List<QuestionAnsweredListener> mQAListeners;

    private Timer mTimer;
    private Question mCurrQuestion;
    private boolean mAnswered;
    private boolean mLastAnsweredResult;
    private QuizDatabase mDB;
    private int mRemainingTime;
    private int mCorrectAnswerCount;
    private int mWrongAnswerCount;
    private ArrayList<Category> mCategories;
    private Category mCurrCategory;
    private GameSettings mSettings;

    private final Context mContext;

    public QuizGame(Context context) {
        mLifes = MAX_LIFES;
        mScore = 0;
        mQuestionTimeoutListeners = new ArrayList<>();
        mCurrentQuestionChangedListeners = new ArrayList<>();
        mLifeListeners = new ArrayList<>();
        mScoreListeners = new ArrayList<>();
        mQAListeners = new ArrayList<>();
        mTimer = new Timer(1000);
        mDB = QuizDatabase.getInstance(context);
        mCurrQuestion = null;
        mAnswered = false;
        mLastAnsweredResult = false;
        mRemainingTime = MAX_DURATION;
        mCorrectAnswerCount = 0;
        mWrongAnswerCount = 0;
        mContext = context;
        mCategories = mDB.getCategories();
        mCurrCategory = null;
        mSettings = GameSettings.getInstance(context);

        Timer.OnTimeoutListener timerTimeout = new Timer.OnTimeoutListener() {
            @Override
            public void onTimeout(long ellapsedTime) {
                if (mRemainingTime > 0) {
                    mRemainingTime--;
                    Log.v("MyQuizGame", "onTimout " + mRemainingTime);
                    if (mRemainingTime <= 0) {
                        endCurrentQuestion();
                        callQuestionDurationChangedListeners();
                    }
                    else {
                        callQuestionDurationChangedListeners();
                    }
                }
            }
        };
        mTimer.addOnTimeoutListener(timerTimeout);
    }

    public void endGame() {
        mTimer.stop();
    }

    public boolean answerCurrentQuestion(PlayerAnswer answer) {
        if (mAnswered)
            return mLastAnsweredResult;

        boolean correct = false;
        if (mCurrQuestion != null) {
            mAnswered = true;
            if (mTimer.isStarted()) {
                mTimer.stop();
            }

            correct = mCurrQuestion.getQuestionAnswer().getCorrect() == answer.getAnswer();
            mLastAnsweredResult = correct;
            if (correct) {
                int prevScore = mScore;
                mScore = calculateNewScore();
                mCorrectAnswerCount++;

                callScoreListeners(mScore, prevScore);
            }
            else {
                decreaseLife();
                mWrongAnswerCount++;
            }

            callQuestionAnsweredListeners(answer, correct);
        }

        if (isGameOver()) {
            if (mCurrCategory == null) {
                mSettings.setRandomCategoryScore(mScore);
            }
            else {
                mSettings.setHighScore(mScore, mCurrCategory);
            }
        }

        return correct;
    }

    public boolean goToNextQuestion() {
        if (isGameOver()) return false;

        mCurrQuestion = mDB.getRandomQuestion(mContext, mCurrCategory);
        if (mCurrQuestion != null) {
            callCurrentQuestionChangedListeners();
            mAnswered = false;
            mLastAnsweredResult = false;
            return true;
        }

        return false;
    }

    public boolean start() {
        if (mCurrQuestion == null || mLifes <= 0) return false;
        if (mTimer.isStarted()) mTimer.stop();

        mRemainingTime = MAX_DURATION;

        mTimer.start();
        return true;
    }

    public void setCategory(int categoryId) {
        mCurrCategory = null;
        for (Category cat : mCategories) {
            if (cat.getId() == categoryId) {
                mCurrCategory = cat;
                break;
            }
        }
    }

    public Category getCategory() {
        return mCurrCategory;
    }

    public boolean isGameOver() {
        return mLifes <= 0;
    }

    public void addQuestionTimerListener(QuestionTimeoutListener listener) {
        mQuestionTimeoutListeners.add(listener);
    }

    public void addCurrentQuestionChangedListener(CurrentQuestionChangedListener listener) {
        mCurrentQuestionChangedListeners.add(listener);
    }

    public void addLifeChangeListener(LifeChangeListener listener) {
        mLifeListeners.add(listener);
    }

    public void addScoreChangeListener(ScoreChangeListener listener) {
        mScoreListeners.add(listener);
    }

    public void addQuestionAnsweredListener(QuestionAnsweredListener listener) {
        mQAListeners.add(listener);
    }

    public int getLastHighScore() {
        if (mCurrCategory == null) {
            return mSettings.getRandomCategoryScore();
        }
        return mSettings.getScore(mCurrCategory);
    }

    public int getLifes() {
        return mLifes;
    }

    public int getScore() {
        return mScore;
    }

    public Timer getTimer() {
        return mTimer;
    }

    public int getCorrectAnswerCount() {
        return mCorrectAnswerCount;
    }

    public int getWrongAnswerCount() {
        return mWrongAnswerCount;
    }

    public Question getCurrentQuestion() {
        return mCurrQuestion;
    }

    public void setCurrentQuestion(Question currQuestion) {
        mCurrQuestion = currQuestion;
    }

    private void callQuestionTimerListeners() {
        for (QuestionTimeoutListener listener : mQuestionTimeoutListeners) {
            listener.onDurationEnd();
        }
    }

    private void callQuestionDurationChangedListeners() {
        for (QuestionTimeoutListener listener : mQuestionTimeoutListeners) {
            listener.onDurationChanged(mRemainingTime);
        }
    }

    private void callCurrentQuestionChangedListeners() {
        for (CurrentQuestionChangedListener listener : mCurrentQuestionChangedListeners) {
            listener.onCurrentQuestionChanged(mCurrQuestion);
        }
    }

    private void callLifeListeners(int now) {
        for (LifeChangeListener listener : mLifeListeners) listener.onLifeChange(now);
    }

    private void callScoreListeners(int now, int before) {
        for (ScoreChangeListener listener : mScoreListeners) listener.onScoreChange(now, before);
    }

    private void callQuestionAnsweredListeners(PlayerAnswer answer, boolean correct) {
        for (QuestionAnsweredListener listener : mQAListeners) {
            if (correct) {
                listener.onAnswerCorrect(answer);
            }
            else {
                listener.onAnswerIncorrect(answer);
            }
        }
    }

    private int calculateNewScore() {
        int inc = MAX_SCORE_PER_QUESTION - ((MAX_DURATION - mRemainingTime) * 5);
        return inc + mScore;
    }

    private void decreaseLife() {
        int prev = mLifes;
        mLifes = Math.max(0, mLifes - 1);
        if (prev != mLifes) callLifeListeners(mLifes);
    }

    private void endCurrentQuestion() {
        mCurrQuestion = null;
        mTimer.stop();
        callQuestionTimerListeners();
        decreaseLife();
    }


    public interface QuestionTimeoutListener {
        void onDurationChanged(int duration);
        void onDurationEnd();
    }

    public interface CurrentQuestionChangedListener {
        void onCurrentQuestionChanged(Question newCurrentQuestion);
    }

    public interface LifeChangeListener {
        void onLifeChange(int lifes);
    }

    public interface ScoreChangeListener {
        void onScoreChange(int now, int before);
    }

    public interface QuestionAnsweredListener {
        void onAnswerCorrect(PlayerAnswer answer);
        void onAnswerIncorrect(PlayerAnswer answer);
    }

}
