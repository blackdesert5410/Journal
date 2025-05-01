package com.edu.journal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.edu.journal.R;
import com.edu.journal.db.ScheduleDatabase;
import com.edu.journal.model.Schedule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarActivity extends AppCompatActivity {
    private static final String TAG = "CalendarActivity";
    private TextView tvCurrentMonth;
    private ImageButton btnPrevMonth, btnNextMonth;
    private GridLayout calendarGrid;
    private Calendar currentDate;
    private SimpleDateFormat monthFormat;
    private ScheduleDatabase scheduleDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        initializeViews();
        setupDateFormats();
        setupClickListeners();
        updateCalendar();
    }

    private void initializeViews() {
        tvCurrentMonth = findViewById(R.id.tv_current_month);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        calendarGrid = findViewById(R.id.calendar_grid);
        currentDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        scheduleDatabase = ScheduleDatabase.getInstance(this);
    }

    private void setupDateFormats() {
        monthFormat = new SimpleDateFormat("yyyy年MM月", Locale.CHINA);
        monthFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    private void setupClickListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }

    private void updateCalendar() {
        // 更新月份显示
        tvCurrentMonth.setText(monthFormat.format(currentDate.getTime()));

        // 清除旧的日历网格
        calendarGrid.removeAllViews();

        // 设置日历网格参数
        calendarGrid.setColumnCount(7);
        calendarGrid.setRowCount(6);

        // 获取当月第一天是星期几
        Calendar firstDayOfMonth = (Calendar) currentDate.clone();
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1;

        // 获取当月天数
        int daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 添加空白格子
        for (int i = 0; i < firstDayOfWeek; i++) {
            addEmptyDayToGrid();
        }

        // 添加日期格子
        for (int day = 1; day <= daysInMonth; day++) {
            addDayToGrid(day);
        }
    }

    private void addEmptyDayToGrid() {
        TextView emptyDay = new TextView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(4, 4, 4, 4);
        emptyDay.setLayoutParams(params);
        emptyDay.setBackgroundResource(android.R.color.transparent);
        calendarGrid.addView(emptyDay);
    }

    private void addDayToGrid(int day) {
        TextView dayView = new TextView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(4, 4, 4, 4);
        dayView.setLayoutParams(params);

        // 设置日期文本
        dayView.setText(String.valueOf(day));
        dayView.setTextSize(16);
        dayView.setGravity(Gravity.CENTER);
        dayView.setPadding(8, 8, 8, 8);
        dayView.setBackgroundResource(R.drawable.bg_calendar_day);

        // 设置日期点击事件
        final int finalDay = day;
        dayView.setOnClickListener(v -> {
            Calendar selectedDate = (Calendar) currentDate.clone();
            selectedDate.set(Calendar.DAY_OF_MONTH, finalDay);
            // 设置时间为北京时间的当天开始时间（00:00:00）
            selectedDate.set(Calendar.HOUR_OF_DAY, 0);
            selectedDate.set(Calendar.MINUTE, 0);
            selectedDate.set(Calendar.SECOND, 0);
            selectedDate.set(Calendar.MILLISECOND, 0);
            openScheduleActivity(selectedDate.getTime());
        });

        // 检查是否有日程
        Calendar dateToCheck = (Calendar) currentDate.clone();
        dateToCheck.set(Calendar.DAY_OF_MONTH, day);
        // 设置时间为北京时间的当天开始时间
        dateToCheck.set(Calendar.HOUR_OF_DAY, 0);
        dateToCheck.set(Calendar.MINUTE, 0);
        dateToCheck.set(Calendar.SECOND, 0);
        dateToCheck.set(Calendar.MILLISECOND, 0);
        int scheduleCount = scheduleDatabase.getScheduleCountByDate(dateToCheck.getTime());
        
        if (scheduleCount > 0) {
            // 创建包含日期和日程数量的文本
            String displayText = day + "\n" + scheduleCount + "项";
            dayView.setText(displayText);
            dayView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        }

        // 如果是今天，添加特殊标记
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        if (isSameDay(dateToCheck, today)) {
            dayView.setBackgroundResource(R.drawable.bg_calendar_today);
            dayView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }

        calendarGrid.addView(dayView);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void openScheduleActivity(Date date) {
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.putExtra("date", date.getTime());
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // 刷新日历显示
            updateCalendar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}
