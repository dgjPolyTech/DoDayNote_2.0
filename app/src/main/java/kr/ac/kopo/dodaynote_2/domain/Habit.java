package kr.ac.kopo.dodaynote_2.domain;

import java.util.List;

public class Habit {
    private Long id;
    private String title;
    private String startDate;
    private String endDate;
    private boolean isAlertOn;
    private int targetMinutes;
    private boolean isCompleted;
    private String activeDays;
    private List<HabitRecord> records;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public boolean isAlertOn() { return isAlertOn; }
    public void setAlertOn(boolean alertOn) { isAlertOn = alertOn; }
    public int getTargetMinutes() { return targetMinutes; }
    public void setTargetMinutes(int targetMinutes) { this.targetMinutes = targetMinutes; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public String getActiveDays() { return activeDays; }
    public void setActiveDays(String activeDays) { this.activeDays = activeDays; }
    public List<HabitRecord> getRecords() { return records; }
    public void setRecords(List<HabitRecord> records) { this.records = records; }
}