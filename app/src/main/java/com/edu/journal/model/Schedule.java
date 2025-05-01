package com.edu.journal.model;

import java.util.Date;

public class Schedule {
    private long id;
    private Date date;
    private Date endDate;
    private String type;
    private String content;
    private int importance; // 0: 普通, 1: 重要, 2: 紧急
    private String startTime;
    private String endTime;
    private boolean hasAlarm;

    public Schedule() {
    }

    public Schedule(Date date, Date endDate, String type, String content, int importance, String startTime, String endTime) {
        this.date = date;
        this.endDate = endDate;
        this.type = type;
        this.content = content;
        this.importance = importance;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hasAlarm = false;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isHasAlarm() {
        return hasAlarm;
    }

    public void setHasAlarm(boolean hasAlarm) {
        this.hasAlarm = hasAlarm;
    }
} 