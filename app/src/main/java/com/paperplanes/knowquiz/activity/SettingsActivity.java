package com.paperplanes.knowquiz.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.paperplanes.knowquiz.R;
import com.paperplanes.knowquiz.core.GameSettings;

public class SettingsActivity extends ActivityFullscreen {

    GameSettings mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSettings = GameSettings.getInstance(this);
        CheckBox cbSound = (CheckBox) findViewById(R.id.check_sound);
        cbSound.setChecked(mSettings.isSoundEnabled());
    }

    public void onSoundCheckClick(View view) {
        boolean enable = !mSettings.isSoundEnabled();
        mSettings.setSoundEnabled(enable);
    }
}
