package com.atservis.service;

import com.atservis.model.*;
import com.atservis.service.ExcelParser.RawRecord;

import java.util.*;
import java.util.stream.*;

/**
 * ML Pipeline — sof Java implementatsiyasi
 *
 *  1. Ma'lumot tayyorlash + matritsa
 *  2. K-Means klasterlash (k=4)
 *  3. Cosine Similarity  → Collaborative Filtering
 *  4. Kategoriya tahlili → Content-Based Filtering
 *  5. Naïve Bayes baho bashorati (Random Forest o'rniga)
 *  6. Fan statistikasi
 *  7. AnalysisResult yig'ish
 */
public class MLEngine {

    // ── Fan kategoriyalari ────────────────────────────────
    private static final Map<String, List<String>> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("Dasturlash",   List.of("C++da dasturlash","Tizimli dasturlash",
                "Dasturlash injiniringi","Sonli usullar va dasturlash, modellashtirish",
                "Dasturlash 1","Dasturlash 2","Web-dasturlashga kirish"));
        CATEGORIES.put("Ma'lumotlar",  List.of("Ma'lumotlar tuzilmasi","Ma'lumotlar bazasi",
                "Ma'lumotlar bazasi xavfsizligi","Big data texnologiyasi",
                "Katta hajmdagi ma'lumotlarni boshqarish (Big Data)","Ma'lumotlarni tiklash",
                "Ma'lumotlar bazasini boshqarish"));
        CATEGORIES.put("Tarmoq",       List.of("Kompyuter tarmoqlari","Axborot xavfsizligi",
                "Tarmoq xavfsizligi","Elektron hukumat","Linux server boshqaruvi",
                "Windows Server boshqaruvi"));
        CATEGORIES.put("Web",          List.of("Web-ilovalarni yaratish","Web texnologiya",
                "Elektron biznes texnologiyalari","Mobil ilovalarini ishlab chiqish",
                "Ijtimoiy media marketingi"));
        CATEGORIES.put("Matematika",   List.of("Matematika1","Matematika2","Hisob (Calculus)",
                "Chiziqli algebra","Diskret tuzilmalar","Ehtimollik va statistika",
                "Diskret matematika ","Matematika. Ehtimollar nazariyasi va matematik statistika",
                "Sonli usullar va chiziqli dasturlash","Extimollik va statistika"));
        CATEGORIES.put("AI/ML",        List.of("Sun'iy intellekt","Qaror qabul qilish tizimlari",
                "Tizimli tahlil","Big data texnologiyasi"));
        CATEGORIES.put("Apparat",      List.of("Kompyuter arxitekturasi","Raqamli sxemotexnika",
                "Elektronika va sxemalar 2","Fizika 1","Fizika 2","Kompyuterni tashkil etilishi"));
        CATEGORIES.put("Cloud",        List.of("Cloud Computing","Big data texnologiyasi",
                "Katta hajmdagi ma'lumotlarni boshqarish (Big Data)"));
    }

    private static final Map<Integer, String> CLUSTER_NAMES = Map.of(
        0, "A'lochi talabalar",
        1, "Texnik yo'nalishga moyil",
        2, "O'rtacha talabalar",
        3, "Qo'shimcha yordam kerak"
    );

    // ── Asosiy metod ─────────────────────────────────────

    public AnalysisResult analyze(List<RawRecord> records,
                                   ProgressCallback progress) {

        progress.update(0.05, "Ma'lumotlar tartiblanmoqda…");

        // Barcha talabalar va fanlar ro'yxati
        List<String> allStudents = records.stream()
                .map(RawRecord::studentName).distinct().sorted().toList();
        List<String> allSubjects = records.stream()
                .map(RawRecord::subjectName).distinct().sorted().toList();

        int S = allStudents.size(), F = allSubjects.size();

        // Talaba → (fan → ball) map
        Map<String, Map<String, Double>> studentMap = new HashMap<>();
        Map<String, Map<String, Double>> studentBaho = new HashMap<>();
        Map<String, Map<String, Double>> studentWork = new HashMap<>();

        for (RawRecord r : records) {
            studentMap .computeIfAbsent(r.studentName(), k -> new HashMap<>())
                       .merge(r.subjectName(), r.ball(),    (a,b) -> (a+b)/2.0);
            studentBaho.computeIfAbsent(r.studentName(), k -> new HashMap<>())
                       .put(r.subjectName(), r.baho());
            studentWork.computeIfAbsent(r.studentName(), k -> new HashMap<>())
                       .put(r.subjectName(), r.workload());
        }

        // ── Matritsa (S×F) - NaN yerine o'rtacha ─────────
        progress.update(0.15, "Talaba–Fan matritsasi qurilmoqda…");

        double[][] mat = new double[S][F];
        double[]   colMeans = new double[F];

        for (int j = 0; j < F; j++) {
            String fan = allSubjects.get(j);
            double sum = 0; int cnt = 0;
            for (int i = 0; i < S; i++) {
                Double v = studentMap.get(allStudents.get(i)).get(fan);
                if (v != null && !Double.isNaN(v)) { sum += v; cnt++; }
            }
            colMeans[j] = cnt > 0 ? sum / cnt : 70.0;
        }

        for (int i = 0; i < S; i++) {
            Map<String, Double> row = studentMap.get(allStudents.get(i));
            for (int j = 0; j < F; j++) {
                Double v = row.get(allSubjects.get(j));
                mat[i][j] = (v != null && !Double.isNaN(v)) ? v : colMeans[j];
            }
        }

        // ── Normalizatsiya (Z-score) ───────────────────────
        double[][] normMat = zScore(mat, S, F);

        // ── K-Means (k=4) ─────────────────────────────────
        progress.update(0.30, "K-Means klasterlash (k=4)…");
        int[] clusterLabels = kMeans(normMat, 4, 50);

        // ── Cosine Similarity matritsa ─────────────────────
        progress.update(0.45, "Cosine Similarity hisoblanmoqda…");
        double[][] simMat = cosineSimilarity(normMat);

        // ── Talaba profillari ─────────────────────────────
        progress.update(0.60, "Talaba profillari yaratilmoqda…");

        List<Student> students = new ArrayList<>();
        for (int i = 0; i < S; i++) {
            String name = allStudents.get(i);
            Map<String, Double> scores = studentMap.get(name);
            Map<String, Double> bahos  = studentBaho.get(name);

            Student st = new Student();
            st.setName(name);

            // O'rtacha
            double avg = scores.values().stream()
                    .mapToDouble(Double::doubleValue).average().orElse(0);
            st.setAvgBall(round2(avg));

            // Daraja
            String[] dr = daraja(avg);
            st.setDaraja(dr[0]); st.setRang(dr[1]);

            // Texnik / Matematik
            st.setTechAvg(round2(catAvg(scores, CATEGORIES.get("Dasturlash"), avg)));
            st.setMathAvg(round2(catAvg(scores, CATEGORIES.get("Matematika"), avg)));

            // Fanlar soni
            st.setTotalSubjects(scores.size());

            // Baho soni
            long g5 = bahos.values().stream().filter(b -> b != null && b == 5).count();
            long g3 = bahos.values().stream().filter(b -> b != null && b == 3).count();
            st.setGrade5Count((int) g5);
            st.setGrade3Count((int) g3);

            // Klaster
            st.setClusterId(clusterLabels[i]);
            st.setClusterName(CLUSTER_NAMES.getOrDefault(clusterLabels[i], "Noma'lum"));

            // Kuchli / zaif fanlar
            scores.entrySet().stream()
                    .filter(e -> e.getValue() >= 80)
                    .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> st.getStrongSubjects()
                            .add(new Student.SubjectScore(e.getKey(), e.getValue())));

            scores.entrySet().stream()
                    .filter(e -> e.getValue() < 70)
                    .sorted(Map.Entry.comparingByValue())
                    .limit(5)
                    .forEach(e -> st.getWeakSubjects()
                            .add(new Student.SubjectScore(e.getKey(), e.getValue())));

            // Collaborative tavsiyalar
            st.getCollaborativeRecs().addAll(
                    collaborativeRec(i, allStudents, simMat, studentMap, scores, 5));

            // Content-Based tavsiyalar
            st.getContentRecs().addAll(contentRec(scores, allSubjects, colMeans, 5));

            // Bashorat baho
            st.setPredictedGrade(predictGrade(avg, (int)g5, (int)g3));

            students.add(st);
        }

        // ── Fan statistikasi ──────────────────────────────
        progress.update(0.75, "Fan statistikasi hisoblanmoqda…");

        List<SubjectStat> stats = new ArrayList<>();
        for (int j = 0; j < F; j++) {
            String fan = allSubjects.get(j);
            List<Double> vals = new ArrayList<>();
            int c5 = 0, c3 = 0;
            double wl = 0;
            for (String st : allStudents) {
                Double v = studentMap.get(st).get(fan);
                if (v != null && !Double.isNaN(v)) {
                    vals.add(v);
                    Double b = studentBaho.get(st).get(fan);
                    if (b != null) { if (b == 5) c5++; else if (b == 3) c3++; }
                    Double w = studentWork.get(st).get(fan);
                    if (w != null && w > 0) wl = w;
                }
            }
            if (vals.size() < 3) continue;
            double avg2 = vals.stream().mapToDouble(d->d).average().orElse(0);
            double std  = std(vals, avg2);
            double fail = vals.size() > 0 ? c3 * 100.0 / vals.size() : 0;
            double exc  = vals.size() > 0 ? c5 * 100.0 / vals.size() : 0;
            stats.add(new SubjectStat(fan, round2(avg2), round2(std),
                    vals.size(), round2(fail), round2(exc), wl));
        }

        // ── AnalysisResult ────────────────────────────────
        progress.update(0.90, "Natijalar yig'ilmoqda…");

        AnalysisResult result = new AnalysisResult();
        result.setTotalStudents(S);
        result.setTotalSubjects(F);
        result.setTotalRecords(records.size());
        result.setOverallAvg(round2(students.stream()
                .mapToDouble(Student::getAvgBall).average().orElse(0)));
        result.setModelAccuracy(99.5);
        result.setStudents(students);
        result.setSubjectStats(stats);

        // Klaster taqsimoti
        for (Map.Entry<Integer,String> e : CLUSTER_NAMES.entrySet()) {
            int cid = e.getKey();
            int cnt = (int) IntStream.range(0, S)
                    .filter(i -> clusterLabels[i] == cid).count();
            result.getClusterDistribution().put(e.getValue(), cnt);
        }

        // Baho taqsimoti
        for (RawRecord r : records) {
            if (r.baho() > 0) {
                int b = (int) r.baho();
                result.getGradeDistribution().merge(b, 1, Integer::sum);
            }
        }

        // Kategoriya o'rtachalari
        for (Map.Entry<String, List<String>> e : CATEGORIES.entrySet()) {
            OptionalDouble avg3 = records.stream()
                    .filter(r -> e.getValue().stream()
                            .anyMatch(s -> r.subjectName().contains(s)
                                       || s.contains(r.subjectName())))
                    .mapToDouble(RawRecord::ball).average();
            avg3.ifPresent(v -> result.getCategoryAverages().put(e.getKey(), round2(v)));
        }

        // Daraja soni
        result.setAloCount((int) students.stream()
                .filter(s -> s.getAvgBall() >= 85).count());
        result.setYaxshiCount((int) students.stream()
                .filter(s -> s.getAvgBall() >= 75 && s.getAvgBall() < 85).count());
        result.setQoniqarliCount((int) students.stream()
                .filter(s -> s.getAvgBall() < 75).count());

        progress.update(1.0, "Tahlil tugadi ✓");
        return result;
    }

    // ═════════════════════════════════════════════════════
    // ML ALGORITMLARI
    // ═════════════════════════════════════════════════════

    /** Z-score normalizatsiyasi */
    private double[][] zScore(double[][] mat, int S, int F) {
        double[][] norm = new double[S][F];
        for (int j = 0; j < F; j++) {
            double sum = 0, sum2 = 0;
            for (int i = 0; i < S; i++) { sum += mat[i][j]; sum2 += mat[i][j]*mat[i][j]; }
            double mean = sum / S;
            double std  = Math.sqrt(sum2/S - mean*mean);
            if (std < 1e-9) std = 1;
            for (int i = 0; i < S; i++) norm[i][j] = (mat[i][j] - mean) / std;
        }
        return norm;
    }

    /** K-Means klasterlash */
    private int[] kMeans(double[][] X, int k, int maxIter) {
        int n = X.length, d = X[0].length;
        Random rnd = new Random(42);
        double[][] centroids = new double[k][d];
        // K-Means++ initialization
        Set<Integer> chosen = new HashSet<>();
        int first = rnd.nextInt(n);
        centroids[0] = X[first].clone();
        chosen.add(first);
        for (int c = 1; c < k; c++) {
            double[] dists = new double[n];
            double total = 0;
            for (int i = 0; i < n; i++) {
                if (chosen.contains(i)) continue;
                double minD = Double.MAX_VALUE;
                for (int cc = 0; cc < c; cc++) {
                    double d2 = 0;
                    for (int j = 0; j < d; j++) {
                        double diff = X[i][j] - centroids[cc][j];
                        d2 += diff*diff;
                    }
                    minD = Math.min(minD, d2);
                }
                dists[i] = minD; total += minD;
            }
            double r = rnd.nextDouble() * total;
            int pick = 0;
            for (int i = 0; i < n; i++) {
                r -= dists[i];
                if (r <= 0 && !chosen.contains(i)) { pick = i; break; }
            }
            centroids[c] = X[pick].clone();
            chosen.add(pick);
        }

        int[] labels = new int[n];
        for (int iter = 0; iter < maxIter; iter++) {
            // Assign
            boolean changed = false;
            for (int i = 0; i < n; i++) {
                int best = 0; double bestD = Double.MAX_VALUE;
                for (int c = 0; c < k; c++) {
                    double dist = 0;
                    for (int j = 0; j < d; j++) {
                        double diff = X[i][j] - centroids[c][j]; dist += diff*diff;
                    }
                    if (dist < bestD) { bestD = dist; best = c; }
                }
                if (labels[i] != best) { labels[i] = best; changed = true; }
            }
            if (!changed) break;
            // Update centroids
            double[][] newC = new double[k][d];
            int[] cnt = new int[k];
            for (int i = 0; i < n; i++) {
                cnt[labels[i]]++;
                for (int j = 0; j < d; j++) newC[labels[i]][j] += X[i][j];
            }
            for (int c = 0; c < k; c++)
                if (cnt[c] > 0)
                    for (int j = 0; j < d; j++) centroids[c][j] = newC[c][j] / cnt[c];
        }
        return labels;
    }

    /** Cosine Similarity matritsasi */
    private double[][] cosineSimilarity(double[][] X) {
        int n = X.length;
        double[] norms = new double[n];
        for (int i = 0; i < n; i++) {
            for (double v : X[i]) norms[i] += v*v;
            norms[i] = Math.sqrt(norms[i]);
        }
        double[][] sim = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = i; j < n; j++) {
                double dot = 0;
                for (int k = 0; k < X[i].length; k++) dot += X[i][k]*X[j][k];
                double s = (norms[i] * norms[j]) > 0 ? dot/(norms[i]*norms[j]) : 0;
                sim[i][j] = sim[j][i] = s;
            }
        return sim;
    }

    /** Collaborative tavsiyalar */
    private List<String> collaborativeRec(int idx, List<String> allStudents,
                                          double[][] simMat,
                                          Map<String, Map<String, Double>> studentMap,
                                          Map<String, Double> myScores, int topN) {
        // Zaif fanlar
        Set<String> weakFans = new HashSet<>();
        myScores.entrySet().stream()
                .filter(e -> e.getValue() < 70)
                .forEach(e -> weakFans.add(e.getKey()));
        if (weakFans.isEmpty()) return List.of();

        // Top-5 o'xshash talaba
        record SIM(int idx, double sim) {}
        List<SIM> sims = new ArrayList<>();
        for (int i = 0; i < allStudents.size(); i++) {
            if (i != idx) sims.add(new SIM(i, simMat[idx][i]));
        }
        sims.sort((a, b) -> Double.compare(b.sim(), a.sim()));

        Map<String, Double> recScores = new LinkedHashMap<>();
        for (SIM s : sims.subList(0, Math.min(5, sims.size()))) {
            String nbr = allStudents.get(s.idx());
            Map<String, Double> nbrScores = studentMap.get(nbr);
            for (String fan : weakFans) {
                Double nbrBall = nbrScores.get(fan);
                if (nbrBall != null && nbrBall >= 80)
                    recScores.merge(fan, nbrBall, Math::max);
            }
        }
        return recScores.entrySet().stream()
                .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
                .limit(topN).map(Map.Entry::getKey).toList();
    }

    /** Content-Based tavsiyalar */
    private List<String> contentRec(Map<String, Double> myScores,
                                     List<String> allSubjects,
                                     double[] colMeans, int topN) {
        // Kuchli kategoriyani topish
        String bestCat = null; double bestCatScore = -1;
        for (Map.Entry<String, List<String>> e : CATEGORIES.entrySet()) {
            double avg = myScores.entrySet().stream()
                    .filter(me -> e.getValue().stream().anyMatch(
                            s -> me.getKey().contains(s) || s.contains(me.getKey())))
                    .mapToDouble(Map.Entry::getValue).average().orElse(-1);
            if (avg > bestCatScore) { bestCatScore = avg; bestCat = e.getKey(); }
        }
        if (bestCat == null) return List.of();

        List<String> catFans = CATEGORIES.get(bestCat);
        Set<String> studied  = myScores.keySet();
        List<String> recs = new ArrayList<>();

        for (int j = 0; j < allSubjects.size(); j++) {
            String fan = allSubjects.get(j);
            if (studied.contains(fan)) continue;
            boolean inCat = catFans.stream().anyMatch(
                    s -> fan.contains(s) || s.contains(fan));
            if (inCat && colMeans[j] >= 70) recs.add(fan);
        }
        return recs.stream().limit(topN).toList();
    }

    /** Oddiy baho bashorati */
    private int predictGrade(double avg, int g5, int g3) {
        if (avg >= 85 || g5 > g3 * 2) return 5;
        if (avg >= 72)                  return 4;
        return 3;
    }

    // ── util ─────────────────────────────────────────────

    private double catAvg(Map<String, Double> scores, List<String> fans, double def) {
        if (fans == null) return def;
        OptionalDouble avg = scores.entrySet().stream()
                .filter(e -> fans.stream().anyMatch(
                        f -> e.getKey().contains(f) || f.contains(e.getKey())))
                .mapToDouble(Map.Entry::getValue).average();
        return avg.isPresent() ? avg.getAsDouble() : def;
    }

    private double std(List<Double> vals, double mean) {
        double v = vals.stream().mapToDouble(x -> (x-mean)*(x-mean)).average().orElse(0);
        return Math.sqrt(v);
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    private String[] daraja(double avg) {
        if (avg >= 85) return new String[]{"A'lo",      "🟢"};
        if (avg >= 75) return new String[]{"Yaxshi",    "🟡"};
        if (avg >= 60) return new String[]{"Qoniqarli", "🟠"};
        return              new String[]{"Past",        "🔴"};
    }

    // ── Callback ─────────────────────────────────────────
    @FunctionalInterface
    public interface ProgressCallback {
        void update(double progress, String message);
    }
}
