package kr.ac.kopo.dodaynote_2.domain;

import java.util.List;

public class YearlyStatDto {

    private int year;
    private int totalRecords;
    private int successCount;
    private double achievementRate;
    private String bestHabitTitle;
    private List<MonthlyStatDto> monthly;

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public double getAchievementRate() { return achievementRate; }
    public void setAchievementRate(double achievementRate) { this.achievementRate = achievementRate; }

    public String getBestHabitTitle() { return bestHabitTitle; }
    public void setBestHabitTitle(String bestHabitTitle) { this.bestHabitTitle = bestHabitTitle; }

    public List<MonthlyStatDto> getMonthly() { return monthly; }
    public void setMonthly(List<MonthlyStatDto> monthly) { this.monthly = monthly; }
}
