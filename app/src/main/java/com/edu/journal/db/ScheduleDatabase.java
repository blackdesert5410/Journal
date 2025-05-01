package com.edu.journal.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.edu.journal.model.Schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ScheduleDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "schedule.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_SCHEDULE = "schedule";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_END_DATE = "end_date";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_IMPORTANCE = "importance";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_HAS_ALARM = "has_alarm";

    private static ScheduleDatabase instance;

    public static synchronized ScheduleDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new ScheduleDatabase(context.getApplicationContext());
        }
        return instance;
    }

    private ScheduleDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SCHEDULE + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DATE + " INTEGER,"
                + COLUMN_END_DATE + " INTEGER,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_IMPORTANCE + " INTEGER,"
                + COLUMN_START_TIME + " TEXT,"
                + COLUMN_END_TIME + " TEXT,"
                + COLUMN_HAS_ALARM + " INTEGER"
                + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // 删除旧表并创建新表
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
            onCreate(db);
        }
    }

    public long insertSchedule(Schedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, schedule.getDate().getTime());
        values.put(COLUMN_END_DATE, schedule.getEndDate().getTime());
        values.put(COLUMN_TYPE, schedule.getType());
        values.put(COLUMN_CONTENT, schedule.getContent());
        values.put(COLUMN_IMPORTANCE, schedule.getImportance());
        values.put(COLUMN_START_TIME, schedule.getStartTime());
        values.put(COLUMN_END_TIME, schedule.getEndTime());
        values.put(COLUMN_HAS_ALARM, schedule.isHasAlarm() ? 1 : 0);

        long id = db.insert(TABLE_SCHEDULE, null, values);
        db.close();
        return id;
    }

    public List<Schedule> getSchedulesByDate(Date date) {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 获取指定日期的开始和结束时间戳（使用北京时间）
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis() - 1;

        String selectQuery = "SELECT * FROM " + TABLE_SCHEDULE
                + " WHERE (" + COLUMN_DATE + " <= ? AND " + COLUMN_END_DATE + " >= ?)"
                + " ORDER BY " + COLUMN_START_TIME + " ASC";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(endOfDay), String.valueOf(startOfDay)});

        if (cursor.moveToFirst()) {
            do {
                Schedule schedule = new Schedule();
                schedule.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                schedule.setDate(new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE))));
                schedule.setEndDate(new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_END_DATE))));
                schedule.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
                schedule.setContent(cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)));
                schedule.setImportance(cursor.getInt(cursor.getColumnIndex(COLUMN_IMPORTANCE)));
                schedule.setStartTime(cursor.getString(cursor.getColumnIndex(COLUMN_START_TIME)));
                schedule.setEndTime(cursor.getString(cursor.getColumnIndex(COLUMN_END_TIME)));
                schedule.setHasAlarm(cursor.getInt(cursor.getColumnIndex(COLUMN_HAS_ALARM)) == 1);
                schedules.add(schedule);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return schedules;
    }

    public int getScheduleCountByDate(Date date) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis() - 1;

        String countQuery = "SELECT COUNT(*) FROM " + TABLE_SCHEDULE
                + " WHERE (" + COLUMN_DATE + " <= ? AND " + COLUMN_END_DATE + " >= ?)";

        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(endOfDay), String.valueOf(startOfDay)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public boolean deleteSchedule(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_SCHEDULE, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public boolean updateSchedule(Schedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, schedule.getDate().getTime());
        values.put(COLUMN_END_DATE, schedule.getEndDate().getTime());
        values.put(COLUMN_TYPE, schedule.getType());
        values.put(COLUMN_CONTENT, schedule.getContent());
        values.put(COLUMN_IMPORTANCE, schedule.getImportance());
        values.put(COLUMN_START_TIME, schedule.getStartTime());
        values.put(COLUMN_END_TIME, schedule.getEndTime());
        values.put(COLUMN_HAS_ALARM, schedule.isHasAlarm() ? 1 : 0);

        int result = db.update(TABLE_SCHEDULE, values, COLUMN_ID + " = ?", 
            new String[]{String.valueOf(schedule.getId())});
        db.close();
        return result > 0;
    }
} 