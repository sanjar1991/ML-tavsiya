# AT-Servis ML Recommendation — Desktop App

**Java 21 + JavaFX 21**  cross-platform desktop ilova  
Windows · macOS · Linux

---

## 📋 Talablar

| Talab | Versiya |
|-------|---------|
| Java JDK | **21** yoki undan yuqori |
| Apache Maven | **3.8+** |
| RAM | 512 MB+ |

### Java o'rnatish

- **Windows/macOS:** https://adoptium.net  (Eclipse Temurin 21 LTS)
- **Ubuntu/Debian:** `sudo apt install openjdk-21-jdk`
- **Arch:**         `sudo pacman -S jdk21-openjdk`

### Maven o'rnatish

- **Windows:** https://maven.apache.org/download.cgi  → Path ga qo'shing
- **macOS:**   `brew install maven`
- **Ubuntu:**  `sudo apt install maven`

Tekshirish:
```bash
java --version    # "openjdk 21 ..." chiqishi kerak
mvn --version     # "Apache Maven 3.x.x ..." chiqishi kerak
```

---

## 🚀 Ishga tushirish

### 1-qadam: Dataset tayyor qiling

```
ATServisApp/
├── AT_servis.xlsx    ← shu yerga qo'ying (ixtiyoriy, dasturdan ham yuklanadi)
├── pom.xml
├── src/
...
```

### 2-qadam: Kutubxonalarni yuklab oling

```bash
cd ATServisApp
mvn dependency:resolve
```

### 3-qadam: Ishga tushiring

```bash
# To'g'ridan-to'g'ri ishga tushirish (tavsiya etiladi)
mvn javafx:run

# yoki avval compile, so'ng run
mvn compile
mvn javafx:run
```

### 4-qadam: Executable JAR yarating (tarqatish uchun)

```bash
mvn package
# Natija: target/ATServisApp-1.0.0.jar

# Ishga tushirish:
java -jar target/ATServisApp-1.0.0.jar
```

---

## 🖥️ Dastur interfeysi

```
┌─────────────────────────────────────────────────────────────┐
│  Sidebar          │  Asosiy maydon (ContentArea)            │
│  ─────────────    │  ──────────────────────────────────     │
│  🏠 Dashboard     │  KPI kartalar                          │
│  👥 Talabalar     │  Pie chart, Bar chart, Line chart      │
│  📚 Fanlar        │  Fan statistikasi jadvali              │
│  💡 Tavsiyalar    │  O'quv reja tavsiyalari                │
│  ℹ  Haqida       │  Tizim ma'lumotlari                    │
│                   │                                         │
│  📂 Dataset      │                                         │
│     Yuklash       │                                         │
│                   │                                         │
├───────────────────┴─────────────────────────────────────────┤
│  Status bar: progress + xabar                               │
└─────────────────────────────────────────────────────────────┘
```

### Dataset yuklash usullari:

1. **Sidebar:** "Dataset Yuklash" tugmasi → FileChooser oynasi
2. **Drag & Drop:** `.xlsx` faylni markaziy maydon ustiga tashlash

---

## ⚙️ ML Algoritmlar (sof Java)

| Algoritm | Tavsif |
|----------|--------|
| **K-Means (k=4)** | K-Means++ initialization, 50 iteratsiya |
| **Cosine Similarity** | n×n o'xshashlik matritsasi |
| **Collaborative Filtering** | Top-5 qo'shni, zaif fan tavsiyasi |
| **Content-Based Filtering** | 8 kategoriya bo'yicha tavsiya |
| **Naïve Bayes-style** | Baho (3/4/5) bashorati |
| **Z-Score Normalization** | Matritsani normallash |

---

## 📁 Loyiha tuzilishi

```
ATServisApp/
├── pom.xml                              ← Maven konfiguratsiya
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java
│       │   └── com/atservis/
│       │       ├── App.java             ← Entry point
│       │       ├── model/
│       │       │   ├── Student.java
│       │       │   ├── SubjectStat.java
│       │       │   └── AnalysisResult.java
│       │       ├── service/
│       │       │   ├── ExcelParser.java ← Apache POI
│       │       │   └── MLEngine.java    ← ML pipeline
│       │       └── view/
│       │           └── MainWindow.java  ← JavaFX UI
│       └── resources/
└── README.md
```

---

## ❓ Ko'p uchraydigan muammolar

**Problem:** `mvn javafx:run` da "JAVA_HOME not set" xatosi  
**Yechim:** `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`

**Problem:** "Module not found" xatosi  
**Yechim:** Java 21 ekanligini tekshiring: `java --version`

**Problem:** Excel fayli o'qilmaydi  
**Yechim:** Fayl `.xlsx` formatida bo'lishi kerak (eski `.xls` emas)

**Problem:** Maven dependency yuklanmaydi  
**Yechim:** Internet ulanishni tekshiring, keyin: `mvn dependency:resolve -U`
