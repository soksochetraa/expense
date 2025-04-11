package com.example.expense.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.expense.model.Category;

@Database(entities = {Category.class}, version = 1)
public abstract class CategoryDatabase extends RoomDatabase {
    private static CategoryDatabase instance;

    public abstract CategoryDao categoryDao();

    public static synchronized CategoryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            CategoryDatabase.class, "category_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}

