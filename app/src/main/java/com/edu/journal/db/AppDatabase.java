package com.edu.journal.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.edu.journal.db.bean.Province;
import com.edu.journal.db.dao.ProvinceDao;

@Database(entities = {Province.class},version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
//    private static final String DATABASE_NAME ="Journal";
    private static final String DATABASE_NAME = "GoodWeatherNew";

    private static volatile AppDatabase mInstance;

    public abstract ProvinceDao provinceDao();

    /**
     * 单例模式
     */
    public static AppDatabase getInstance(Context context) {
        if (mInstance == null) {
            synchronized (AppDatabase.class) {
                if (mInstance == null) {
                    mInstance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME).build();
                }
            }
        }
        return mInstance;
    }
}
