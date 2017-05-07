package com.paperplanes.knowquiz.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.paperplanes.knowquiz.R;
import com.paperplanes.knowquiz.core.SoundManager;

public class GameOverActivity extends ActivityFullscreen {

    private SoundManager mSoundManager;
    private boolean isHighScore;
    private int mLastCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        TextView textScore = (TextView) findViewById(R.id.text_score);
        TextView correctAnswer = (TextView) findViewById(R.id.text_correct_count);
        TextView wrongAnswer = (TextView) findViewById(R.id.text_wrong_count);
        mSoundManager = SoundManager.getInstance(this);

        isHighScore = false;
        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            textScore.setText(String.valueOf(b.getInt(QuizActivity.KEY_TEXT_SCORE)));
            correctAnswer.setText(String.valueOf(b.getInt(QuizActivity.KEY_CORRECT_ANSWER)));
            wrongAnswer.setText(String.valueOf(b.getInt(QuizActivity.KEY_WRONG_ANSWER)));
            isHighScore = b.getBoolean(QuizActivity.KEY_IS_HIGH_SCORE);
            mLastCategory = b.getInt(CategoryActivity.EXTRA_CATEGORY_ID);
        }
        if (!isHighScore) {
            View textHighScore = findViewById(R.id.text_high_score);
            textHighScore.setVisibility(View.GONE);
        }
        else {
            mSoundManager.playSound(SoundManager.SOUND_HIGHSCORE);
        }

        Animator anim = AnimatorInflater.loadAnimator(this, R.animator.zoom_in_out);
        anim.setTarget(textScore);
        anim.start();
    }

    public void onReplayClick(View v) {
        mSoundManager.playSound(SoundManager.SOUND_CLICK);

        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra(CategoryActivity.EXTRA_CATEGORY_ID, mLastCategory);
        startActivity(intent);
    }

    public void onMenuClick(View v) {
        mSoundManager.playSound(SoundManager.SOUND_CLICK);

        NavUtils.navigateUpFromSameTask(this);
    }

    public void onScoreClick(View v) {
        mSoundManager.playSound(SoundManager.SOUND_CLICK);

        Intent intent = new Intent(this, ScoreActivity.class);
        startActivity(intent);
    }
}
