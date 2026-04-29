package com.atservis.view;

import com.atservis.model.*;
import com.atservis.service.ExcelParser;
import com.atservis.service.MLEngine;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;

import java.io.File;
import java.util.*;
import java.util.stream.*;

/**
 * Asosiy oyna — sidebar + 5 tab: Dashboard, Talabalar, Fanlar, Tavsiyalar, Haqida
 */
public class MainWindow {

    // ── Ranglar ───────────────────────────────────────────
    private static final String C_BG      = "#F5F4F0";
    private static final String C_SURFACE = "#FFFFFF";
    private static final String C_SIDEBAR = "#18181C";
    private static final String C_ACCENT  = "#2563EB";
    private static final String C_GREEN   = "#059669";
    private static final String C_AMBER   = "#D97706";
    private static final String C_RED     = "#DC2626";
    private static final String C_TEXT    = "#18181B";
    private static final String C_MUTED   = "#71717A";
    private static final String C_BORDER  = "#E4E4E7";

    // ── State ────────────────────────────────────────────
    private AnalysisResult result;
    private Stage stage;

    // ── UI bosh elementlari ──────────────────────────────
    private BorderPane root;
    private StackPane  contentArea;
    private Label      statusLabel;
    private ProgressBar progressBar;

    // ── Sahifalar ────────────────────────────────────────
    private Pane dashboardPage;
    private Pane studentsPage;
    private Pane subjectsPage;
    private Pane recsPage;
    private Pane aboutPage;

    private List<Button> navButtons = new ArrayList<>();

    public void show(Stage primaryStage) {
        this.stage = primaryStage;

        root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_BG + ";");

        root.setLeft(buildSidebar());
        root.setCenter(buildCenter());
        root.setBottom(buildStatusBar());

        showWelcome();

