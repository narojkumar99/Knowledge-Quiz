package com.paperplanes.knowquiz.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.paperplanes.knowquiz.R;
import com.paperplanes.knowquiz.core.QuizGame;
import com.paperplanes.knowquiz.core.SoundManager;
import com.paperplanes.knowquiz.fragment.ChoicesFragment;
import com.paperplanes.knowquiz.model.PlayerAnswer;
import com.paperplanes.knowquiz.model.Question;

public class QuizActivity extends ActivityFullscreen
        implements ChoicesFragment.ChoicesClickListener, QuizGame.QuestionTimeoutListener,
                    QuizGame.CurrentQuestionChangedListener,
                    QuizGame.LifeChangeListener, QuizGame.ScoreChangeListener,
                    QuizGame.QuestionAnsweredListener {

    public static final String KEY_TEXT_SCORE = "com.paperplanes.knowquiz.activity.TXT_SCORE";
    public static final String KEY_CORRECT_ANSWER = "com.paperplanes.knowquiz.activity.CORRECT_ANSW";
    public static final String KEY_WRONG_ANSWER = "com.paperplanes.knowquiz.activity.WRONG_ANSW";
    public static final String KEY_IS_HIGH_SCORE = "com.paperplanes.knowquiz.activity.IS_HIGH_SCORE";

    private ViewGroup mRootView;
    private LinearLayout mLayoutLife;
    private TextView mTextCategory;
    private TextView mTextScore;
    private TextView mTextTimer;
    private TextView mTextQuestion;
    private ImageView mImageQuestion;
    private View mLayoutImage;
    private ChoicesFragment mChoicesFragment;
    private ChoicesFragment mChoicesFragmentGrid;
    private ChoicesFragment mChoicesFragmentLinear;

    private QuizGame mGame;
    private Question mCurrQuestion;
    private SoundManager mSoundManager;
    private int mSelectedChoice = -1;
    private int mLastScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        mRootView = (ViewGroup) findViewById(R.id.root_view);
        mLayoutLife = (LinearLayout) findViewById(R.id.layout_lifes);
        mTextCategory = (TextView) findViewById(R.id.text_category);
        mTextScore = (TextView) findViewById(R.id.text_score);
        mTextTimer = (TextView) findViewById(R.id.text_timer);
        mTextQuestion = (TextView) findViewById(R.id.text_question);
        mImageQuestion = (ImageView) findViewById(R.id.image_question);
        mLayoutImage = findViewById(R.id.layout_image);

        mChoicesFragmentGrid = ChoicesFragment.newInstance(ChoicesFragment.LAYOUT_GRID);
        mChoicesFragmentLinear = ChoicesFragment.newInstance(ChoicesFragment.LAYOUT_LINEAR);
        mChoicesFragmentGrid.setChoiceClickListener(this);
        mChoicesFragmentLinear.setChoiceClickListener(this);

        mChoicesFragment = mChoicesFragmentGrid;

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, mChoicesFragment)
                .commit();

        mSoundManager = SoundManager.getInstance(this);

        mGame = new QuizGame(this);
        mGame.addQuestionTimerListener(this);
        mGame.addCurrentQuestionChangedListener(this);
        mGame.addLifeChangeListener(this);
        mGame.addScoreChangeListener(this);
        mGame.addQuestionAnsweredListener(this);

        Bundle iBundle = getIntent().getExtras();
        if (iBundle != null) {
            int categoryId = iBundle.getInt(CategoryActivity.EXTRA_CATEGORY_ID);
            mGame.setCategory(categoryId);
        }

        mLastScore = mGame.getLastHighScore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrQuestion == null) goToNextQuestion();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGame.endGame();
    }

    @Override
    public void onChoiceClick(View choice, int index) {
        mSelectedChoice = index;
        mSoundManager.playSound(SoundManager.SOUND_CLICK);
        mChoicesFragment.setBlockTouch(true);
        mChoicesFragment.runPreAnswerAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                doAnswerCurrentQuestion();
            }
        });
        mGame.getTimer().stop();
    }

    @Override
    public void onDurationChanged(int duration) {
        mTextTimer.setText(String.valueOf(duration));
    }

    @Override
    public void onDurationEnd() {
        mChoicesFragment.setBlockTouch(true);
        mChoicesFragment.runCorrectAnswerAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                goToNextQuestion();
            }
        });
    }

    @Override
    public void onCurrentQuestionChanged(Question newCurrentQuestion) {
        mCurrQuestion = newCurrentQuestion;
        resetView();
        mTextCategory.setText(mCurrQuestion.getCategory().getName());
        mTextQuestion.setText(mCurrQuestion.getText());
        Drawable img = mCurrQuestion.getImage();
        mImageQuestion.setImageDrawable(img);
        if (img == null) {
            mLayoutImage.setVisibility(View.GONE);
            switchChoicesFragment(mChoicesFragmentLinear);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition(mRootView);
            }
        }
        else if (mChoicesFragment.getLayoutType() == ChoicesFragment.LAYOUT_LINEAR) {
            switchChoicesFragment(mChoicesFragmentGrid);

            Animator anim = AnimatorInflater.loadAnimator(this, R.animator.fade_in);
            anim.setTarget(mLayoutImage);
            anim.start();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition(mRootView);
            }
        }

        Animator anim = AnimatorInflater.loadAnimator(this, R.animator.fade_in);
        anim.setTarget(mTextQuestion);
        anim.start();

        mChoicesFragment.setQuestionAnswer(mCurrQuestion.getQuestionAnswer());
    }

    @Override
    public void onLifeChange(int lifes) {
        if (lifes >= 0 && lifes < mLayoutLife.getChildCount()) {
            View view = mLayoutLife.getChildAt(lifes);
            if (view != null) {
                Animator anim = AnimatorInflater.loadAnimator(this, R.animator.fade_out);
                anim.setTarget(view);
                anim.start();
            }
        }
    }

    @Override
    public void onScoreChange(int now, int before) {
        mTextScore.setText(String.valueOf(now));

        Animator anim = AnimatorInflater.loadAnimator(this, R.animator.zoom_in_out);
        anim.setTarget(mTextScore);
        anim.start();
    }

    @Override
    public void onAnswerCorrect(PlayerAnswer answer) {
        mChoicesFragment.runPostAnswerAnimation(true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mSoundManager.playSound(SoundManager.SOUND_CORRECT);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                goToNextQuestion();
            }
        });
    }

    @Override
    public void onAnswerIncorrect(PlayerAnswer answer) {
        mChoicesFragment.runPostAnswerAnimation(false, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mSoundManager.playSound(SoundManager.SOUND_WRONG);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                goToNextQuestion();
            }
        });
    }

    private void resetView() {
        mTextQuestion.setText("");
        mImageQuestion.setImageDrawable(null);
        mChoicesFragment.resetView();
        mTextTimer.setText(String.valueOf(QuizGame.MAX_DURATION));
        mLayoutImage.setVisibility(View.VISIBLE);
    }

    private void doAnswerCurrentQuestion() {
        if (mSelectedChoice < 0) return;
        mGame.answerCurrentQuestion(
                new PlayerAnswer(mSelectedChoice, mCurrQuestion.getQuestionAnswer().getChoice(mSelectedChoice)));
    }

    private void goToNextQuestion() {
        if (mGame.isGameOver()) {
            goToGameOverActivity();
            return;
        }

        if (!mGame.goToNextQuestion()) {
            goToGameOverActivity();
            return;
        }

        mChoicesFragment.setBlockTouch(false);
        mGame.start();
    }

    private void switchChoicesFragment(ChoicesFragment other) {
        if (other != null && other != mChoicesFragment) {
            mChoicesFragment = other;

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, mChoicesFragment)
                    .commit();
        }
    }

    private void goToGameOverActivity() {
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra(KEY_TEXT_SCORE, mGame.getScore());
        intent.putExtra(KEY_CORRECT_ANSWER, mGame.getCorrectAnswerCount());
        intent.putExtra(KEY_WRONG_ANSWER, mGame.getWrongAnswerCount());
        boolean isHighScore = false;
        if (mGame.getScore() > mLastScore) isHighScore = true;
        intent.putExtra(KEY_IS_HIGH_SCORE, isHighScore);
        if (mGame.getCategory() != null) {
            intent.putExtra(CategoryActivity.EXTRA_CATEGORY_ID, mGame.getCategory().getId());
        }
        startActivity(intent);
    }
}
