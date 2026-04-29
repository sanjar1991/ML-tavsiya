package com.atservis.service;

import com.atservis.model.Student;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * AT_servis.xlsx faylini o'qib, talaba yozuvlarini qaytaradi.
 *
 * Fayl tuzilishi:
 *   Har bir talaba bloki "Talaba" so'zi bilan boshlanadi.
 *   Fan satrlari: [No] [Fan nomi] [...] [...] [...] [Yuklama] [Kredit] [Ball] [Baho]
 *   Ustun indekslari: 0=No 1=Label 2=Value 3=Sem 4=Ozlash 5=Yuklama 6=Kredit 7=Ball 8=Baho
 */
public class ExcelParser {

    /** (fanNomi, talabaNomi) → ball yozuvlari */
    public record RawRecord(
            String studentName,
            String subjectName,
            double ball,
            double baho,
            double workload
    ) {}

    public List<RawRecord> parse(File file) throws Exception {
        List<RawRecord> records = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            String currentStudent = null;

            for (Row row : sheet) {
                String label = str(row, 1);
                String value = str(row, 2);

                if ("Talaba".equalsIgnoreCase(label.trim())) {
                    currentStudent = value.trim();
                    continue;
                }

                if (currentStudent == null) continue;

                // Fan satri: No ustunida raqam bo'lishi kerak
                double noVal = num(row, 0);
                if (Double.isNaN(noVal)) continue;

                String subject  = str(row, 1).trim();
                double ball     = num(row, 7);
                double baho     = num(row, 8);
                double workload = num(row, 5);

                if (subject.isEmpty() || Double.isNaN(ball) || ball <= 0) continue;

                records.add(new RawRecord(
                        currentStudent, subject,
                        ball,
                        Double.isNaN(baho) ? 0 : baho,
                        Double.isNaN(workload) ? 0 : workload
                ));
            }
        }
        return records;
    }

    // ── helpers ──────────────────────────────────────────

    private String str(Row row, int col) {
        if (row == null) return "";
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    private double num(Row row, int col) {
        if (row == null) return Double.NaN;
        Cell cell = row.getCell(col);
        if (cell == null) return Double.NaN;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING  -> {
                try { yield Double.parseDouble(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield Double.NaN; }
            }
            default -> Double.NaN;
        };
    }
}
