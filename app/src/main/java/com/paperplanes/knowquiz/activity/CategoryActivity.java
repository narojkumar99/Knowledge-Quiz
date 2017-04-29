package com.paperplanes.knowquiz.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.paperplanes.knowquiz.R;
import com.paperplanes.knowquiz.core.SoundManager;
import com.paperplanes.knowquiz.data.QuizDatabase;
import com.paperplanes.knowquiz.model.Category;

import java.util.ArrayList;

public class CategoryActivity extends ActivityFullscreen {

    public static final String EXTRA_CATEGORY_ID = "com.paperplanes.knowquiz.activity.CategoryId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_category);

        QuizDatabase db = QuizDatabase.getInstance(this);

        ArrayList<Category> categories = db.getCategories();

        for (int i = 0; i < categories.size(); i++) {
            LinearLayout.LayoutParams lParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lParams.setMargins(0, 0, 0, (int)getResources().getDimension(R.dimen.category_btn_spacing));
            Category category = categories.get(i);
            Button btn = new Button(this);
            btn.setLayoutParams(lParams);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                btn.setAllCaps(true);
            }

            btn.setText(category.getName());
            int padding = (int)getResources().getDimension(R.dimen.btn_main_menu_padding);
            btn.setPadding(padding, padding, padding, padding);
            btn.setBackgroundResource(R.drawable.selector_btn_bg);
            btn.setMinWidth(getResources().getDimensionPixelSize(R.dimen.btn_category_min_width));
            btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.btn_main_menu_text));
            btn.setTextColor(getResources().getColor(android.R.color.white));
            btn.setTag(category.getId());

            layout.addView(btn);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToQuizActivity((int)v.getTag());
                }
            });
        }

        Log.v("Category", String.valueOf(getResources().getDimension(R.dimen.btn_main_menu_text)));
    }

    public void onRandomCatClick(View view) {
        goToQuizActivity(-1);
    }

    private void goToQuizActivity(int categoryId) {
        SoundManager.getInstance(this).playSound(SoundManager.SOUND_CLICK);

        Intent intent = new Intent(this, QuizActivity.class);
        if (categoryId >= 0)
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        startActivity(intent);
    }

    private float dpToPixel(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
