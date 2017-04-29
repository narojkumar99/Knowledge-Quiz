package com.paperplanes.knowquiz.fragment;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.paperplanes.knowquiz.R;
import com.paperplanes.knowquiz.model.QuestionAnswer;

import java.util.ArrayList;

public class ChoicesFragment extends Fragment {

    public static final int LAYOUT_GRID = 0;
    public static final int LAYOUT_LINEAR = 1;

    public static final int CHOICE_A = 0;
    public static final int CHOICE_B = 1;
    public static final int CHOICE_C = 2;
    public static final int CHOICE_D = 3;

    private int mLayout = LAYOUT_GRID;
    private Button[] mChoices;
    private int mSelected = -1;
    private QuestionAnswer mQuestionAnswer;
    private ChoicesClickListener mListener;
    private boolean mBlockTouch = false;

    public static ChoicesFragment newInstance(int layoutType) {
        ChoicesFragment fragment = new ChoicesFragment();
        fragment.mLayout = layoutType;
        return fragment;
    }

    public ChoicesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        switch (mLayout) {
            case LAYOUT_GRID :
                return inflater.inflate(R.layout.fragment_choices_grid, container, false);
            case LAYOUT_LINEAR :
                return inflater.inflate(R.layout.fragment_choices_linear, container, false);
        }

        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mChoices = new Button[4];

        mChoices[0] = (Button) view.findViewById(R.id.btn_choice_a);
        mChoices[1] = (Button) view.findViewById(R.id.btn_choice_b);
        mChoices[2] = (Button) view.findViewById(R.id.btn_choice_c);
        mChoices[3] = (Button) view.findViewById(R.id.btn_choice_d);

        mChoices[0].setTag(CHOICE_A);
        mChoices[1].setTag(CHOICE_B);
        mChoices[2].setTag(CHOICE_C);
        mChoices[3].setTag(CHOICE_D);

        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBlockTouch) {
                    mSelected = (int)v.getTag();
                    if (mListener != null) mListener.onChoiceClick(v, mSelected);
                }
            }
        };

        for (Button b : mChoices) b.setOnClickListener(onClick);

        setQuestionAnswer(mQuestionAnswer);
    }

    public void runPreAnswerAnimation(Animator.AnimatorListener callback) {
        if (mSelected < 0)
            return;

        Button selected = mChoices[mSelected];
        Animator anim = AnimatorInflater.loadAnimator(getActivity(), R.animator.blink);
        anim.setTarget(selected);
        if (callback != null) anim.addListener(callback);
        anim.start();
    }

    public void runPostAnswerAnimation(boolean correct, Animator.AnimatorListener callback) {
        if (mQuestionAnswer == null)
            return;

        Button selectedBtn = mChoices[mSelected];
        if (correct) {
            selectedBtn.setTextColor(getResources().getColor(R.color.btn_correct_text));
            selectedBtn.setBackgroundResource(R.drawable.bg_green);

            Animator anim = AnimatorInflater.loadAnimator(getActivity(), R.animator.blink);
            anim.setTarget(selectedBtn);
            if (callback != null) anim.addListener(callback);
            anim.start();
        }
        else {
            selectedBtn.setBackgroundResource(R.drawable.bg_red);

            runCorrectAnswerAnimation(callback);
        }
    }

    public void runCorrectAnswerAnimation(Animator.AnimatorListener callback) {
        if (mQuestionAnswer == null)
            return;

        Button correctBtn = mChoices[mQuestionAnswer.getCorrect()];
        correctBtn.setTextColor(getResources().getColor(R.color.btn_correct_text));
        correctBtn.setBackgroundResource(R.drawable.bg_green);

        Animator anim = AnimatorInflater.loadAnimator(getActivity(), R.animator.blink);;
        anim.setTarget(correctBtn);
        if (callback != null) anim.addListener(callback);
        anim.start();
    }

    public void setQuestionAnswer(QuestionAnswer questionAnswer) {
        mQuestionAnswer = questionAnswer;
        if (mChoices != null && mQuestionAnswer != null) {
            ArrayList<String> strChoices = mQuestionAnswer.getChoices();
            for (int i = 0; i < mChoices.length; i++) {
                mChoices[i].setText(strChoices.get(i));

                if (getActivity() == null) continue;
                Animator anim = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_in);
                anim.setTarget(mChoices[i]);
                anim.start();
            }
        }
    }

    public void setChoiceClickListener(ChoicesClickListener listener) {
        mListener = listener;
    }

    public void resetView() {
        for (Button btn : mChoices) {
            btn.setText("");
            btn.setTextColor(getResources().getColor(R.color.btn_normal_text));
            btn.setBackgroundResource(R.drawable.selector_btn_bg);
            btn.setVisibility(View.VISIBLE);
            btn.setAlpha(1f);
        }
    }

    public void setBlockTouch(boolean block) {
        mBlockTouch = block;
    }

    public int getLayoutType() {
        return mLayout;
    }

    public interface ChoicesClickListener {
        void onChoiceClick(View choice, int index);
    }
}
