package com.paperplanes.knowquiz.model;

/**
 * Created by abdularis on 23/04/17.
 */

public class Category {

    private int mId;
    private String mName;

    public Category() {
        this(0, "");
    }

    public Category(int id, String name) {
        mId = id;
        mName = name;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
