package kr.ac.kopo.dodaynote_2.domain;

public class MonthlyStatDto {

    private int month;
    private int totalRecords;
    private int successCount;
    private double achievementRate;

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public double getAchievementRate() { return achievementRate; }
    public void setAchievementRate(double achievementRate) { this.achievementRate = achievementRate; }
}
