# CrimeScope AI — Tamil Nadu Crime Intelligence Command Center
### Academic OOPS Microproject & Geospatial Analytics Platform

[![Java 21 LTS](https://img.shields.io/badge/Java-21%20LTS-00f0ff?style=flat-square&logo=openjdk&logoColor=black)](https://corretto.aws/)
[![Spring Boot 3.3.5](https://img.shields.io/badge/Spring%20Boot-3.3.5-00e5a3?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React 19](https://img.shields.io/badge/React-19.0-61dafb?style=flat-square&logo=react&logoColor=black)](https://react.dev/)
[![Leaflet](https://img.shields.io/badge/Leaflet-1.9.4-199900?style=flat-square&logo=leaflet&logoColor=white)](https://leafletjs.com/)
[![FlatLaf Dark](https://img.shields.io/badge/Java%20Swing-FlatLaf%20Dark-f59e0b?style=flat-square)](https://www.formdev.com/flatlaf/)
[![Google Gemini API](https://img.shields.io/badge/Google%20Gemini-2.5%20Flash-8e75ff?style=flat-square&logo=google)](https://ai.google.dev/)
[![Tests Passing](https://img.shields.io/badge/Unit%20Tests-15%2F15%20Passing-00e5a3?style=flat-square)]()

---

## 1. Project Overview

**CrimeScope AI** is a full-stack, enterprise-grade historical crime statistics and geospatial intelligence platform engineered for the state of **Tamil Nadu, India** (covering all 38 districts across 2018–2023). 

Designed as an **Advanced Object-Oriented Programming (OOPS) microproject**, CrimeScope AI bridges robust Java architectural patterns (strict encapsulation, polymorphic analytical strategies, RFC 4180 file handling, direct JDBC telemetry) with a **Futuristic Gaming Command Center** web interface (dark navy/neon cyber aesthetic, 38-district polygon choropleth, dynamic Recharts visualizations) and an accompanying **Java Swing Desktop Client**.

```
========================================================================================
                          CRIMESCOPE AI - SYSTEM TOPOLOGY
========================================================================================
 [ REACT 19 + VITE FRONTEND ]       [ JAVA SWING FLATLAF DESKTOP ]
   (Port 5173 - Cyber Theme)          (Standalone Shaded Fat JAR)
           |                                       |
           +-------------------+-------------------+
                               | HTTP REST / JSON
                               v
               [ SPRING BOOT 3.3.5 BACKEND ]
                    (Port 8080 - Java 21 LTS)
          +--------------------+--------------------+
          |                    |                    |
   [ OOPS Core ]      [ Gemini 2.5 Flash ]   [ RFC 4180 CSV Engine ]
   - Encapsulation      - Pre-computed Bounds  - Finite State Machine
   - BaseEntity Inherit - Strict Anti-Halluc   - UTF-8 BOM Stripper
   - 4 Polymorphic      - Deterministic        - Malformed Row Guard
     Strategies           Rule-Based Fallback  - SHA-256 Hash Duplicate
          |                    |                    |
          +--------------------+--------------------+
                               |
            [ DUAL PERSISTENCE ARCHITECTURE ]
             /                             \
     [ Flyway Migrations ]         [ Direct JDBC Demo ]
     - V1: Schema & Constraints    - PreparedStatement
     - V2: 38 Districts & Cats     - Raw ResultSet Nav
     - V3: 1,611 Seed Records      - Query Telemetry
             \                             /
      [ H2 MySQL-Mode / Production MySQL 8.x ]
========================================================================================
```

---

## 2. Academic OOPS Microproject Compliance

CrimeScope AI was explicitly engineered to satisfy the rigorous grading requirements of an advanced Java Object-Oriented Programming curriculum:

### A. Encapsulation & Defensive State Management
- **Strict Private Access**: All domain entities (`CrimeRecord`, `District`, `CrimeCategory`, `User`, `DatasetImport`) declare private attributes, accessible only via validated accessors and mutators.
- **Defensive Invariant Protection**:
  - `CrimeRecord.calculateClearanceRate()` calculates resolution percentages with zero-denominator guards (`caseCount > 0`), mathematically eliminating `NaN` or `Infinity` errors.
  - Category shares and population densities protect against missing census data or zero division.
  - Immutability of calculation payloads (`StrategyResult`, `CsvValidationResult`).

### B. Inheritance
- **Mapped Superclass Hierarchy**:
  - `BaseEntity` (`@MappedSuperclass`) provides unified identity (`Long id`), timestamp tracking (`createdAt`, `updatedAt`), and lifecycle interceptors (`@PrePersist`, `@PreUpdate`).
  - Inherited cleanly by `CrimeRecord`, `District`, `CrimeCategory`, `DatasetSource`, `DatasetImport`, and `User`.

### C. Abstraction
- **Repository Contracts**: Spring Data `CrimeRecordRepository` abstracts relational SQL access behind declarative type-safe query interfaces.
- **Analytical Strategy Abstraction**: `CrimeAnalyticsStrategy` interface specifies the contract `evaluate(records, params) -> StrategyResult`, decoupling mathematical evaluation from HTTP controllers.

### D. Polymorphism (The Strategy Pattern)
Four interchangeable analytical strategy implementations can be invoked at runtime or switched dynamically:
1. **`DistrictComparisonStrategy`**: Multi-district case count, clearance rate, and spatial differential calculations.
2. **`TrendForecastingStrategy`**: Multi-year moving averages and period-over-period percentage changes with zero-growth protection.
3. **`HotspotDensityStrategy`**: Computes spatial incident density per 100 square kilometers (`caseCount / areaSqKm * 100`) and categorizes threat tiers (`LOW`, `MODERATE`, `ELEVATED`, `HIGH`, `CRITICAL`).
4. **`CategoryDistributionStrategy`**: Computes proportional shares across crime categories and severity-weighted threat indexing (`SeverityLevel.weight`).

### E. Java File Handling (RFC 4180 CSV Engine)
- Implemented in `CrimeCsvHandler`:
  - **Finite State Machine**: Handles commas within quoted strings, escaped quotes (`""`), and multiline entries without naive `String.split(",")`.
  - **Byte Order Mark (BOM) Stripping**: Strips UTF-8 BOM (`\uFEFF`) to prevent header corruption.
  - **Malformed Row Isolation**: Identifies invalid numbers, invalid dates, unknown districts, or missing headers, capturing row number, raw text, and error reason in `MalformedRowRecord` without crashing the import.
  - **Duplicate Prevention**: Hashes composite fields using SHA-256 to reject duplicate submissions.

### F. Educational Direct JDBC Demonstration
- `EducationalJdbcCrimeDao` and desktop `DirectJdbcDemoRunner`:
  - Directly opens a raw `java.sql.Connection` from the pooled DataSource.
  - Employs parameterized `PreparedStatement` to demonstrate defense against SQL injection.
  - Navigates cursor-based `ResultSet` to construct data transfer structures.
  - Extracts database metadata (driver name, driver version, isolation level, execution time in milliseconds).

---

## 3. Technology Stack

| Layer | Technologies |
|:---|:---|
| **Frontend** | React 19, Vite, JavaScript (ES2024), React Router 6, Leaflet 1.9.4, React Leaflet 4.2.1, Recharts 2.13.0, Lucide React, Custom Cyber CSS |
| **Backend** | Java 21 LTS (Amazon Corretto), Spring Boot 3.3.5, Spring Data JPA, Hibernate 6, Spring Security 6, JJWT, Flyway 10, Maven 3.9.9 |
| **Desktop Client** | Java Swing, FlatLaf Dark Cyber Look-and-Feel, Java 21 `HttpClient`, Direct JDBC |
| **Database** | MySQL 8.x compatible, H2 in-memory MySQL compatibility mode (default), Flyway versioned migrations (V1, V2, V3) |
| **AI Integration** | Google Gemini 2.5 Flash API with bounded context grounding and deterministic rule-based fallback |
| **Geospatial & Street View** | Mapillary Graph API (street-level survey exploration, compass azimuths, 360 viewer deep-links), Tamil Nadu 38-District Polygon GeoJSON boundary dataset |

---

## 4. System Architecture & Modules

```
crimescope-ai/
├── backend/                               # Spring Boot 3.3.5 Java 21 Backend
│   ├── src/main/java/com/crimescope/
│   │   ├── config/                        # SecurityConfig, CorsConfig
│   │   ├── domain/
│   │   │   ├── enums/                     # SeverityLevel, ThreatLevel, VerificationStatus
│   │   │   ├── model/                     # BaseEntity, CrimeRecord, District, Category, User, Audit
│   │   │   └── strategy/                  # CrimeAnalyticsStrategy + 4 Polymorphic Implementations
│   │   ├── jdbc/                          # EducationalJdbcCrimeDao (Raw JDBC demonstration)
│   │   ├── repository/                    # Spring Data JPA Repositories
│   │   ├── service/                       # Business services & audit logging
│   │   │   ├── ai/                        # GeminiAnalysisService (Grounded AI + Fallback)
│   │   │   └── file/                      # CrimeCsvHandler (RFC 4180 CSV Engine)
│   │   └── web/                           # REST Controllers, DTOs, Exception Handlers
│   └── src/main/resources/
│       ├── application.properties         # Development H2 MySQL mode
│       ├── application-mysql.properties   # Production MySQL 8.x configuration
│       └── db/migration/                  # Flyway Migrations (V1 Schema, V2 Districts, V3 1611 Records)
│
├── frontend/                              # React 19 + Vite Frontend
│   ├── public/data/                       # tamil-nadu-districts.geojson (38-district polygon boundaries)
│   └── src/
│       ├── components/                    # TopNavigation, TamilNaduMap, LeftFilter, RightIntel, BottomTimeline
│       ├── pages/                         # CommandCenter, DistrictAnalytics, CrimeRecords, AiIntel, DataMgmt, SystemStatus
│       ├── context/                       # CrimeContext.jsx (Centralized state management)
│       └── services/                      # api.js (Axios REST client with fallback error handling)
│
├── desktop/                               # Standalone Java Swing Desktop Client
│   ├── src/main/java/com/crimescope/desktop/
│   │   ├── CrimeScopeDesktopApp.java      # FlatLaf Dark GUI with filter toolbar & record table
│   │   ├── CrimeScopeRestClient.java      # Java 21 HttpClient wrapper
│   │   └── DirectJdbcDemoRunner.java      # Academic Raw JDBC Dialog Runner
│   └── target/crimescope-desktop-1.0.0.jar# Shaded fat JAR (executable)
│
├── data/                                  # Reference datasets and sample CSV import files
├── docs/                                  # ARCHITECTURE.md, API_DOCUMENTATION.md, DATABASE_SCHEMA.md
├── pom.xml                                # Root Maven parent project
├── .env.example                           # Environment variable template
└── README.md                              # This file
```

---

## 5. Prerequisites & Environment Setup

### Required Runtimes
1. **Java Development Kit (JDK) 21 LTS** (Amazon Corretto, Eclipse Temurin, or Oracle JDK 21).
   - Verify: `java -version` (must report `openjdk version "21.x.x"`).
2. **Apache Maven 3.9+**.
   - Verify: `mvn -version`.
3. **Node.js 18+ & npm 9+**.
   - Verify: `node -v` and `npm -v`.

---

## 6. Build & Launch Instructions

### Step 1: Clone or Navigate to the Workspace
```bash
cd C:\Users\Bagyalakshmi\.gemini\antigravity\scratch\crimescope-ai
```

### Step 2: Configure API Keys Securely (.env)
Copy the template file to `.env`:
```bash
cp .env.example .env
```
Open `.env` in your text editor and paste your API keys:
```env
# Google Gemini API Key (https://aistudio.google.com/)
GEMINI_API_KEY=your_gemini_api_key_here

# Mapillary Client Token (https://www.mapillary.com/dashboard/developers)
MAPILLARY_CLIENT_TOKEN=your_mapillary_client_token_here
```
> [!NOTE]
> `.env` is ignored by `.gitignore` and is never committed to GitHub. You can test your keys at runtime using the **`KEYS`** button in the top navigation bar. If left blank, CrimeScope AI utilizes its built-in **Deterministic Statistical Synthesis Fallback** and cadastral spatial mapping.

### Step 3: Build the Entire Project (Backend + Desktop)
From the root directory:
```bash
mvn clean package
```
*This compiles both `backend` and `desktop`, runs all 15 unit tests, and packages executable JAR files.*

### Step 3: Run the Spring Boot Backend Server
```bash
java -jar backend/target/crimescope-backend-1.0.0.jar
```
*The backend starts on `http://localhost:8080`. Flyway automatically creates the database schema and seeds 1,611 historical records across all 38 districts.*

### Step 4: Run the React 19 Frontend
In a new terminal window:
```bash
cd frontend
npm install
npm run dev
```
*Open your browser and navigate to `http://localhost:5173`.*

### Step 5: (Optional) Run the Java Swing Desktop Client
In another terminal window:
```bash
java -jar desktop/target/crimescope-desktop-1.0.0.jar
```
*The dark cyber desktop window will appear, connecting to `http://localhost:8080` to fetch records, inspect dossiers, import/export CSV files, and run raw JDBC queries.*

---

## 7. Key Features Walkthrough

### 1. Command Center (`/`)
- **Interactive Tamil Nadu Geospatial Map**: Renders full district boundaries with GeoJSON polygons. Districts are color-coded into risk tiers (`LOW`, `MODERATE`, `ELEVATED`, `HIGH`, `CRITICAL`) using spatial density mathematics.
- **Interactive Selection**: Clicking any district focuses the camera, updates the intelligence dossier, and filters historical charts.
- **Left Filter Panel**: Filter by administrative zone (Chennai, North, Central, West, South), crime categories, and year ranges (2018–2023).
- **Right Intel Panel**: Displays district population, case counts, clearance rate, category breakdown donut chart, and Gemini observations.
- **Bottom Historical Timeline**: Area chart tracking multi-year trends and resolution rates.

### 2. District Analytics (`/district-analytics`)
- Multi-district comparative analysis powered by the polymorphic `DistrictComparisonStrategy`.
- Clearance efficiency comparison (`(chargesheetedCount / caseCount) * 100`).
- District ranking table sorted by raw volume or spatial density.

### 3. Crime Records Management (`/crime-records`)
- Paginated table of crime incidents with multi-column sorting and filtering.
- **Incident Dossier Modal**: Comprehensive view of an individual record with coordinates, reporting unit, and verification status.
- **Create Record Modal**: Add verified or demonstration records with validation.
- **Export to CSV**: Download filtered database records as an RFC 4180 CSV file.

### 4. AI Intelligence Console (`/ai-intelligence`)
- Powered by **Google Gemini 2.5 Flash** with **Java Grounding**:
  - The Java backend computes exact sums and clearance rates before prompting Gemini.
  - Strictly prevents hallucinated incidents, fake URLs, or synthetic coordinates.
  - **Deterministic Grounded Fallback Engine**: If `GEMINI_API_KEY` is not supplied, the system automatically uses Java statistical synthesis to generate comprehensive insights.
- Feature tabs:
  - *Strategic Assessment*: In-depth district overview.
  - *District Comparison*: Side-by-side comparative analysis.
  - *Historical Trajectories*: Analysis of period-over-period accelerations and shifts.
  - *Grounded Q&A*: Natural language query answering strictly citing database metrics.

### 5. Data Management & CSV File Handling (`/data-management`)
- **RFC 4180 Batch CSV Uploader**: Drag and drop CSV datasets.
- **Malformed Row Inspector**: Identifies invalid rows with exact line numbers and root cause descriptions.
- **CSV Template Download**: Download standard format template.
- **Dataset Provenance Ledger**: View source metadata, licensing (GODL India), and import logs.

### 6. System Status & Academic OOPS Inspector (`/system-status`)
- Real-time Spring Boot, JVM (Amazon Corretto 21), and Flyway status.
- **OOPS Design Pattern Visualizer**: Explains how Encapsulation, Inheritance, Abstraction, Polymorphism, and File Handling are realized in code.
- **Live Direct JDBC Demonstration Runner**: Execute parameterized JDBC queries directly against the database and view execution telemetry (connection isolation, metadata, query latency in milliseconds).
- **Audit Trail Viewer**: Paginated ledger of creates, updates, and deletes.

---

## 8. Automated Testing & Verification

15 automated unit and integration tests verify the core backend logic:

```bash
mvn test
```

### Test Suite Summary:
1. **`CrimeAnalyticsStrategyTest`** (5 tests):
   - Verifies `DistrictComparisonStrategy` multi-district aggregation.
   - Verifies `TrendForecastingStrategy` safe zero-denominator handling (`0 -> X` growth).
   - Verifies `HotspotDensityStrategy` per-100-sq-km spatial density and threat tiering.
   - Verifies `CategoryDistributionStrategy` percentage shares and severity weighting.
   - Verifies empty dataset resilience (no `NullPointerException` or `NaN`).
2. **`CrimeCsvHandlerTest`** (4 tests):
   - Verifies RFC 4180 compliance with commas inside quoted fields.
   - Verifies UTF-8 BOM stripping.
   - Verifies malformed row isolation and reporting without aborting valid rows.
   - Verifies CSV streaming generation.
3. **`CrimeRecordValidationTest`** (3 tests):
   - Verifies `calculateClearanceRate()` invariant protection (`caseCount == 0` returns `0.0`).
   - Verifies Bean Validation constraints (negative case counts rejected).
   - Verifies verification status transitions (`VERIFIED` vs `DEMONSTRATION`).
4. **`AuthSecurityTest`** (3 tests):
   - Verifies password hashing (BCrypt).
   - Verifies public access configuration for academic evaluation.
   - Verifies JWT token generation and validation.

---

## 9. Data Integrity & Ethical Disclaimers

> [!IMPORTANT]
> **Academic and Historical Scope**: CrimeScope AI contains historical crime statistics compiled from public sources (Tamil Nadu Police Department compendiums, National Crime Records Bureau reports, OpenCity portals) spanning 2018 to 2023.
> 
> - **Not Live Crime Monitoring**: This application analyzes historical reporting data and must never be used or presented as a live dispatch or real-time surveillance system.
> - **Registered Cases vs. Convictions**: The system rigorously distinguishes registered FIR cases from arrests, chargesheets, and judicial convictions. High registered case counts often reflect higher reporting accessibility rather than higher underlying criminality.
> - **No Hallucinated Incidents**: The application strictly prohibits AI hallucination of individual crimes, victims, coordinates, or official citations.

---

## 10. Documentation Index

- [System Architecture & OOPS Design Patterns](file:///C:/Users/Bagyalakshmi/.gemini/antigravity/scratch/crimescope-ai/docs/ARCHITECTURE.md)
- [REST API Reference & Endpoints](file:///C:/Users/Bagyalakshmi/.gemini/antigravity/scratch/crimescope-ai/docs/API_DOCUMENTATION.md)
- [Database Schema & Data Dictionary](file:///C:/Users/Bagyalakshmi/.gemini/antigravity/scratch/crimescope-ai/docs/DATABASE_SCHEMA.md)

---

## 11. Authors & License

Developed as an **Academic OOPS Microproject** demonstrating enterprise Java full-stack engineering, geospatial mapping, and artificial intelligence integration.

Licensed under the **MIT License**. Data references subject to the **Government Open Data License - India (GODL)**.
