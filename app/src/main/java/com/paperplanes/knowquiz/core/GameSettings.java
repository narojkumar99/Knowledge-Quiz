package com.paperplanes.knowquiz.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.paperplanes.knowquiz.model.Category;

import java.util.List;

/**
 * Created by abdularis on 27/04/17.
 */

public class GameSettings {
    private static final String SETTINGS_NAME = "QuizGameSettingsValue";
    private static final String SETTINGS_KEY_SOUND = "SettingSound";
    private static final String SETTINGS_KEY_RANDOM_CATEGORY = "RandomCategory";

    private SharedPreferences mPref;

    private static GameSettings sInstance;

    public static GameSettings getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new GameSettings(context);
        }

        return sInstance;
    }

    private GameSettings(Context context) {
        mPref = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSoundEnabled() {
        return mPref.getBoolean(SETTINGS_KEY_SOUND, false);
    }

    public void setSoundEnabled(boolean enabled) {
        mPref.edit().putBoolean(SETTINGS_KEY_SOUND, enabled).apply();
    }

    public int getRandomCategoryScore() {
        return mPref.getInt(SETTINGS_KEY_RANDOM_CATEGORY, 0);
    }

    public void setRandomCategoryScore(int score) {
        int lastScore = getRandomCategoryScore();
        if (score > lastScore) {
            mPref.edit()
                    .putInt(SETTINGS_KEY_RANDOM_CATEGORY, score)
                    .apply();
        }
    }

    public int getScore(Category category) {
        return mPref.getInt(category.getName(), 0);
    }

    public void setHighScore(int score, Category category) {
        int lastScore = getScore(category);
        if (score > lastScore) {
            mPref.edit().putInt(category.getName(), score).apply();
        }
    }

    public void resetScore(List<Category> categories) {
        SharedPreferences.Editor edit = mPref.edit();
        edit.putInt(SETTINGS_KEY_RANDOM_CATEGORY, 0);
        for (Category cat : categories) {
            edit.putInt(cat.getName(), 0);
        }
        edit.apply();
    }

}
