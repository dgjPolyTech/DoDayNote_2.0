package kr.ac.kopo.dodaynote_2.domain;

import com.google.gson.annotations.SerializedName;

public class HabitRecord {
    private Long id;
    private String recordDate;
    @SerializedName(value = "isDone", alternate = {"done", "is_done"})
    private boolean isDone;
    private int progressMinutes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecordDate() { return recordDate; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }
    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }
    public int getProgressMinutes() { return progressMinutes; }
    public void setProgressMinutes(int progressMinutes) { this.progressMinutes = progressMinutes; }
}