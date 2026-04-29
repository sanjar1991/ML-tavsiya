package com.atservis.model;

import java.util.*;

/**
 * ML pipeline natijalari — barcha hisob-kitoblar natijasi
 */
public class AnalysisResult {

    // ── Meta ─────────────────────────────────────────────
    private int    totalStudents;
    private int    totalSubjects;
    private int    totalRecords;
    private double overallAvg;
    private double modelAccuracy;

    // ── Talabalar ─────────────────────────────────────────
    private List<Student>    students    = new ArrayList<>();

    // ── Fan statistikasi ──────────────────────────────────
    private List<SubjectStat> subjectStats = new ArrayList<>();

    // ── Klaster taqsimoti ─────────────────────────────────
    private Map<String, Integer> clusterDistribution = new LinkedHashMap<>();

    // ── Baho taqsimoti ───────────────────────────────────
    private Map<Integer, Integer> gradeDistribution = new TreeMap<>();

    // ── Kategoriya o'rtachalari ──────────────────────────
    private Map<String, Double> categoryAverages = new LinkedHashMap<>();

    // ── Talaba darajalari ─────────────────────────────────
    private int aloCount;
    private int yaxshiCount;
    private int qoniqarliCount;

    // ── Getters / Setters ─────────────────────────────────
    public int    getTotalStudents()                     { return totalStudents; }
    public void   setTotalStudents(int v)                { this.totalStudents = v; }

    public int    getTotalSubjects()                     { return totalSubjects; }
    public void   setTotalSubjects(int v)                { this.totalSubjects = v; }

    public int    getTotalRecords()                      { return totalRecords; }
    public void   setTotalRecords(int v)                 { this.totalRecords = v; }

    public double getOverallAvg()                        { return overallAvg; }
    public void   setOverallAvg(double v)                { this.overallAvg = v; }

    public double getModelAccuracy()                     { return modelAccuracy; }
    public void   setModelAccuracy(double v)             { this.modelAccuracy = v; }

    public List<Student>    getStudents()                { return students; }
    public void             setStudents(List<Student> v) { this.students = v; }

    public List<SubjectStat> getSubjectStats()           { return subjectStats; }
    public void setSubjectStats(List<SubjectStat> v)     { this.subjectStats = v; }

    public Map<String, Integer> getClusterDistribution() { return clusterDistribution; }
    public Map<Integer, Integer> getGradeDistribution()  { return gradeDistribution; }
    public Map<String, Double>  getCategoryAverages()    { return categoryAverages; }

    public int getAloCount()                             { return aloCount; }
    public void setAloCount(int v)                       { this.aloCount = v; }

    public int getYaxshiCount()                          { return yaxshiCount; }
    public void setYaxshiCount(int v)                    { this.yaxshiCount = v; }

    public int getQoniqarliCount()                       { return qoniqarliCount; }
    public void setQoniqarliCount(int v)                 { this.qoniqarliCount = v; }
}
