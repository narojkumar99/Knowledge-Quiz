package com.paperplanes.knowquiz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.paperplanes.knowquiz.R;
import com.paperplanes.knowquiz.core.SoundManager;

public class MainMenuActivity extends ActivityFullscreen {

    private SoundManager mSoundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mSoundManager = SoundManager.getInstance(this);
    }

    public void onPlayClick(View v) {
        goToActivity(CategoryActivity.class);
    }

    public void onSettingsClick(View v) {
        goToActivity(SettingsActivity.class);
    }

    public void onScoreClick(View v) {
        goToActivity(ScoreActivity.class);
    }

    public void onAboutClick(View v) {
        goToActivity(AboutActivity.class);
    }

    private void goToActivity(Class activityClass) {
        mSoundManager.playSound(SoundManager.SOUND_CLICK);

        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }
}