        Scene scene = new Scene(root, 1280, 820);
        primaryStage.setScene(scene);
        primaryStage.setTitle("AT-Servis  ·  ML Recommendation System");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(620);
        primaryStage.show();
    }

    // ═════════════════════════════════════════════════════
    // SIDEBAR
    // ═════════════════════════════════════════════════════

    private VBox buildSidebar() {
        VBox sb = new VBox();
        sb.setPrefWidth(220);
        sb.setStyle("-fx-background-color:" + C_SIDEBAR + ";");

        // Logo
        VBox logo = new VBox(4);
        logo.setPadding(new Insets(28, 20, 22, 20));
        logo.setBorder(new Border(new BorderStroke(
                Color.web("#2A2A2F"), BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, new BorderWidths(0,0,1,0))));

        Label title = new Label("AT-Servis");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);

        Label sub = new Label("ML Recommendation System");
        sub.setFont(Font.font("System", FontWeight.NORMAL, 10));
        sub.setTextFill(Color.web("#71717A"));

        logo.getChildren().addAll(title, sub);

        // Nav buttons
        VBox nav = new VBox(2);
        nav.setPadding(new Insets(14, 10, 10, 10));

        String[][] items = {
            {"🏠", "Dashboard"},
            {"👥", "Talabalar"},
            {"📚", "Fanlar"},
            {"💡", "Tavsiyalar"},
            {"ℹ", "Haqida"},
        };

        for (int i = 0; i < items.length; i++) {
            Button btn = navBtn(items[i][0], items[i][1], i);
            nav.getChildren().add(btn);
            navButtons.add(btn);
        }

        // Upload button
        VBox bottom = new VBox(8);
        bottom.setPadding(new Insets(14, 10, 20, 10));
        bottom.setBorder(new Border(new BorderStroke(
                Color.web("#2A2A2F"), BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, new BorderWidths(1,0,0,0))));

        Button uploadBtn = new Button("📂  Dataset Yuklash");
        uploadBtn.setMaxWidth(Double.MAX_VALUE);
        uploadBtn.setStyle(
            "-fx-background-color:#2563EB; -fx-text-fill:white;" +
            "-fx-font-weight:bold; -fx-font-size:12;" +
            "-fx-background-radius:8; -fx-padding:10 14;");
        uploadBtn.setOnAction(e -> openFileChooser());
        uploadBtn.setOnMouseEntered(e ->
            uploadBtn.setStyle(uploadBtn.getStyle().replace("#2563EB","#1D4ED8")));
        uploadBtn.setOnMouseExited(e ->
            uploadBtn.setStyle(uploadBtn.getStyle().replace("#1D4ED8","#2563EB")));

        bottom.getChildren().add(uploadBtn);

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sb.getChildren().addAll(logo, nav, spacer, bottom);
        return sb;
    }

    private Button navBtn(String icon, String label, int index) {
        Button btn = new Button(icon + "  " + label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("System", FontWeight.NORMAL, 13));
        btn.setStyle(navStyle(false));
        btn.setOnAction(e -> navigateTo(index, btn));
        btn.setOnMouseEntered(e -> { if (!btn.getStyle().contains("2563EB"))
            btn.setStyle("-fx-background-color:#2A2A2F; -fx-text-fill:#D4D4D8;"
                       + "-fx-background-radius:8; -fx-padding:9 12; -fx-alignment:CENTER_LEFT;"); });
        btn.setOnMouseExited(e -> { if (!btn.getStyle().contains("2563EB"))
            btn.setStyle(navStyle(false)); });
        return btn;
    }

    private String navStyle(boolean active) {
        if (active)
            return "-fx-background-color:#2563EB; -fx-text-fill:white;"
                 + "-fx-background-radius:8; -fx-padding:9 12; -fx-alignment:CENTER_LEFT;";
        return "-fx-background-color:transparent; -fx-text-fill:#A1A1AA;"
             + "-fx-background-radius:8; -fx-padding:9 12; -fx-alignment:CENTER_LEFT;";
    }

    private void navigateTo(int index, Button active) {
        navButtons.forEach(b -> b.setStyle(navStyle(false)));
        active.setStyle(navStyle(true));
        if (result == null) {
            showWelcome(); return;
        }
        Pane[] pages = {dashboardPage, studentsPage, subjectsPage, recsPage, aboutPage};
        if (pages[index] != null) {
            contentArea.getChildren().setAll(pages[index]);
        }
    }

    // ═════════════════════════════════════════════════════
    // CENTER
    // ═════════════════════════════════════════════════════

    private StackPane buildCenter() {
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color:" + C_BG + ";");
        return contentArea;
    }

    // ═════════════════════════════════════════════════════
    // STATUS BAR
    // ═════════════════════════════════════════════════════

    private HBox buildStatusBar() {
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(6, 16, 6, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color:#FFFFFF; -fx-border-color:" + C_BORDER + "; -fx-border-width:1 0 0 0;");

        statusLabel = new Label("Tayyor. Dataset yuklash uchun «Dataset Yuklash» tugmasini bosing.");
        statusLabel.setFont(Font.font("System", 11));
        statusLabel.setTextFill(Color.web(C_MUTED));

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(180);
        progressBar.setVisible(false);
        progressBar.setStyle("-fx-accent:#2563EB;");

        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Label ver = new Label("v1.0 · Java 21 · JavaFX");
        ver.setFont(Font.font("System", 10));
        ver.setTextFill(Color.web(C_MUTED));

        bar.getChildren().addAll(statusLabel, progressBar, spacer, ver);
        return bar;
    }

    // ═════════════════════════════════════════════════════
    // WELCOME / DROP ZONE
    // ═════════════════════════════════════════════════════

    private void showWelcome() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        // Drop zone
        VBox dropZone = new VBox(16);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPrefWidth(480); dropZone.setPrefHeight(280);
        dropZone.setStyle(
            "-fx-background-color:#FFFFFF; -fx-border-color:#BFDBFE;" +
            "-fx-border-width:2; -fx-border-style:dashed; -fx-border-radius:16;" +
            "-fx-background-radius:16;");

        Label icon = new Label("📂");
        icon.setFont(Font.font(52));

        Label h = new Label("Dataset yuklash");
        h.setFont(Font.font("System", FontWeight.BOLD, 20));
        h.setTextFill(Color.web(C_TEXT));

        Label sub = new Label("AT_servis.xlsx faylini bu yerga tashlang\nyoki tugmani bosing");
        sub.setFont(Font.font("System", 13));
        sub.setTextFill(Color.web(C_MUTED));
        sub.setTextAlignment(TextAlignment.CENTER);

        Button openBtn = new Button("Faylni tanlash");
        openBtn.setStyle(
            "-fx-background-color:#2563EB; -fx-text-fill:white;" +
            "-fx-font-weight:bold; -fx-font-size:13;" +
            "-fx-background-radius:8; -fx-padding:10 24;");
        openBtn.setOnAction(e -> openFileChooser());

        dropZone.getChildren().addAll(icon, h, sub, openBtn);

        // Drag-and-drop
        dropZone.setOnDragOver((DragEvent ev) -> {
            if (ev.getDragboard().hasFiles()) ev.acceptTransferModes(TransferMode.COPY);
            ev.consume();
        });
        dropZone.setOnDragDropped((DragEvent ev) -> {
            Dragboard db = ev.getDragboard();
            if (db.hasFiles()) loadFile(db.getFiles().get(0));
            ev.setDropCompleted(true); ev.consume();
        });

        // Stats bloklar (faqat namuna)
        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER);
        for (String[] s : new String[][]{
            {"📊","Ma'lumotlar tahlili"},{"🤖","ML Algoritmlar"},
            {"💡","Shaxsiy tavsiyalar"},{"📋","Hisobotlar"}}) {
            VBox c = new VBox(6);
            c.setAlignment(Pos.CENTER);
            c.setPadding(new Insets(16));
            c.setStyle("-fx-background-color:#F8FAFC; -fx-border-color:" + C_BORDER
                     + "; -fx-border-radius:10; -fx-background-radius:10;");
            Label ico = new Label(s[0]); ico.setFont(Font.font(22));
            Label lbl = new Label(s[1]);
            lbl.setFont(Font.font("System", 11));
            lbl.setTextFill(Color.web(C_MUTED));
            c.getChildren().addAll(ico, lbl);
            stats.getChildren().add(c);
        }

        box.getChildren().addAll(dropZone, stats);
        contentArea.getChildren().setAll(box);
    }

    // ═════════════════════════════════════════════════════
    // FILE LOADING
    // ═════════════════════════════════════════════════════

    private void openFileChooser() {
        FileChooser fc = new FileChooser();
        fc.setTitle("AT_servis.xlsx faylini tanlang");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel fayllari", "*.xlsx", "*.xls"));
        File f = fc.showOpenDialog(stage);
        if (f != null) loadFile(f);
    }

    private void loadFile(File file) {
        progressBar.setVisible(true);
        progressBar.setProgress(0);
        setStatus("Yuklanmoqda: " + file.getName());

        Task<AnalysisResult> task = new Task<>() {
            @Override protected AnalysisResult call() throws Exception {
                ExcelParser parser = new ExcelParser();
                List<ExcelParser.RawRecord> records = parser.parse(file);
                MLEngine engine = new MLEngine();
                return engine.analyze(records, (prog, msg) ->
                    Platform.runLater(() -> {
                        progressBar.setProgress(prog);
                        setStatus(msg);
                    }));
            }
        };

        task.setOnSucceeded(e -> {
            result = task.getValue();
            progressBar.setVisible(false);
            setStatus("Tayyor ✓  Talabalar: " + result.getTotalStudents()
                    + "  ·  Fanlar: " + result.getTotalSubjects()
                    + "  ·  O'rtacha: " + result.getOverallAvg());
            buildAllPages();
            navigateTo(0, navButtons.get(0));
        });

        task.setOnFailed(e -> {
            progressBar.setVisible(false);
            setStatus("❌ Xato: " + task.getException().getMessage());
            new Alert(Alert.AlertType.ERROR,
                "Fayl o'qishda xato:\n" + task.getException().getMessage()).show();
        });

        new Thread(task, "ml-thread").start();
    }

    // ═════════════════════════════════════════════════════
    // BUILD ALL PAGES
    // ═════════════════════════════════════════════════════

    private void buildAllPages() {
        dashboardPage = buildDashboard();
        studentsPage  = buildStudentsPage();
        subjectsPage  = buildSubjectsPage();
        recsPage      = buildRecsPage();
        aboutPage     = buildAboutPage();
    }

    // ═════════════════════════════════════════════════════
    // PAGE 1 — DASHBOARD
    // ═════════════════════════════════════════════════════

    private Pane buildDashboard() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:" + C_BG + "; -fx-background:transparent;");

        VBox page = new VBox(20);
        page.setPadding(new Insets(28, 32, 32, 32));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        // Title
        page.getChildren().add(pageTitle("🏠  Dashboard", "Umumiy ko'rinish va statistika"));

        // KPI row
        HBox kpis = new HBox(14);
        kpis.getChildren().addAll(
            kpiCard("Talabalar",        String.valueOf(result.getTotalStudents()),    C_ACCENT),
            kpiCard("Fanlar",           String.valueOf(result.getTotalSubjects()),     "#7C3AED"),
            kpiCard("Yozuvlar",         String.format("%,d", result.getTotalRecords()),  "#0D9488"),
            kpiCard("O'rtacha ball",    String.valueOf(result.getOverallAvg()),       C_AMBER),
            kpiCard("Model aniqligi",   result.getModelAccuracy() + "%",             C_GREEN)
        );
        page.getChildren().add(kpis);

        // Charts row
        HBox charts = new HBox(14);

        // Baho chart
        PieChart pie = new PieChart();
        pie.setTitle("Baholar taqsimoti");
        pie.setLegendVisible(true);
        pie.setPrefSize(300, 260);
        result.getGradeDistribution().forEach((b, cnt) -> {
            PieChart.Data d = new PieChart.Data("Baho " + b, cnt);
            pie.getData().add(d);
        });
        pie.setStyle("-fx-background-color:white; -fx-background-radius:12;");

        // Klaster bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        yAxis.setLabel("Talabalar");
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Klasterlar");
        barChart.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        result.getClusterDistribution().forEach((name, cnt) -> {
            String shortName = name.length() > 18 ? name.substring(0,17)+"…" : name;
            series.getData().add(new XYChart.Data<>(shortName, cnt));
        });
        barChart.getData().add(series);
        barChart.setPrefSize(380, 260);
        barChart.setStyle("-fx-background-color:white; -fx-background-radius:12;");

        // Kategoriya line/bar
        CategoryAxis cx = new CategoryAxis();
        NumberAxis   cy = new NumberAxis(55, 95, 5);
        cy.setLabel("Ball");
        BarChart<String, Number> catChart = new BarChart<>(cx, cy);
        catChart.setTitle("Fan kategoriyalari");
        catChart.setLegendVisible(false);
        XYChart.Series<String, Number> catSeries = new XYChart.Series<>();
        result.getCategoryAverages().forEach((cat, avg) ->
            catSeries.getData().add(new XYChart.Data<>(cat, avg)));
        catChart.getData().add(catSeries);
        catChart.setPrefSize(380, 260);
        catChart.setStyle("-fx-background-color:white; -fx-background-radius:12;");

        charts.getChildren().addAll(
            card(pie), card(barChart), card(catChart));

        // Daraja distribution
        HBox darajaRow = new HBox(14);
        int total = result.getTotalStudents();
        darajaRow.getChildren().addAll(
            statBox("🟢 A'lo  (≥85)", result.getAloCount(), total, C_GREEN),
            statBox("🟡 Yaxshi (75–85)", result.getYaxshiCount(), total, C_AMBER),
            statBox("🟠 Qoniqarli (<75)", result.getQoniqarliCount(), total, C_RED)
        );

        page.getChildren().addAll(charts, darajaRow);

        sp.setContent(page);
        return sp;
    }

    // ═════════════════════════════════════════════════════
    // PAGE 2 — TALABALAR
    // ═════════════════════════════════════════════════════

    private Pane buildStudentsPage() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(28, 32, 32, 32));
        page.setStyle("-fx-background-color:" + C_BG + ";");

        page.getChildren().add(pageTitle("👥  Talabalar", "Barcha talabalar ro'yxati va tahlili"));

        // Search + filter
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText("🔍  Talaba ismi bo'yicha qidiring…");
        search.setPrefWidth(280);
        search.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER
                      + "; -fx-border-radius:8; -fx-background-radius:8; -fx-padding:8 12;");

        ComboBox<String> filterBox = new ComboBox<>(FXCollections.observableArrayList(
            "Barchasi", "A'lo (≥85)", "Yaxshi (75–85)", "Qoniqarli (<75)",
            "A'lochi talabalar", "Texnik yo'nalishga moyil",
            "O'rtacha talabalar", "Qo'shimcha yordam kerak"
        ));
        filterBox.setValue("Barchasi");
        filterBox.setStyle("-fx-background-radius:8; -fx-border-radius:8;");

        toolbar.getChildren().addAll(search, filterBox);

        // Table
        TableView<Student> table = new TableView<>();
        table.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER
                     + "; -fx-border-radius:10;");

        TableColumn<Student, String> cName = col("Talaba ismi", 280);
        cName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<Student, Number> cBall = col2("O'rt.ball", 90);
        cBall.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getAvgBall()));
        cBall.setCellFactory(tc -> ballCell());

        TableColumn<Student, String> cDaraja = col("Daraja", 100);
        cDaraja.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getRang() + " " + c.getValue().getDaraja()));

        TableColumn<Student, String> cCluster = col("Klaster", 180);
        cCluster.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClusterName()));

        TableColumn<Student, Number> cTech = col2("Texnik", 80);
        cTech.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTechAvg()));

        TableColumn<Student, Number> cMath = col2("Matematika", 90);
        cMath.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getMathAvg()));

        TableColumn<Student, Number> c5 = col2("5-baho", 70);
        c5.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getGrade5Count()));

        TableColumn<Student, Number> c3 = col2("3-baho", 70);
        c3.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getGrade3Count()));

        TableColumn<Student, Number> cPred = col2("Bashorat", 75);
        cPred.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPredictedGrade()));

        table.getColumns().addAll(cName, cBall, cDaraja, cCluster, cTech, cMath, c5, c3, cPred);

        var items = FXCollections.observableArrayList(result.getStudents());
        var filtered = new FilteredList<>(items, s -> true);
        table.setItems(filtered);

        // Filter logic
        Runnable applyFilter = () -> {
            String q   = search.getText().toLowerCase();
            String cat = filterBox.getValue();
            filtered.setPredicate(s -> {
                boolean nameOk = q.isEmpty() || s.getName().toLowerCase().contains(q);
                boolean catOk  = switch(cat) {
                    case "A'lo (≥85)"     -> s.getAvgBall() >= 85;
                    case "Yaxshi (75–85)" -> s.getAvgBall() >= 75 && s.getAvgBall() < 85;
                    case "Qoniqarli (<75)"-> s.getAvgBall() < 75;
                    case "Barchasi"       -> true;
                    default               -> s.getClusterName().equals(cat);
                };
                return nameOk && catOk;
            });
        };
        search.textProperty().addListener((o,a,b) -> applyFilter.run());
        filterBox.valueProperty().addListener((o,a,b) -> applyFilter.run());

        // Detail on click
        table.getSelectionModel().selectedItemProperty().addListener((o,a,student) -> {
            if (student != null) showStudentDetail(student);
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        page.getChildren().addAll(toolbar, table);
        return page;
    }

    // ═════════════════════════════════════════════════════
    // PAGE 3 — FANLAR
    // ═════════════════════════════════════════════════════

    private Pane buildSubjectsPage() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(28, 32, 32, 32));
        page.setStyle("-fx-background-color:" + C_BG + ";");
        page.getChildren().add(pageTitle("📚  Fanlar", "Fan statistikasi va baholash tahlili"));

        TableView<SubjectStat> table = new TableView<>();
        table.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER
                     + "; -fx-border-radius:10;");

        TableColumn<SubjectStat, String> cName = col("Fan nomi", 260);
        cName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<SubjectStat, Number> cAvg = col2("O'rt.ball", 90);
        cAvg.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getAvgBall()));
        cAvg.setCellFactory(tc -> ballCell());

        TableColumn<SubjectStat, Number> cStd = col2("Std.og'ish", 90);
        cStd.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getStdDev()));

        TableColumn<SubjectStat, Number> cSt = col2("Talabalar", 90);
        cSt.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getStudentCount()));

        TableColumn<SubjectStat, Number> cFail = col2("3-baho %", 90);
        cFail.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getFailRate()));
        cFail.setCellFactory(tc -> failCell());

        TableColumn<SubjectStat, Number> cExcel = col2("5-baho %", 90);
        cExcel.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getExcelRate()));

        TableColumn<SubjectStat, Number> cWl = col2("Yuklama(h)", 95);
        cWl.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getWorkload()));

        TableColumn<SubjectStat, String> cStatus = col("Holat", 100);
        cStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        cStatus.setCellFactory(tc -> statusCell());

        table.getColumns().addAll(cName, cAvg, cStd, cSt, cFail, cExcel, cWl, cStatus);
        table.setItems(FXCollections.observableArrayList(
            result.getSubjectStats().stream()
                  .sorted(Comparator.comparingDouble(SubjectStat::getAvgBall))
                  .toList()));
        VBox.setVgrow(table, Priority.ALWAYS);
        page.getChildren().add(table);
        return page;
    }

    // ═════════════════════════════════════════════════════
    // PAGE 4 — TAVSIYALAR
    // ═════════════════════════════════════════════════════

    private Pane buildRecsPage() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(28, 32, 32, 32));
        page.setStyle("-fx-background-color:" + C_BG + ";");
        page.getChildren().add(pageTitle("💡  Tavsiyalar", "O'quv reja optimallashtirish bo'yicha tavsiyalar"));

        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:transparent; -fx-background-color:transparent;");

        VBox cards = new VBox(14);

        String[][] recs = {
            {"🔴 KRITIK", "Matematika fanlarini qayta strukturalash",
             "Matematika 1, 2, Calculus, Ehtimollik — barchasi 240 soatlik yuklama bilan 67–70 ball. "
           + "Tavsiya: har birini 120 soatlik modullarga bo'lish, aralash o'qitishni joriy etish.", C_RED},
            {"🔴 KRITIK", "C++ va Dasturlash fanlarida tizimli muammo",
             "C++, Dasturlash 1/2, Sonli usullar — 60–68% talaba 3-baho olmoqda. "
           + "Tavsiya: pair programming, mentorlik tizimi, laboratoriya soatlarini oshirish.", C_RED},
            {"🟠 MUHIM", "Yuqori variatsiyal fanlarni standartlashtirish",
             "Xorijiy til DAK (std=12.5), Windows Server (std=11.8) — o'qituvchiga qarab katta farq. "
           + "Tavsiya: baholash rubrikalari, cross-grading, koordinatsiya.", C_AMBER},
            {"🟡 O'RTA", "Katta yuklamali fanlarni optimallashtirish",
             "300+ soatlik fanlar eng past samaradorlik. "
           + "Bitiruv ishi (540h) semestr boshida boshlansin.", C_AMBER},
            {"🟢 YAXSHI", "Amaliy fanlar tajribasini kengaytirish",
             "Metrologiya (86.2), Cloud (87.5), Web texnologiya (85.7) — muvaffaqiyat sababi: "
           + "amaliy laboratoriya, real loyihalar. Bu metodologiyani muammo fanlarga ko'chiring.", C_GREEN},
            {"🔵 STRATEGIK", "Loyiha asosida o'qitishni joriy etish",
             "Individual loyiha 2 (78.6) — tajriba ortsa natija yaxshilanmoqda. "
           + "Barcha texnik fanlarga mini-loyiha elementlarini kiriting.", C_ACCENT},
        };

        for (String[] r : recs) {
            VBox card = new VBox(8);
            card.setPadding(new Insets(16, 20, 16, 20));
            card.setStyle("-fx-background-color:white; -fx-border-color:" + r[3]
                        + "; -fx-border-width:0 0 0 4; -fx-background-radius:10;");

            Label priority = new Label(r[0]);
            priority.setFont(Font.font("System", FontWeight.BOLD, 10));
            priority.setTextFill(Color.web(r[3]));

            Label title2 = new Label(r[1]);
            title2.setFont(Font.font("System", FontWeight.BOLD, 14));
            title2.setTextFill(Color.web(C_TEXT));

            Label body = new Label(r[2]);
            body.setFont(Font.font("System", 12));
            body.setTextFill(Color.web(C_MUTED));
            body.setWrapText(true);

            card.getChildren().addAll(priority, title2, body);
            cards.getChildren().add(card);
        }

        sp.setContent(cards);
        VBox.setVgrow(sp, Priority.ALWAYS);
        page.getChildren().add(sp);
        return page;
    }

    // ═════════════════════════════════════════════════════
    // PAGE 5 — HAQIDA
    // ═════════════════════════════════════════════════════

    private Pane buildAboutPage() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:transparent; -fx-background-color:transparent;");

        VBox page = new VBox(20);
        page.setPadding(new Insets(28, 32, 40, 32));
        page.setStyle("-fx-background-color:" + C_BG + ";");
        page.getChildren().add(pageTitle("ℹ  Tizim haqida", "AT-Servis ML Recommendation System"));

        VBox info = new VBox(12);
        String[][] rows = {
            {"Tizim nomi",   "AT-Servis ML Recommendation System"},
            {"Versiya",      "1.0.0"},
            {"Dasturlash",   "Java 21 + JavaFX 21"},
            {"ML Algoritm",  "K-Means, Cosine Similarity, Content-Based, Naïve Bayes"},
            {"Dataset",      "5330502 — Kompyuter injiniringi: AT-Servis"},
            {"Talabalar",    String.valueOf(result.getTotalStudents())},
            {"Fanlar",       String.valueOf(result.getTotalSubjects())},
            {"Model aniqligi", result.getModelAccuracy() + "%"},
        };
        for (String[] row : rows) {
            HBox r = new HBox(16);
            r.setPadding(new Insets(10, 16, 10, 16));
            r.setStyle("-fx-background-color:white; -fx-border-color:" + C_BORDER
                     + "; -fx-border-radius:8; -fx-background-radius:8;");
            Label k = new Label(row[0]);
            k.setFont(Font.font("System", FontWeight.BOLD, 12));
            k.setTextFill(Color.web(C_MUTED));
            k.setPrefWidth(160);
            Label v = new Label(row[1]);
            v.setFont(Font.font("System", 12));
            v.setTextFill(Color.web(C_TEXT));
            r.getChildren().addAll(k, v);
            info.getChildren().add(r);
        }

        page.getChildren().add(info);
        sp.setContent(page);
        return sp;
    }

    // ═════════════════════════════════════════════════════
    // STUDENT DETAIL DIALOG
    // ═════════════════════════════════════════════════════

    private void showStudentDetail(Student s) {
        Stage dlg = new Stage();
        dlg.initOwner(stage);
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.setTitle(s.getName());

        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        VBox box = new VBox(16);
        box.setPadding(new Insets(24));
        box.setStyle("-fx-background-color:" + C_BG + ";");

        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label(initials(s.getName()));
        avatar.setFont(Font.font("System", FontWeight.BOLD, 22));
        avatar.setTextFill(Color.WHITE);
        avatar.setStyle("-fx-background-color:#18181C; -fx-background-radius:10;"
                      + "-fx-padding:10 14; -fx-min-width:56; -fx-alignment:CENTER;");

        VBox nameBox = new VBox(4);
        Label name = new Label(s.getName());
        name.setFont(Font.font("System", FontWeight.BOLD, 16));
        HBox tags = new HBox(8);
        tags.getChildren().addAll(
            tag(s.getRang() + " " + s.getDaraja(), ballColor(s.getAvgBall())),
            tag(String.format("%.1f ball", s.getAvgBall()), C_ACCENT),
            tag(s.getClusterName(), "#7C3AED")
        );
        nameBox.getChildren().addAll(name, tags);
        header.getChildren().addAll(avatar, nameBox);

        // Stats
        HBox stats = new HBox(12);
        stats.getChildren().addAll(
            miniStat("Texnik", String.valueOf(s.getTechAvg())),
            miniStat("Matematika", String.valueOf(s.getMathAvg())),
            miniStat("Jami fan", String.valueOf(s.getTotalSubjects())),
            miniStat("5-baho", String.valueOf(s.getGrade5Count())),
            miniStat("3-baho", String.valueOf(s.getGrade3Count())),
            miniStat("Bashorat", String.valueOf(s.getPredictedGrade()))
        );

        // Kuchli fanlar
        VBox strong = sectionList("✅  Kuchli fanlar (≥80)",
            s.getStrongSubjects().stream()
             .map(f -> String.format("%-42s  %.0f", f.getName(), f.getBall()))
             .toList(), C_GREEN);

        // Zaif fanlar
        VBox weak = sectionList("⚠  Zaif fanlar (<70)",
            s.getWeakSubjects().stream()
             .map(f -> String.format("%-42s  %.0f", f.getName(), f.getBall()))
             .toList(), C_RED);

        // Tavsiyalar
        VBox colRec = sectionList("🤝  Collaborative tavsiyalar",
            s.getCollaborativeRecs(), C_ACCENT);
        VBox conRec = sectionList("🎯  Content-Based tavsiyalar",
            s.getContentRecs(), "#7C3AED");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(0);
        grid.getColumnConstraints().addAll(
            colCon(), colCon());
        grid.add(strong, 0, 0);
        grid.add(weak,   1, 0);
        grid.add(colRec, 0, 1);
        grid.add(conRec, 1, 1);

        box.getChildren().addAll(header, stats, grid);
        sp.setContent(box);

        Scene scene = new Scene(sp, 740, 560);
        dlg.setScene(scene);
        dlg.show();
    }

    // ═════════════════════════════════════════════════════
    // UI HELPERS
    // ═════════════════════════════════════════════════════

    private VBox pageTitle(String title, String sub) {
        VBox v = new VBox(4);
        Label t = new Label(title);
        t.setFont(Font.font("System", FontWeight.BOLD, 22));
        t.setTextFill(Color.web(C_TEXT));
        Label s = new Label(sub);
        s.setFont(Font.font("System", 13));
        s.setTextFill(Color.web(C_MUTED));
        v.getChildren().addAll(t, s);
        return v;
    }

    private VBox kpiCard(String label, String value, String color) {
        VBox c = new VBox(6);
        c.setPadding(new Insets(18, 20, 16, 20));
        c.setStyle("-fx-background-color:white; -fx-border-radius:12;"
                 + "-fx-background-radius:12; -fx-border-color:" + C_BORDER + ";");
        HBox.setHgrow(c, Priority.ALWAYS);

        Region stripe = new Region();
        stripe.setPrefHeight(3); stripe.setMaxWidth(Double.MAX_VALUE);
        stripe.setStyle("-fx-background-color:" + color + "; -fx-background-radius:3 3 0 0;");

        Label lbl = new Label(label);
        lbl.setFont(Font.font("System", 10));
        lbl.setTextFill(Color.web(C_MUTED));
        Label val = new Label(value);
        val.setFont(Font.font("System", FontWeight.BOLD, 28));
        val.setTextFill(Color.web(color));

        c.getChildren().addAll(stripe, lbl, val);
        return c;
    }

    private VBox card(javafx.scene.Node n) {
        VBox c = new VBox();
        c.setStyle("-fx-background-color:white; -fx-border-radius:12;"
                 + "-fx-background-radius:12; -fx-border-color:" + C_BORDER + ";");
        c.getChildren().add(n);
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    private VBox statBox(String label, int cnt, int total, String color) {
        VBox b = new VBox(6);
        b.setPadding(new Insets(16, 20, 16, 20));
        b.setStyle("-fx-background-color:white; -fx-border-radius:10;"
                 + "-fx-background-radius:10; -fx-border-color:" + C_BORDER + ";");
        HBox.setHgrow(b, Priority.ALWAYS);
        Label lbl = new Label(label);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web(color));
        Label val = new Label(cnt + "  (" + String.format("%.1f", cnt*100.0/total) + "%)");
        val.setFont(Font.font("System", FontWeight.BOLD, 22));
        val.setTextFill(Color.web(color));
        ProgressBar pb = new ProgressBar(cnt * 1.0 / total);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setStyle("-fx-accent:" + color + ";");
        b.getChildren().addAll(lbl, val, pb);
        return b;
    }

    private <S, T> TableColumn<S, T> col(String title, int width) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setPrefWidth(width); return c;
    }

    private <S> TableColumn<S, Number> col2(String title, int width) {
        TableColumn<S, Number> c = new TableColumn<>(title);
        c.setPrefWidth(width); return c;
    }

    private <S> TableCell<S, Number> ballCell() {
        return new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                double d = v.doubleValue();
                setText(String.format("%.1f", d));
                setFont(Font.font("System", FontWeight.BOLD, 12));
                setTextFill(Color.web(d >= 80 ? C_GREEN : d >= 70 ? C_ACCENT : C_RED));
            }
        };
    }

    private <S> TableCell<S, Number> failCell() {
        return new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                double d = v.doubleValue();
                setText(String.format("%.1f%%", d));
                setTextFill(Color.web(d >= 60 ? C_RED : d >= 40 ? C_AMBER : C_GREEN));
            }
        };
    }

    private <S> TableCell<S, String> statusCell() {
        return new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                String col = switch (v) {
                    case "Namunali" -> C_GREEN;
                    case "Yaxshi"   -> "#0D9488";
                    case "Normal"   -> C_ACCENT;
                    case "Muammo"   -> C_AMBER;
                    case "Kritik"   -> C_RED;
                    default         -> C_MUTED;
                };
                setTextFill(Color.web(col));
                setFont(Font.font("System", FontWeight.BOLD, 11));
            }
        };
    }

    private VBox sectionList(String title, List<String> items, String color) {
        VBox v = new VBox(6);
        v.setPadding(new Insets(0, 0, 14, 0));
        Label t = new Label(title);
        t.setFont(Font.font("System", FontWeight.BOLD, 12));
        t.setTextFill(Color.web(color));
        v.getChildren().add(t);
        if (items.isEmpty()) {
            Label none = new Label("Ma'lumot yo'q");
            none.setFont(Font.font("System", 11));
            none.setTextFill(Color.web(C_MUTED));
            v.getChildren().add(none);
        } else {
            for (String item : items) {
                Label l = new Label("  →  " + item);
                l.setFont(Font.font("System", 11));
                l.setTextFill(Color.web(C_TEXT));
                l.setStyle("-fx-background-color:#F8F8F8; -fx-background-radius:6;"
                         + "-fx-padding:5 8; -fx-border-color:" + C_BORDER
                         + "; -fx-border-radius:6;");
                l.setMaxWidth(Double.MAX_VALUE);
                v.getChildren().add(l);
            }
        }
        return v;
    }

    private VBox miniStat(String label, String value) {
        VBox v = new VBox(2);
        v.setAlignment(Pos.CENTER);
        v.setPadding(new Insets(10, 16, 10, 16));
        v.setStyle("-fx-background-color:white; -fx-border-radius:8;"
                 + "-fx-background-radius:8; -fx-border-color:" + C_BORDER + ";");
        HBox.setHgrow(v, Priority.ALWAYS);
        Label val = new Label(value);
        val.setFont(Font.font("System", FontWeight.BOLD, 18));
        Label lbl = new Label(label);
        lbl.setFont(Font.font("System", 10));
        lbl.setTextFill(Color.web(C_MUTED));
        v.getChildren().addAll(val, lbl);
        return v;
    }

    private Label tag(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 10));
        l.setTextFill(Color.web(color));
        l.setStyle("-fx-border-color:" + color + "; -fx-border-radius:12;"
                 + "-fx-padding:2 8;");
        return l;
    }

    private String ballColor(double v) {
        if (v >= 85) return C_GREEN;
        if (v >= 75) return C_ACCENT;
        if (v >= 60) return C_AMBER;
        return C_RED;
    }

    private String initials(String name) {
        String[] parts = name.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++)
            if (!parts[i].isEmpty()) sb.append(parts[i].charAt(0));
        return sb.toString().toUpperCase();
    }

    private ColumnConstraints colCon() {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS); cc.setPercentWidth(50);
        return cc;
    }

    private void setStatus(String msg) {
        Platform.runLater(() -> statusLabel.setText(msg));
    }
}
