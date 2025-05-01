package com.edu.journal.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.edu.journal.R;
import com.edu.journal.db.ScheduleDatabase;
import com.edu.journal.model.Schedule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.ParseException;
import android.widget.ArrayAdapter;

public class ScheduleActivity extends AppCompatActivity {
    private static final String TAG = "ScheduleActivity";
    private TextView tvDate;
    private LinearLayout scheduleList;
    private ScheduleDatabase scheduleDatabase;
    private Date currentDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        initializeViews();
        setupDateFormat();
        loadCurrentDate();
        loadSchedules();
        setupAddButton();
    }

    private void initializeViews() {
        tvDate = findViewById(R.id.tv_date);
        scheduleList = findViewById(R.id.schedule_list);
        scheduleDatabase = ScheduleDatabase.getInstance(this);
    }

    private void setupDateFormat() {
        dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
    }

    private void loadCurrentDate() {
        long dateTime = getIntent().getLongExtra("date", System.currentTimeMillis());
        currentDate = new Date(dateTime);
        tvDate.setText(dateFormat.format(currentDate));
    }

    private void loadSchedules() {
        scheduleList.removeAllViews();
        List<Schedule> schedules = scheduleDatabase.getSchedulesByDate(currentDate);
        
        for (Schedule schedule : schedules) {
            addScheduleView(schedule);
        }
    }

    private String formatTimeRange(Schedule schedule) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日", Locale.CHINA);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        
        try {
            String startDateStr = dateFormat.format(schedule.getDate());
            String endDateStr = dateFormat.format(schedule.getEndDate());
            
            if (startDateStr.equals(endDateStr)) {
                // 同一天只显示时间范围
                return schedule.getStartTime() + " - " + schedule.getEndTime();
            } else {
                // 跨天显示完整日期时间范围
                return startDateStr + " " + schedule.getStartTime() + " - " + 
                       endDateStr + " " + schedule.getEndTime();
            }
        } catch (Exception e) {
            return schedule.getStartTime() + " - " + schedule.getEndTime();
        }
    }

    private String calculateRemainingTime(Schedule schedule) {
        try {
            Calendar now = Calendar.getInstance();
            Calendar startDateTime = Calendar.getInstance();
            Calendar endDateTime = Calendar.getInstance();
            
            // 设置开始日期和时间
            startDateTime.setTime(schedule.getDate());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
            Date startTime = timeFormat.parse(schedule.getStartTime());
            Calendar startTimeCal = Calendar.getInstance();
            startTimeCal.setTime(startTime);
            startDateTime.set(Calendar.HOUR_OF_DAY, startTimeCal.get(Calendar.HOUR_OF_DAY));
            startDateTime.set(Calendar.MINUTE, startTimeCal.get(Calendar.MINUTE));
            
            // 设置结束日期和时间
            endDateTime.setTime(schedule.getEndDate());
            Date endTime = timeFormat.parse(schedule.getEndTime());
            Calendar endTimeCal = Calendar.getInstance();
            endTimeCal.setTime(endTime);
            endDateTime.set(Calendar.HOUR_OF_DAY, endTimeCal.get(Calendar.HOUR_OF_DAY));
            endDateTime.set(Calendar.MINUTE, endTimeCal.get(Calendar.MINUTE));
            
            // 如果还未开始
            if (now.before(startDateTime)) {
                long diffMillis = startDateTime.getTimeInMillis() - now.getTimeInMillis();
                long diffHours = diffMillis / (1000 * 60 * 60);
                
                if (diffHours < 1) {
                    long diffMinutes = diffMillis / (1000 * 60);
                    return diffMinutes + "分钟后开始";
                } else if (diffHours < 24) {
                    return diffHours + "小时后开始";
                } else {
                    long diffDays = diffHours / 24;
                    return diffDays + "天后开始";
                }
            }
            // 如果已经结束
            else if (now.after(endDateTime)) {
                return "已结束";
            }
            // 如果正在进行中
            else {
                long diffMillis = endDateTime.getTimeInMillis() - now.getTimeInMillis();
                long diffDays = diffMillis / (1000 * 60 * 60 * 24);
                long diffHours = (diffMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
                long diffMinutes = (diffMillis % (1000 * 60 * 60)) / (1000 * 60);
                
                StringBuilder remainingTime = new StringBuilder();
                if (diffDays > 0) {
                    remainingTime.append(diffDays).append("天");
                }
                if (diffHours > 0 || diffDays > 0) {
                    remainingTime.append(diffHours).append("小时");
                }
                remainingTime.append(diffMinutes).append("分钟");
                
                return "剩余" + remainingTime.toString();
            }
        } catch (Exception e) {
            return "";
        }
    }

    private void addScheduleView(Schedule schedule) {
        View scheduleView = LayoutInflater.from(this).inflate(R.layout.item_schedule, scheduleList, false);
        
        TextView tvType = scheduleView.findViewById(R.id.tv_schedule_type);
        TextView tvImportance = scheduleView.findViewById(R.id.tv_importance);
        TextView tvContent = scheduleView.findViewById(R.id.tv_schedule_content);
        TextView tvTime = scheduleView.findViewById(R.id.tv_schedule_time);
        TextView tvRemainingTime = scheduleView.findViewById(R.id.tv_remaining_time);

        tvType.setText(schedule.getType());
        tvContent.setText(schedule.getContent());
        tvTime.setText(formatTimeRange(schedule));
        tvRemainingTime.setText(calculateRemainingTime(schedule));

        // 设置重要程度颜色
        switch (schedule.getImportance()) {
            case 0:
                tvImportance.setText("普通");
                tvImportance.setTextColor(getResources().getColor(android.R.color.darker_gray));
                break;
            case 1:
                tvImportance.setText("重要");
                tvImportance.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case 2:
                tvImportance.setText("紧急");
                tvImportance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
        }

        // 添加点击修改功能
        scheduleView.setOnClickListener(v -> {
            // 检查日程是否已结束
            Calendar now = Calendar.getInstance();
            Calendar endDateTime = Calendar.getInstance();
            try {
                // 设置结束日期和时间
                endDateTime.setTime(schedule.getEndDate());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
                Date endTime = timeFormat.parse(schedule.getEndTime());
                Calendar endTimeCal = Calendar.getInstance();
                endTimeCal.setTime(endTime);
                endDateTime.set(Calendar.HOUR_OF_DAY, endTimeCal.get(Calendar.HOUR_OF_DAY));
                endDateTime.set(Calendar.MINUTE, endTimeCal.get(Calendar.MINUTE));
                
                if (endDateTime.before(now)) {
                    Toast.makeText(this, "不能修改过去的痕迹哦", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                showEditScheduleDialog(schedule, scheduleView);
            } catch (ParseException e) {
                Toast.makeText(this, "日期格式错误", Toast.LENGTH_SHORT).show();
            }
        });

        // 添加长按删除功能
        scheduleView.setOnLongClickListener(v -> {
            showDeleteConfirmDialog(schedule, scheduleView);
            return true;
        });

        scheduleList.addView(scheduleView);
    }

    private void showDeleteConfirmDialog(Schedule schedule, View scheduleView) {
        new AlertDialog.Builder(this)
            .setTitle("删除日程")
            .setMessage("确定要删除这条日程吗？")
            .setPositiveButton("删除", (dialog, which) -> {
                if (scheduleDatabase.deleteSchedule(schedule.getId())) {
                    scheduleList.removeView(scheduleView);
                    Toast.makeText(this, "日程已删除", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                } else {
                    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void setupAddButton() {
        findViewById(R.id.fab_add_schedule).setOnClickListener(v -> {
            // 检查当前日期是否是今天或以后
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar currentDateCal = Calendar.getInstance();
            currentDateCal.setTime(currentDate);
            currentDateCal.set(Calendar.HOUR_OF_DAY, 0);
            currentDateCal.set(Calendar.MINUTE, 0);
            currentDateCal.set(Calendar.SECOND, 0);
            currentDateCal.set(Calendar.MILLISECOND, 0);

            if (currentDateCal.before(today)) {
                Toast.makeText(this, "昨天已成为历史", Toast.LENGTH_SHORT).show();
                return;
            }

            showAddScheduleDialog();
        });
    }

    private boolean checkTimeValid(String startTime, String endTime, Date startDate, Date endDate) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
            Date startTimeDate = timeFormat.parse(startTime);
            Date endTimeDate = timeFormat.parse(endTime);
            
            // 如果是同一天，检查开始时间是否早于结束时间
            if (startDate.equals(endDate)) {
                return startTimeDate.before(endTimeDate);
            }
            
            // 如果是不同天，开始时间必须早于结束时间
            return startDate.before(endDate);
        } catch (ParseException e) {
            return false;
        }
    }

    private void showAddScheduleDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_type);
        Spinner spinnerImportance = dialogView.findViewById(R.id.spinner_importance);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        TextView tvStartDate = dialogView.findViewById(R.id.tv_start_date);
        TextView tvEndDate = dialogView.findViewById(R.id.tv_end_date);
        TextView tvStartTime = dialogView.findViewById(R.id.tv_start_time);
        TextView tvEndTime = dialogView.findViewById(R.id.tv_end_time);

        // 设置默认日期为当前日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        tvStartDate.setText(dateFormat.format(currentDate));
        tvEndDate.setText(dateFormat.format(currentDate));

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("添加日程")
            .setView(dialogView)
            .setPositiveButton("确定", null)
            .setNegativeButton("取消", null)
            .create();

        // 设置开始日期选择器
        tvStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    tvStartDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // 设置结束日期选择器
        tvEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    tvEndDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // 设置开始时间选择器
        tvStartTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    tvStartTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });

        // 设置结束时间选择器
        tvEndTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    tvEndTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String type = spinnerType.getSelectedItem().toString();
                String content = etContent.getText().toString().trim();
                String startTime = tvStartTime.getText().toString();
                String endTime = tvEndTime.getText().toString();

                // 检查时间是否已选择
                if (startTime.equals("选择时间")) {
                    Toast.makeText(this, "请选择开始时间", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (endTime.equals("选择时间")) {
                    Toast.makeText(this, "请选择结束时间", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 检查内容是否为空
                if (content.isEmpty()) {
                    Toast.makeText(this, "请输入日程内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // 解析开始日期
                    Date startDate = dateFormat.parse(tvStartDate.getText().toString());
                    // 解析结束日期
                    Date endDate = dateFormat.parse(tvEndDate.getText().toString());

                    // 检查日期是否有效
                    if (startDate.after(endDate)) {
                        Toast.makeText(this, "开始日期不能晚于结束日期", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 检查时间是否有效
                    if (!checkTimeValid(startTime, endTime, startDate, endDate)) {
                        Toast.makeText(this, "开始时间必须早于结束时间", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Schedule schedule = new Schedule(startDate, endDate, type, content, 
                        spinnerImportance.getSelectedItemPosition(), startTime, endTime);
                    long id = scheduleDatabase.insertSchedule(schedule);
                    if (id != -1) {
                        schedule.setId(id);
                        addScheduleView(schedule);
                        Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    Toast.makeText(this, "日期格式错误", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showEditScheduleDialog(Schedule schedule, View scheduleView) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_type);
        Spinner spinnerImportance = dialogView.findViewById(R.id.spinner_importance);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        TextView tvStartDate = dialogView.findViewById(R.id.tv_start_date);
        TextView tvEndDate = dialogView.findViewById(R.id.tv_end_date);
        TextView tvStartTime = dialogView.findViewById(R.id.tv_start_time);
        TextView tvEndTime = dialogView.findViewById(R.id.tv_end_time);

        // 设置当前值
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        tvStartDate.setText(dateFormat.format(schedule.getDate()));
        tvEndDate.setText(dateFormat.format(schedule.getEndDate()));
        tvStartTime.setText(schedule.getStartTime());
        tvEndTime.setText(schedule.getEndTime());
        etContent.setText(schedule.getContent());

        // 设置类型和重要程度
        ArrayAdapter<CharSequence> typeAdapter = (ArrayAdapter<CharSequence>) spinnerType.getAdapter();
        ArrayAdapter<CharSequence> importanceAdapter = (ArrayAdapter<CharSequence>) spinnerImportance.getAdapter();
        for (int i = 0; i < typeAdapter.getCount(); i++) {
            if (typeAdapter.getItem(i).toString().equals(schedule.getType())) {
                spinnerType.setSelection(i);
                break;
            }
        }
        spinnerImportance.setSelection(schedule.getImportance());

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("修改日程")
            .setView(dialogView)
            .setPositiveButton("确定", null)
            .setNegativeButton("取消", null)
            .create();

        // 设置日期选择器
        tvStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(schedule.getDate());
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    tvStartDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        tvEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(schedule.getEndDate());
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    tvEndDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // 设置时间选择器
        tvStartTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
                Date time = timeFormat.parse(schedule.getStartTime());
                calendar.setTime(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    tvStartTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });

        tvEndTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
                Date time = timeFormat.parse(schedule.getEndTime());
                calendar.setTime(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    tvEndTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String type = spinnerType.getSelectedItem().toString();
                String content = etContent.getText().toString().trim();
                String startTime = tvStartTime.getText().toString();
                String endTime = tvEndTime.getText().toString();

                // 检查时间是否已选择
                if (startTime.equals("选择时间")) {
                    Toast.makeText(this, "请选择开始时间", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (endTime.equals("选择时间")) {
                    Toast.makeText(this, "请选择结束时间", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 检查内容是否为空
                if (content.isEmpty()) {
                    Toast.makeText(this, "请输入日程内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // 解析开始日期
                    Date startDate = dateFormat.parse(tvStartDate.getText().toString());
                    // 解析结束日期
                    Date endDate = dateFormat.parse(tvEndDate.getText().toString());

                    // 检查日期是否有效
                    if (startDate.after(endDate)) {
                        Toast.makeText(this, "开始日期不能晚于结束日期", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 检查时间是否有效
                    if (!checkTimeValid(startTime, endTime, startDate, endDate)) {
                        Toast.makeText(this, "开始时间必须早于结束时间", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 检查是否修改过去的日程
                    Calendar now = Calendar.getInstance();
                    Calendar endDateTime = Calendar.getInstance();
                    endDateTime.setTime(endDate);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
                    Date endTimeDate = timeFormat.parse(endTime);
                    Calendar endTimeCal = Calendar.getInstance();
                    endTimeCal.setTime(endTimeDate);
                    endDateTime.set(Calendar.HOUR_OF_DAY, endTimeCal.get(Calendar.HOUR_OF_DAY));
                    endDateTime.set(Calendar.MINUTE, endTimeCal.get(Calendar.MINUTE));

                    if (endDateTime.before(now)) {
                        Toast.makeText(this, "不能修改过去的痕迹哦", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 更新日程
                    schedule.setDate(startDate);
                    schedule.setEndDate(endDate);
                    schedule.setType(type);
                    schedule.setContent(content);
                    schedule.setImportance(spinnerImportance.getSelectedItemPosition());
                    schedule.setStartTime(startTime);
                    schedule.setEndTime(endTime);

                    if (scheduleDatabase.updateSchedule(schedule)) {
                        // 更新视图
                        scheduleList.removeView(scheduleView);
                        addScheduleView(schedule);
                        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "修改失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    Toast.makeText(this, "日期格式错误", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
