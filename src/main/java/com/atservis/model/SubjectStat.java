package com.atservis.model;

/**
 * Fan statistikasi (o'rtacha ball, talabalar soni, 3-baho %)
 */
public class SubjectStat {

    private String name;
    private double avgBall;
    private double stdDev;
    private int    studentCount;
    private double failRate;   // 3-baho %
    private double excelRate;  // 5-baho %
    private double workload;   // soat
    private String status;     // Kritik | Muammo | Normal | Yaxshi | Namunali

    public SubjectStat() {}

    public SubjectStat(String name, double avgBall, double stdDev,
                       int studentCount, double failRate,
                       double excelRate, double workload) {
        this.name         = name;
        this.avgBall      = avgBall;
        this.stdDev       = stdDev;
        this.studentCount = studentCount;
        this.failRate     = failRate;
        this.excelRate    = excelRate;
        this.workload     = workload;
        this.status       = computeStatus();
    }

    private String computeStatus() {
        if (avgBall >= 83)           return "Namunali";
        if (avgBall >= 75)           return "Yaxshi";
        if (avgBall >= 70)           return "Normal";
        if (failRate >= 60)          return "Kritik";
        return "Muammo";
    }

    // ── Getters / Setters ────────────────────────────────
    public String getName()              { return name; }
    public void   setName(String v)      { this.name = v; }

    public double getAvgBall()           { return avgBall; }
    public void   setAvgBall(double v)   { this.avgBall = v; this.status = computeStatus(); }

    public double getStdDev()            { return stdDev; }
    public void   setStdDev(double v)    { this.stdDev = v; }

    public int    getStudentCount()      { return studentCount; }
    public void   setStudentCount(int v) { this.studentCount = v; }

    public double getFailRate()          { return failRate; }
    public void   setFailRate(double v)  { this.failRate = v; this.status = computeStatus(); }

    public double getExcelRate()         { return excelRate; }
    public void   setExcelRate(double v) { this.excelRate = v; }

    public double getWorkload()          { return workload; }
    public void   setWorkload(double v)  { this.workload = v; }

    public String getStatus()            { return status; }
}
