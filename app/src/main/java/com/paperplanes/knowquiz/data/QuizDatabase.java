package com.paperplanes.knowquiz.data;

/**
 * Created by abdularis on 16/04/17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.paperplanes.knowquiz.model.Category;
import com.paperplanes.knowquiz.model.Question;
import com.paperplanes.knowquiz.model.QuestionAnswer;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class QuizDatabase {
    private static final String TAG = QuizDbHelper.class.getSimpleName();

    private static final int MAX_RANDOM_TRY = 12;

    private QuizDbHelper mDbHelper;
    private SQLiteDatabase mReadDB;
    private SQLiteDatabase mWriteDB;

    private SparseArray<Set<Integer>> mUsed;
    private SparseArray<Category> mCategories;
    private ArrayList<Category> mCategoryArray;
    private ArrayList<Integer> mCategoryIds;
    private SparseArray<ArrayList<Integer>> mQuestCat;
    private Random mRand;
    private boolean mInitialized = false;

    private static QuizDatabase sInstance;

    public static QuizDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new QuizDatabase(context);
        }

        return sInstance;
    }

    private QuizDatabase(Context context) {
        init(context);
    }

    private void init(Context context) {
        if (mInitialized) return;

        mInitialized = true;

        mDbHelper = new QuizDbHelper(context);
        mReadDB = mDbHelper.getDatabase();
        mWriteDB = mDbHelper.getDatabase();
        mCategories = new SparseArray<>();
        mCategoryArray = new ArrayList<>();
        mCategoryIds = new ArrayList<>();
        mQuestCat = new SparseArray<>();
        mUsed = new SparseArray<>();
        mRand = new Random();
        mRand.setSeed(System.currentTimeMillis());

        String[] cols;
        cols = new String[]{
                CategoryEntry._ID,
                CategoryEntry.COLUMN_NAME_NAME
        };
        Cursor cur = mReadDB.query(CategoryEntry.TABLE_NAME, cols, null, null, null, null, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            Category cat = new Category(cur.getInt(0), cur.getString(1));
            mCategories.put(cat.getId(), cat);
            mCategoryIds.add(cat.getId());
            mCategoryArray.add(cat);
            cur.moveToNext();
        }
        cur.close();

        cols = new String[]{
                QuestionEntry._ID, QuestionEntry.COLUMN_NAME_CATEGORY
        };
        cur = mReadDB.query(QuestionEntry.TABLE_NAME, cols, null, null, null, null, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            int questionId = cur.getInt(0);
            int categoryId = cur.getInt(1);

            ArrayList<Integer> questionIds = mQuestCat.get(categoryId);
            if (questionIds == null) {
                questionIds = new ArrayList<>();
                mQuestCat.put(categoryId, questionIds);
            }

            questionIds.add(questionId);

            cur.moveToNext();
        }
        cur.close();
    }

    public ArrayList<Category> getCategories() {
        return mCategoryArray;
    }

    public void resetRandom() {
        mUsed.clear();
    }

    public Question getRandomQuestion(Context context, Category category) {

        int categoryId;
        int questionId = 0;
        if (category != null) {
            categoryId = category.getId();
            questionId = getUnusedQuestionId(categoryId);
        } else {
            int tryCount = 0;
            do {
                categoryId = mCategoryIds.get(Math.abs(mRand.nextInt()) % mQuestCat.size());
                questionId = getUnusedQuestionId(categoryId);
                tryCount++;
            } while (tryCount < MAX_RANDOM_TRY && questionId < 0);
        }

        String selection = QuestionEntry._ID + "=? AND " + QuestionEntry.COLUMN_NAME_CATEGORY + " =?";
        String[] selArgs = new String[]{String.valueOf(questionId), String.valueOf(categoryId)};
        String[] cols = {
                QuestionEntry.COLUMN_NAME_TEXT, QuestionEntry.COLUMN_NAME_IMAGE
        };

        Cursor qRes = mReadDB.query(QuestionEntry.TABLE_NAME, cols, selection, selArgs, null, null, null);
        if (qRes.moveToFirst()) {
            byte[] img = qRes.getBlob(1);
            Drawable drawable = null;
            if (img != null) {
                drawable =
                        new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(img, 0, img.length));
            }

            Question q = new Question();
            q.setText(qRes.getString(0));
            q.setImage(drawable);
            q.setQuestionAnswer(getQuestionAnswer(questionId));
            q.setCategory(mCategories.get(categoryId));

            qRes.close();

            return q;
        }

        return null;
    }

    public void insertQuestion(Question q) {
        if (mWriteDB == null) {
            mWriteDB = mDbHelper.getDatabase();
        }

        if (q != null) {
            Bitmap bitmap = ((BitmapDrawable) q.getImage()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] imageData = stream.toByteArray();

            ContentValues values = new ContentValues();
            values.put(QuestionEntry.COLUMN_NAME_TEXT, q.getText());
            values.put(QuestionEntry.COLUMN_NAME_IMAGE, imageData);

            long questionId = mWriteDB.insert(QuestionEntry.TABLE_NAME, null, values);
            if (questionId >= 0) {
                long[] insertedIndex = insertQuestionAnswer(q.getQuestionAnswer(), questionId);
                long correctAnswerIndex = insertedIndex[q.getQuestionAnswer().getCorrect()];

                values.clear();
                values.put(CorrectAnswerEntry.COLUMN_NAME_QUESTION_ID, questionId);
                values.put(CorrectAnswerEntry.COLUMN_NAME_ANSWER_ID, correctAnswerIndex);
                mWriteDB.insert(CorrectAnswerEntry.TABLE_NAME, null, values);
            }
        }
    }

    private long[] insertQuestionAnswer(QuestionAnswer questionAnswer, long questionId) {

        List<String> choices = questionAnswer.getChoices();
        long[] insertedIndex = new long[choices.size()];

        ContentValues values = new ContentValues();
        values.put(AnswerEntry.COLUMN_NAME_QUESTION_ID, questionId);

        for (int i = 0; i < choices.size(); i++) {
            values.put(AnswerEntry.COLUMN_NAME_TEXT, choices.get(i));
            insertedIndex[i] = mWriteDB.insert(AnswerEntry.TABLE_NAME, null, values);
        }

        return insertedIndex;
    }

    private QuestionAnswer getQuestionAnswer(int questionId) {
        String[] columns = {
                AnswerEntry._ID,
                AnswerEntry.COLUMN_NAME_TEXT
        };

        String selection = AnswerEntry.COLUMN_NAME_QUESTION_ID + "=?";
        String[] selectionArgs = {String.valueOf(questionId)};

        Cursor cursor = mReadDB.query(AnswerEntry.TABLE_NAME,
                columns, selection, selectionArgs, null, null, null);

        int correctAnsId = getCorrectAnswerIndex(questionId);

        QuestionAnswer qa = new QuestionAnswer();
        ArrayList<String> choices = qa.getChoices();
        int idx = 0;

        cursor.moveToFirst();
        while (!cursor.isAfterLast() && idx < 4) {
            int answerId = cursor.getInt(0);
            if (answerId == correctAnsId) qa.setCorrect(idx);

            choices.add(idx++, cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();

        // randomize answer
        String str = choices.get(qa.getCorrect());
        Collections.shuffle(choices, mRand);
        qa.setCorrect(choices.indexOf(str));

        return qa;
    }

    private int getCorrectAnswerIndex(int questionId) {
        String[] columns = {
                CorrectAnswerEntry.COLUMN_NAME_ANSWER_ID
        };

        String selection = CorrectAnswerEntry.COLUMN_NAME_QUESTION_ID + "=?";
        String[] selectionArgs = {String.valueOf(questionId)};

        Cursor cursor = mReadDB.query(CorrectAnswerEntry.TABLE_NAME,
                columns, selection, selectionArgs, null, null, null);
        cursor.moveToFirst();

        if (cursor.moveToFirst()) return cursor.getInt(0);
        cursor.close();

        return -1;
    }

    private int getUnusedQuestionId(int categoryId) {
        Set<Integer> usedIds = mUsed.get(categoryId);
        if (usedIds == null) {
            usedIds = new HashSet<>();
            mUsed.put(categoryId, usedIds);
        }

        ArrayList<Integer> questionIds = mQuestCat.get(categoryId);
        if (usedIds.size() >= questionIds.size()) usedIds.clear();
        Log.d(TAG, "UsedIds: " + usedIds.size() + " of " + questionIds.size());

        int questionId;
        int size = questionIds.size();
        int tryCount = 0;
        boolean used;
        do {
            questionId = questionIds.get(Math.abs(mRand.nextInt()) % size);
            used = usedIds.contains(questionId);
            if (!used) {
                usedIds.add(questionId);
                return questionId;
            }
            tryCount++;
        } while (tryCount < MAX_RANDOM_TRY);

        // linear search
        if (usedIds.size() < questionIds.size()) {
            Log.d(TAG, "LinearSearch: [Perform]");
            for (Integer i : questionIds) {
                if (!usedIds.contains(i)) {
                    Log.d(TAG, "LinearSearch: [Found] " + i + " (UnusedId)");

                    usedIds.add(i);
                    return i;
                }
            }
        }

        return -1;
    }


    private class QuizDbHelper extends SimpleSQLiteOpenHelper {

        static final String DB_NAME = "quiz.db";
        static final int DB_VERSION = 1;

        private ArrayList<Pair<Integer, Integer>> mTmpScores;

        QuizDbHelper(Context context) {
            super(context, DB_NAME, DB_VERSION);
        }

        @Override
        public void onBeforeReplaced(SQLiteDatabase db) {
        }

        @Override
        public void onAfterReplaced(SQLiteDatabase db) {
        }
    }


    static abstract class QuestionEntry implements BaseColumns {
        static final String TABLE_NAME = "questions";
        static final String COLUMN_NAME_CATEGORY = "category";
        static final String COLUMN_NAME_TEXT = "text";
        static final String COLUMN_NAME_IMAGE = "image";
    }

    static abstract class AnswerEntry implements BaseColumns {
        static final String TABLE_NAME = "answers";
        static final String COLUMN_NAME_QUESTION_ID = "question_id";
        static final String COLUMN_NAME_TEXT = "text";
    }

    static abstract class CorrectAnswerEntry implements BaseColumns {
        static final String TABLE_NAME = "correct_answer";
        static final String COLUMN_NAME_QUESTION_ID = "question_id";
        static final String COLUMN_NAME_ANSWER_ID = "answer_id";
    }

    static abstract class CategoryEntry implements BaseColumns {
        static final String TABLE_NAME = "category";
        static final String COLUMN_NAME_NAME = "name";
    }

    static abstract class ScoreEntry implements BaseColumns {
        static final String TABLE_NAME = "scores";
        static final String COLUMN_NAME_CATEGORY_ID = "category_id";
        static final String COLUMN_NAME_SCORE = "score";
    }
}
