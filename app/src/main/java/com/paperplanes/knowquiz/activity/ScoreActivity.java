package com.paperplanes.knowquiz.activity;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.paperplanes.knowquiz.R;
import com.paperplanes.knowquiz.core.GameSettings;
import com.paperplanes.knowquiz.data.QuizDatabase;
import com.paperplanes.knowquiz.model.Category;

import java.util.ArrayList;

public class ScoreActivity extends ActivityFullscreen {

    QuizDatabase mDb;
    GameSettings mSettings;
    ArrayList<Score> mScores;
    ScoreAdapter mScoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        ListView listView = (ListView) findViewById(R.id.list_view_score);
        mScores = new ArrayList<>();

        mSettings = GameSettings.getInstance(this);
        mDb = QuizDatabase.getInstance(this);
        Score catRandom = new Score();
        catRandom.category = getResources().getString(R.string.cat_random_category);
        catRandom.score = mSettings.getRandomCategoryScore();
        mScores.add(catRandom);
        ArrayList<Category> categories = mDb.getCategories();
        for (Category category : categories) {
            Score score = new Score();
            score.category = category.getName();
            score.score = mSettings.getScore(category);

            mScores.add(score);
        }

        mScoreAdapter = new ScoreAdapter(this, R.layout.item_score);
        mScoreAdapter.setData(mScores);
        listView.setAdapter(mScoreAdapter);
    }

    public void onResetScoreClick(View view) {
        for (Score s : mScores) {
            s.score = 0;
        }
        mScoreAdapter.setData(mScores);
        mSettings.resetScore(mDb.getCategories());
    }

    class Score {
        String category;
        int score;
    }

    class ScoreAdapter extends ArrayAdapter<Score> {

        private int mRes;
        private Context mContext;
        private ArrayList<Score> mData;

        public ScoreAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
            mRes = resource;
            mContext = context;
        }

        public void setData(ArrayList<Score> data) {
            super.clear();
            super.addAll(data);
            mData = data;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            Holder holder;

            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                view = inflater.inflate(mRes, parent, false);

                holder = new Holder();
                holder.textCategory = (TextView) view.findViewById(R.id.text_category_name);
                holder.textScore = (TextView) view.findViewById(R.id.text_category_score);
                view.setTag(holder);
            }

            holder = (Holder) view.getTag();
            Score score = mData.get(position);
            holder.textCategory.setText(score.category);
            holder.textScore.setText(String.valueOf(score.score));

            return view;
        }

        class Holder {
            TextView textCategory;
            TextView textScore;
        }
    }
}
