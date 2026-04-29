package com.atservis.model;

import java.util.*;

/**
 * Talaba ma'lumotlari va ML natijalarini saqlaydi
 */
public class Student {

    private String name;
    private double avgBall;
    private double techAvg;
    private double mathAvg;
    private int totalSubjects;
    private int grade5Count;
    private int grade3Count;
    private int clusterId;
    private String clusterName;
    private String daraja;          // A'lo / Yaxshi / Qoniqarli
    private String rang;            // 🟢 / 🟡 / 🟠 / 🔴

    private List<SubjectScore> strongSubjects = new ArrayList<>();
    private List<SubjectScore> weakSubjects   = new ArrayList<>();
    private List<String> collaborativeRecs    = new ArrayList<>();
    private List<String> contentRecs          = new ArrayList<>();
    private int predictedGrade;

    // ── Getters / Setters ────────────────────────────────

    public String getName()                    { return name; }
    public void   setName(String v)            { this.name = v; }

    public double getAvgBall()                 { return avgBall; }
    public void   setAvgBall(double v)         { this.avgBall = v; }

    public double getTechAvg()                 { return techAvg; }
    public void   setTechAvg(double v)         { this.techAvg = v; }

    public double getMathAvg()                 { return mathAvg; }
    public void   setMathAvg(double v)         { this.mathAvg = v; }

    public int    getTotalSubjects()           { return totalSubjects; }
    public void   setTotalSubjects(int v)      { this.totalSubjects = v; }

    public int    getGrade5Count()             { return grade5Count; }
    public void   setGrade5Count(int v)        { this.grade5Count = v; }

    public int    getGrade3Count()             { return grade3Count; }
    public void   setGrade3Count(int v)        { this.grade3Count = v; }

    public int    getClusterId()               { return clusterId; }
    public void   setClusterId(int v)          { this.clusterId = v; }

    public String getClusterName()             { return clusterName; }
    public void   setClusterName(String v)     { this.clusterName = v; }

    public String getDaraja()                  { return daraja; }
    public void   setDaraja(String v)          { this.daraja = v; }

    public String getRang()                    { return rang; }
    public void   setRang(String v)            { this.rang = v; }

    public int    getPredictedGrade()          { return predictedGrade; }
    public void   setPredictedGrade(int v)     { this.predictedGrade = v; }

    public List<SubjectScore> getStrongSubjects()        { return strongSubjects; }
    public List<SubjectScore> getWeakSubjects()          { return weakSubjects; }
    public List<String>       getCollaborativeRecs()     { return collaborativeRecs; }
    public List<String>       getContentRecs()           { return contentRecs; }

    // ── SubjectScore inner class ─────────────────────────

    public static class SubjectScore {
        private final String name;
        private final double ball;

        public SubjectScore(String name, double ball) {
            this.name = name;
            this.ball = ball;
        }

        public String getName() { return name; }
        public double getBall() { return ball; }

        @Override
        public String toString() {
            return String.format("%-45s  %.0f", name, ball);
        }
    }
}
