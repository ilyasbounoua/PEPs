# PEPs Application - Monitoring & Management System

**Version:** 1.1 (Nov 2025)

PEPs is a web-based monitoring system for intelligent modules, featuring real-time statistics, historical data analysis, and audio management.

## 🛠 Technology Stack
* **Backend:** Java 8+, Spring Boot/Framework, Apache Tomcat 9+
* **Frontend:** Angular 17+, Angular Material
* **Database:** PostgreSQL 12+

---

## 🚀 Quick Start Guide

### 1. Database Setup
```bash
# 1. Start PostgreSQL
pg_ctl start

# 2. Create Tables
psql -U postgres -d postgres -f "sql/requete creation tables.sql"

# 3. Insert Test Data
psql -U postgres -d postgres -f "sql/Creation données test.sql"
```

### 2. Backend Setup (Java)
1.  Navigate to `back/PEPs_back`.
2.  Build: `mvn clean install`.
3.  Deploy `target/PEPs_back-0.1.war` to your Tomcat `webapps` folder.
4.  **Context Path:** Ensure the app is deployed at `/PEPs_back`.

### 3. Frontend Setup (Angular)
```bash
cd front/pepsfront
npm install
npm start
# Access at: http://localhost:4200
```

**Default Credentials:**
* **Password:** `admin`

---

## ⚙️ Configuration (Crucial)

### Audio Storage Paths
By default, audio files upload to the Tomcat bin directory. To fix this, **you must set an Environment Variable** pointing to your project source folder.

**Method A: Windows System Variable (Recommended)**
1.  Search "Environment Variables" in Windows.
2.  Create a new **System Variable**:
    * **Name:** `PEPS_AUDIO_DIR`
    * **Value:** `D:\Path\To\Your\Project\PEPs\back\PEPs_back\sons` (Adjust to your actual path)
3.  Restart Tomcat/NetBeans.

**Method B: Tomcat `catalina.bat`**
Add this line to `bin/catalina.bat`:
`set PEPS_AUDIO_DIR=D:\Path\To\Your\Project\PEPs\back\PEPs_back\sons`

---

## 📡 API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/dashboard` | Global statistics (interactions, active modules). |
| **GET** | `/interactions` | Full history of module interactions. |
| **GET** | `/modules` | List all modules and their statuses. |
| **PUT** | `/modules/{id}` | Update config (Volume, IP, Mode). |
| **GET** | `/sounds` | List available audio files. |
| **POST** | `/sounds` | Upload new audio (Multipart: mp3, wav, ogg). |
| **DELETE** | `/sounds/{id}` | Delete audio file and DB record. |

---

## ❓ Troubleshooting

* **CORS Errors:** Ensure Frontend runs on port `4200`.
* **Upload Failed:** Check if the `sons/` directory exists and has write permissions.
* **404 Errors:** Verify Tomcat deployed the WAR to `/PEPs_back`.
* **DB Connection:** Check `persistence.xml` if your Postgres password is not default.