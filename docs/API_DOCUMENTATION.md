# CrimeScope AI — REST API Documentation
## Tamil Nadu Crime Intelligence Command Center

Base URL: `http://localhost:8080`  
Protocol: `HTTP/1.1`  
Data Format: `application/json` (UTF-8) / `text/csv` (for CSV exports/templates)

---

## Table of Contents
1. [Overview & Standards](#1-overview--standards)
2. [Authentication & Session](#2-authentication--session)
3. [Districts API](#3-districts-api)
4. [Crime Categories API](#4-crime-categories-api)
5. [Crime Records API](#5-crime-records-api)
6. [Analytics & Polymorphic Strategies API](#6-analytics--polymorphic-strategies-api)
7. [Gemini Grounded AI Intelligence API](#7-gemini-grounded-ai-intelligence-api)
8. [Data Management & CSV File Handling API](#8-data-management--csv-file-handling-api)
9. [System Telemetry & JDBC Demonstration API](#9-system-telemetry--jdbc-demonstration-api)
10. [Error Handling & Status Codes](#10-error-handling--status-codes)

---

## 1. Overview & Standards

All endpoints adhere to REST conventions:
- Successful retrievals return `200 OK`.
- Successful creations return `201 Created`.
- Successful deletions return `204 No Content`.
- Client errors return `400 Bad Request` or `404 Not Found` with structured error messages.
- Server errors return `500 Internal Server Error`.
- Zero-division errors and mathematical `NaN` are defensively guarded across all analytics computations.

---

## 2. Authentication & Session

### `POST /api/auth/login`
Authenticates a user and generates a signed JWT token.
*(Note: In non-auth mode, all endpoints are publicly accessible for immediate academic evaluation).*

**Request Body:**
```json
{
  "username": "admin",
  "password": "Password@123"
}
```

**Response (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "fullName": "System Administrator",
  "roles": ["ROLE_ADMIN", "ROLE_ANALYST"]
}
```

### `GET /api/auth/status`
Returns the authentication state and active security profile.

---

## 3. Districts API

### `GET /api/districts`
Retrieves all 38 districts of Tamil Nadu, optionally filtered by administrative zone.

**Query Parameters:**
- `zone` *(optional, string)*: Filter by zone (`NORTH`, `CENTRAL`, `WEST`, `SOUTH`, `CHENNAI`).

**Response (`200 OK`):**
```json
[
  {
    "id": 1,
    "code": "CHN",
    "name": "Chennai",
    "headquarters": "Chennai",
    "zone": "CHENNAI",
    "areaSqKm": 426.00,
    "population2021Est": 7100000,
    "centerLat": 13.082700,
    "centerLng": 80.270700,
    "createdAt": "2026-10-25T14:25:00",
    "updatedAt": "2026-10-25T14:25:00"
  },
  {
    "id": 2,
    "code": "CBE",
    "name": "Coimbatore",
    "headquarters": "Coimbatore",
    "zone": "WEST",
    "areaSqKm": 4723.00,
    "population2021Est": 3458000,
    "centerLat": 11.016800,
    "centerLng": 76.955800,
    "createdAt": "2026-10-25T14:25:00",
    "updatedAt": "2026-10-25T14:25:00"
  }
]
```

### `GET /api/districts/{id}`
Retrieves a single district by its unique primary key ID.

### `GET /api/districts/code/{code}`
Retrieves a district by its official 3-letter code (e.g., `MDU`, `TRY`, `SAL`).

---

## 4. Crime Categories API

### `GET /api/crime-categories`
Retrieves all 7 standardized IPC and statutory crime categories with severity levels.

**Response (`200 OK`):**
```json
[
  {
    "id": 1,
    "code": "VIOLENT",
    "name": "Violent Crimes Against Body",
    "description": "Murder, Culpable Homicide, Grievous Hurt, Kidnapping (IPC 302, 307, 326)",
    "severityLevel": "HIGH",
    "isIpc": true
  },
  {
    "id": 2,
    "code": "PROPERTY",
    "name": "Crimes Against Property",
    "description": "Dacoity, Robbery, Burglary, Theft (IPC 392, 395, 379)",
    "severityLevel": "MEDIUM",
    "isIpc": true
  },
  {
    "id": 3,
    "code": "WOMEN_CHILDREN",
    "name": "Crimes Against Women & Children",
    "description": "Cruelty by Husband/Relatives, Dowry Deaths, POCSO, Assault (IPC 498A, POCSO Act)",
    "severityLevel": "CRITICAL",
    "isIpc": true
  },
  {
    "id": 4,
    "code": "CYBER",
    "name": "Cyber Crimes",
    "description": "Online Financial Fraud, Identity Theft, Phishing, IT Act Violations",
    "severityLevel": "MEDIUM",
    "isIpc": false
  },
  {
    "id": 5,
    "code": "ECONOMIC",
    "name": "Economic & White-Collar Offences",
    "description": "Cheating, Forgery, Criminal Breach of Trust, Counterfeiting (IPC 420, 406)",
    "severityLevel": "MEDIUM",
    "isIpc": true
  },
  {
    "id": 6,
    "code": "SLL_SPECIAL",
    "name": "Special & Local Laws (SLL)",
    "description": "NDPS Act, Arms Act, Explosives Act, Tamil Nadu Prohibition Act",
    "severityLevel": "HIGH",
    "isIpc": false
  },
  {
    "id": 7,
    "code": "PUBLIC_ORDER",
    "name": "Public Order & Riot Offences",
    "description": "Rioting, Unlawful Assembly, Affray (IPC 147, 148, 160)",
    "severityLevel": "LOW",
    "isIpc": true
  }
]
```

---

## 5. Crime Records API

### `GET /api/crime-records`
Lists and paginates crime records with multi-criteria filtering and sorting.

**Query Parameters:**
- `districtId` *(optional, Long)*: Filter by district primary key.
- `categoryId` *(optional, Long)*: Filter by crime category primary key.
- `startYear` *(optional, Integer)*: Filter records from year (inclusive, e.g. 2018).
- `endYear` *(optional, Integer)*: Filter records to year (inclusive, e.g. 2023).
- `includeDemo` *(optional, Boolean, default: false)*: Whether to include demonstration sandbox records.
- `page` *(optional, Integer, default: 0)*: Zero-based page index.
- `size` *(optional, Integer, default: 20)*: Page size (records per page).
- `sortBy` *(optional, String, default: reportingYear)*: Sort property.
- `direction` *(optional, String, default: desc)*: Sort direction (`asc` or `desc`).

**Response (`200 OK`):**
```json
{
  "content": [
    {
      "id": 1420,
      "district": { "id": 1, "code": "CHN", "name": "Chennai" },
      "category": { "id": 4, "code": "CYBER", "name": "Cyber Crimes" },
      "reportingYear": 2023,
      "reportingMonth": 11,
      "caseCount": 142,
      "arrestCount": 89,
      "convictedCount": 23,
      "chargesheetedCount": 112,
      "isDemonstration": false,
      "verificationStatus": "VERIFIED",
      "reportingUnit": "District Cyber Crime Police Station",
      "latitude": 13.082700,
      "longitude": 80.270700,
      "locationDescription": "Chennai Metro Cyber Command",
      "sourceReference": "TN Police Crime Review Compendium 2023",
      "dataHash": "4a7c2f8b...",
      "createdAt": "2026-10-25T14:27:00",
      "updatedAt": "2026-10-25T14:27:00"
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 1611,
  "totalPages": 81,
  "last": false
}
```

### `GET /api/crime-records/{id}`
Retrieves a specific crime record dossier by ID.

### `POST /api/crime-records`
Creates a new verified or demonstration crime record with audit logging.

**Request Body (`CrimeRecordDto`):**
```json
{
  "districtId": 1,
  "categoryId": 4,
  "reportingYear": 2023,
  "reportingMonth": 12,
  "caseCount": 156,
  "arrestCount": 94,
  "convictedCount": 28,
  "chargesheetedCount": 125,
  "isDemonstration": false,
  "verificationStatus": "VERIFIED",
  "reportingUnit": "Cyber Crime Cell",
  "locationDescription": "Mount Road Cyber Command",
  "latitude": 13.0827,
  "longitude": 80.2707,
  "sourceReference": "Manual Entry Verification"
}
```

**Response (`201 Created`):** Returns the persisted `CrimeRecord` entity.

### `PUT /api/crime-records/{id}`
Updates an existing crime record with audit logging and recalculation of hash signatures.

### `DELETE /api/crime-records/{id}`
Deletes an incident record with full audit trail logging. Returns `204 No Content`.

---

## 6. Analytics & Polymorphic Strategies API

### `GET /api/analytics/overview`
Retrieves consolidated intelligence metrics across Tamil Nadu for the geospatial Command Center:
- Total registered cases, arrests, convictions, and chargesheets.
- Overall state-wide clearance rate percentage (`(chargesheetedCount / caseCount) * 100`).
- Category-wise distribution breakdown.
- Multi-year trend summary.
- District hotspot map data with spatial incident densities per 100 sq km and calculated threat levels (`LOW`, `MODERATE`, `ELEVATED`, `HIGH`, `CRITICAL`).

**Query Parameters:**
- `includeDemo` *(optional, Boolean, default: false)*

### `GET /api/analytics/district-compare`
Executes `DistrictComparisonStrategy` dynamically to compare multiple districts.

**Query Parameters:**
- `districtIds` *(optional, List<Long>)*: List of district IDs to compare (e.g. `1,2,3`).
- `year` *(optional, Integer)*: Evaluation year (e.g. 2023).
- `includeDemo` *(optional, Boolean, default: false)*

**Response (`200 OK` - `StrategyResult`):**
```json
{
  "strategyIdentifier": "DISTRICT_COMPARISON_STRATEGY",
  "title": "Multi-District Comparative Analytics",
  "computedAt": "2026-10-25T15:20:00",
  "metrics": {
    "districtCount": 2,
    "evaluatedYear": 2023
  },
  "seriesData": [
    {
      "districtCode": "CHN",
      "districtName": "Chennai",
      "totalCases": 18450,
      "totalArrests": 14200,
      "clearanceRate": 78.4,
      "densityPer100SqKm": 4330.98
    },
    {
      "districtCode": "CBE",
      "districtName": "Coimbatore",
      "totalCases": 9820,
      "totalArrests": 7950,
      "clearanceRate": 82.1,
      "densityPer100SqKm": 207.91
    }
  ],
  "observations": [
    "Chennai exhibits highest raw density due to urban concentration.",
    "Coimbatore demonstrates superior clearance efficiency at 82.1%."
  ]
}
```

### `GET /api/analytics/trends`
Executes `TrendForecastingStrategy` to compute period-over-period growth rates with zero-denominator safety guards.

### `GET /api/analytics/categories`
Executes `CategoryDistributionStrategy` to calculate category proportions and severity-weighted threat indices.

### `GET /api/analytics/strategy/{strategyId}`
Executes any arbitrary polymorphic analytics strategy by identifier:
- `DISTRICT_COMPARISON`
- `TREND_FORECASTING`
- `HOTSPOT_DENSITY`
- `CATEGORY_DISTRIBUTION`

---

## 7. Gemini Grounded AI Intelligence API

Every AI endpoint operates under **strict factual grounding**:
1. Java backend calculates exact numerical sums, category distributions, and percentage changes from the verified SQL records.
2. The bounded statistical context is submitted to Google Gemini (`gemini-2.5-flash`).
3. If `GEMINI_API_KEY` is not present, the system automatically runs the **Deterministic Grounded Fallback Engine**, guaranteeing 100% availability without network dependency.

### `GET /api/ai/status`
Returns real-time diagnostics of the Google Gemini API key configuration, active model, authorization test status, and masked key representation.

### `POST /api/ai/validate`
Tests an operator-supplied candidate Gemini API key against Google Generative Language endpoints.

**Request Body:**
```json
{
  "key": "AIzaSyYourCandidateKeyHere"
}
```

### `POST /api/ai/insights`
Generates comprehensive AI strategic insights for a specified district or state-wide.

**Request Body:**
```json
{
  "districtId": 1,
  "startYear": 2018,
  "endYear": 2023
}
```

**Response (`200 OK`):**
```json
{
  "status": "SUCCESS",
  "isAiGenerated": true,
  "modelUsed": "gemini-2.5-flash",
  "analysis": "Based on historical statistics for Chennai (2018–2023), total registered cases stand at 94,520 with an overall case clearance rate of 76.8%. Violent offences represent 14.2% of recorded crime, while Property and Cyber crimes constitute 38.5% and 18.2% respectively...",
  "sourceMetadata": "Tamil Nadu Police Crime Review Compendium 2023",
  "generatedAt": "2026-10-25T15:20:00"
}
```

### `POST /api/ai/compare`
Produces a grounded comparative assessment between two districts.

**Request Body:**
```json
{
  "districtCodeA": "CHN",
  "districtCodeB": "MDU",
  "year": 2023
}
```

### `POST /api/ai/trends`
Explains historical trajectories, acceleration points, and clearance rate shifts across years.

### `POST /api/ai/ask`
Interprets arbitrary natural language questions strictly grounded in the database statistics.

**Request Body:**
```json
{
  "question": "Which crime category experienced the highest growth rate between 2020 and 2023?",
  "districtId": 1,
  "year": 2023
}
```

---

## 8. Mapillary Street-Level Imagery & Exploration API

Provides authentic street-level photographic survey exploration, sequence inspection, compass bearing indicators, and geographic vector coverage across Tamil Nadu districts.

### `GET /api/mapillary/status`
Checks the active Mapillary token configuration, tests connectivity against Mapillary Graph API, and returns status:
```json
{
  "configured": true,
  "valid": true,
  "status": "OPERATIONAL",
  "message": "Mapillary client token verified. Street-level imagery and vector layers active.",
  "tokenMasked": "MLY|987...****"
}
```

### `GET /api/mapillary/config`
Retrieves public client token configuration for frontend vector tile layers without exposing backend secrets.

### `GET /api/mapillary/imagery-near`
Retrieves authentic street-level photographic surveys from Mapillary Graph API within proximity of requested geographic coordinates.

**Query Parameters:**
- `lat` *(required, Double)*: Latitude
- `lng` *(required, Double)*: Longitude
- `radius` *(optional, Integer, default: 1000)*: Search radius in meters

**Response (`200 OK`):**
```json
{
  "status": "SUCCESS",
  "requestedCoordinates": { "latitude": 13.0827, "longitude": 80.2707 },
  "count": 4,
  "images": [
    {
      "id": "184729384729",
      "thumbUrl": "https://images.mapillary.com/...",
      "thumbSmallUrl": "https://images.mapillary.com/...",
      "capturedAt": "2023-08-14T06:30:00Z",
      "compassAngle": 182.4,
      "latitude": 13.0825,
      "longitude": 80.2709,
      "distanceMeters": 32,
      "viewerUrl": "https://www.mapillary.com/app/?pKey=184729384729&focus=photo"
    }
  ]
}
```

### `GET /api/mapillary/district-coverage`
Retrieves street survey points near a district administrative headquarters.

**Query Parameters:**
- `districtCode` *(required, String, e.g. `CHN`, `CBE`, `MDU`)*

### `POST /api/mapillary/validate`
Tests an operator-supplied candidate Mapillary access token.

---

## 9. Data Management & CSV File Handling API

### `POST /api/data/import/csv`
Uploads and validates a CSV dataset conforming to RFC 4180 standards.

**Request:** `multipart/form-data`
- `file`: CSV file part
- `sourceId` *(optional, Long)*: Associated dataset source ID

**Response (`200 OK` - `ImportSummaryDto`):**
```json
{
  "importId": 4,
  "fileName": "crimescope_sample_import.csv",
  "totalRowsRead": 25,
  "validRecordsImported": 25,
  "malformedRowsCount": 0,
  "duplicateRowsSkipped": 0,
  "status": "COMPLETED",
  "executionTimeMs": 142,
  "malformedRows": [],
  "logSummary": "Batch import completed successfully in 142ms. 25 valid records imported."
}
```

### `GET /api/data/export/csv`
Exports filtered records as an RFC 4180 compliant CSV stream with UTF-8 encoding.

**Query Parameters:**
- `districtId`, `categoryId`, `startYear`, `endYear`, `includeDemo`

**Response (`200 OK`):**
- Content-Type: `text/csv; charset=UTF-8`
- Header: `Content-Disposition: attachment; filename="crimescope_records_export.csv"`

### `GET /api/data/template`
Downloads an empty RFC 4180 CSV template with column headers and sample format.

### `GET /api/data/sources`
Retrieves registered dataset sources, licensing, and provenance metadata.

### `GET /api/data/imports`
Retrieves recent batch import audit records and execution logs.

---

## 9. System Telemetry & JDBC Demonstration API

### `GET /api/system/status`
Returns real-time server telemetry, database metadata, Corretto Java version, and Gemini model status.

**Response (`200 OK`):**
```json
{
  "systemName": "CrimeScope AI - Tamil Nadu Intelligence Command Center",
  "status": "OPERATIONAL_NOMINAL",
  "databaseProductName": "H2 (MySQL Compatibility Mode)",
  "databaseVersion": "2.2.224",
  "activeProfile": "[]",
  "javaVersion": "21.0.13 (Amazon.com Inc.)",
  "totalCrimeRecords": 1611,
  "totalDistrictsCount": 38,
  "geminiModel": "gemini-2.5-flash",
  "geminiKeyConfigured": true,
  "serverTime": "2026-10-25T15:22:18"
}
```

### `GET /api/system/jdbc-demo`
Executes raw direct JDBC queries via `EducationalJdbcCrimeDao` to demonstrate low-level `java.sql.Connection`, `PreparedStatement`, and `ResultSet` execution.

**Query Parameters:**
- `districtCode` *(optional, default: CHN)*
- `year` *(optional, default: 2023)*

**Response (`200 OK`):**
```json
{
  "academicPurpose": "Demonstration of raw java.sql.Connection, PreparedStatement, and ResultSet without ORM abstraction",
  "executedSql": "SELECT r.id, d.name AS district_name, d.code AS district_code, c.name AS category_name, r.reporting_year, r.case_count, r.chargesheeted_count, r.arrest_count, r.convicted_count FROM crime_records r JOIN districts d ON r.district_id = d.id JOIN crime_categories c ON r.category_id = c.id WHERE d.code = ? AND r.reporting_year = ? ORDER BY r.case_count DESC",
  "driverName": "H2 JDBC Driver",
  "driverVersion": "2.2.224",
  "transactionIsolation": "TRANSACTION_READ_COMMITTED",
  "autoCommit": true,
  "rowCount": 7,
  "executionTimeMs": 14,
  "records": [ ... ]
}
```

### `GET /api/system/audit-logs`
Paginates system audit logs tracking creates, updates, deletes, and CSV batch imports.

---

## 10. Error Handling & Status Codes

All errors return a structured JSON response:
```json
{
  "timestamp": "2026-10-25T15:22:18",
  "status": 404,
  "error": "Not Found",
  "message": "District ID 999 not found.",
  "path": "/api/districts/999"
}
```

| HTTP Code | Description | Scenario |
|:---|:---|:---|
| `200 OK` | Request succeeded | Standard data retrieval or update |
| `201 Created` | Resource created | Manual record creation |
| `204 No Content` | Resource deleted | Record deletion |
| `400 Bad Request` | Validation failure | Missing mandatory CSV columns or malformed JSON |
| `404 Not Found` | Resource missing | District, category, or record ID does not exist |
| `500 Server Error` | Internal exception | Unhandled database or operational error |
