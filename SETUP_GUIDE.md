# PEPs - Backend/Frontend Connection Setup Guide

## Update Overview

The application now fully connects the Angular frontend with the Java Spring backend to fetch and update real data from PostgreSQL database.

### Changes Made:

#### 1. Backend Changes (Java Spring)

**File: `web.xml`**
- Changed servlet mapping from `*.do` to `/` to handle REST endpoints

**File: `DashBoardController.java`**
- Fetches dashboard statistics from database

**New Controllers:**
- `InteractionController.java` - GET `/interactions` returns all interactions with module names
- `ModuleController.java` - GET/PUT `/modules` manages module data and configuration
- `SoundController.java` - GET `/sounds` returns all sounds from database
- `DailyStatsController.java` - GET `/daily-stats` returns hourly interaction counts for today

**DTOs Created:**
- `DashboardStats.java` - Dashboard statistics
- `InteractionDTO.java` - Interaction data transfer
- `ModuleDTO.java`, `ModuleConfigDTO.java` - Module data and configuration
- `SoundDTO.java` - Sound data
- `DailyDataDTO.java` - Daily statistics

#### 2. Frontend Changes (Angular)

**File: `app.ts`**
- Added BASE_URL constant for API endpoint
- Removed all mock/static data
- All data is now fetched from backend API
- Implemented date filtering that works with current dates (not hardcoded)
- Module configuration now saves to backend via PUT request
- Added sounds signal to store and display database sounds
- Refresh button now reloads all data from backend

**File: `app.html`**
- Fixed last interaction display to show full date and time
- Changed dailyChartData to use signal
- Updated sounds page to display all sounds from database

**File: `app.css`**
- Added styling for sounds grid display

---

## Setup Instructions

### Prerequisites
1. PostgreSQL installed and running on localhost:5432
2. Java 8+ and Maven installed
3. Node.js and npm installed
4. Application server (like Apache Tomcat or GlassFish)

### Step 1: Setup Database

1. Start PostgreSQL
2. Connect to PostgreSQL (default database: postgres, user: postgres, password: postgres)
3. Create tables by running:
   ```bash
   psql -U postgres -d postgres -f "sql/requete creation tables.sql"
   ```
4. Insert test data:
   ```bash
   psql -U postgres -d postgres -f "sql/Creation données test.sql"
   ```

**Note:** If PostgreSQL credentials are different, update them in:
`back/PEPs_back/src/main/resources/META-INF/persistence.xml`

### Step 2: Build and Deploy Backend

**IMPORTANT:** Stop your running Tomcat/GlassFish server first!

Run the Peps_back project in Netbeans, or via command line:

1. Navigate to backend directory:
   ```bash
   cd back\PEPs_back
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Deploy the generated WAR file:
   - Find the WAR file in `target/PEPs_back-0.1.war`
   - Deploy it to your application server (Tomcat/GlassFish)
   - Make sure it's deployed at context path `/PEPs_back`

4. Verify backend is running:
   - Dashboard: `http://localhost:8080/PEPs_back/dashboard`
   - Should return JSON like: `{"totalInteractions":4,"activeModules":2,"lastInteraction":"2025-01-18 10:30:45"}`
   - Interactions: `http://localhost:8080/PEPs_back/interactions`
   - Modules: `http://localhost:8080/PEPs_back/modules`
   - Sounds: `http://localhost:8080/PEPs_back/sounds`
   - Daily Stats: `http://localhost:8080/PEPs_back/daily-stats`

### Step 3: Run Frontend

1. Navigate to frontend directory:
   ```bash
   cd front\pepsfront
   ```

2. Install dependencies (if not already done):
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

4. Open browser: `http://localhost:4200`

### Step 4: Test the Connection

1. Login with password: `admin`
2. The application will fetch all data from the database:
   - **Dashboard**: Shows real interaction counts, active modules, and last interaction timestamp
   - **Daily Chart**: Shows hourly interaction counts for today
   - **Interactions Page**: Shows all interactions from database
     - Filters work with current date (not hardcoded)
     - "Aujourd'hui" shows today's interactions
     - "Hier" shows yesterday's interactions
     - "Semaine" shows last 7 days
   - **Modules Page**: Shows all modules from database with their configuration
     - Click on a module to edit its configuration
     - Changes are saved to database
   - **Sons Page**: Shows all sounds from database

---

## API Endpoints

Currently implemented:

- **GET** `/dashboard` - Returns dashboard statistics
  ```json
  {
    "totalInteractions": 4,
    "activeModules": 2,
    "lastInteraction": "2025-01-18 10:30:45"
  }
  ```

- **GET** `/interactions` - Returns list of all interactions
  ```json
  [
    {
      "id": 1,
      "date": "2025-01-18T10:25:00",
      "module": "Module Perchoir 1",
      "type": "Bec"
    }
  ]
  ```

- **GET** `/modules` - Returns list of all modules
  ```json
  [
    {
      "id": 1,
      "name": "Module Perchoir 1",
      "location": "",
      "status": "Actif",
      "ip": "192.168.1.10",
      "config": {
        "volume": 80,
        "mode": "Automatique",
        "actif": true,
        "son": false
      }
    }
  ]
  ```

- **GET** `/modules/{id}` - Returns specific module details

- **PUT** `/modules/{id}` - Updates module configuration
  
- **GET** `/sounds` - Returns list of all sounds
  ```json
  [
    {
      "id": 1,
      "name": "Cri Ara Bleu",
      "type": "Vocal"
    }
  ]
  ```

- **GET** `/daily-stats` - Returns hourly statistics for today
  ```json
  [
    {"time": "8h", "count": 0},
    {"time": "10h", "count": 2},
    {"time": "12h", "count": 1}
  ]
  ```

---

## Troubleshooting

### Backend Issues

**Problem: Cannot connect to database**
- Check PostgreSQL is running: `pg_ctl status`
- Verify credentials in `persistence.xml`
- Check database exists: `psql -U postgres -l`

**Problem: 404 error on endpoints**
- Verify WAR is deployed correctly
- Check application server logs
- Ensure context path is `/PEPs_back`
- Verify web.xml servlet mapping is set to `/` not `*.do`

**Problem: Backend returns null or errors**
- Check application server logs
- Verify tables exist: `psql -U postgres -d postgres -c "\dt"`
- Verify data exists: `psql -U postgres -d postgres -c "SELECT COUNT(*) FROM interaction;"`

### Frontend Issues

**Problem: CORS errors**
- Backend already has `@CrossOrigin(origins = "http://localhost:4200")`
- If using different port, update the annotation in all controllers

**Problem: "Erreur de connexion" message**
- Check backend is running and accessible
- Open browser console (F12) to see detailed error
- Verify URL is correct: `http://localhost:8080/PEPs_back/`

**Problem: Still showing old data or empty data**
- Clear browser cache
- Hard refresh: Ctrl+Shift+R
- Check browser console for HTTP errors
- Verify backend endpoints return data (test in browser or with curl)

**Problem: Date filters not working correctly**
- Filters now use current date, not hardcoded dates
- Verify backend is returning dates in correct format
- Check browser console for date parsing errors

---

## Database Schema

**Module Table:**
- idmodule (PK)
- nom (name)
- ip_adress
- status
- volume
- current_mode (Manual/Automatic)
- actif (active boolean)
- last_seen (timestamp)

**Interaction Table:**
- idinteraction (PK)
- idsound (FK)
- idmodule (FK)
- typeInteraction (Bec/Patte)
- time_lancement (timestamp)

**Sound Table:**
- idsound (PK)
- nom (name)
- type_son (type)

---

## Contact & Support

If you encounter issues:
1. Check application server logs
2. Check PostgreSQL logs
3. Check browser console (F12)
4. Verify all services are running on correct ports
5. Test each endpoint individually with curl or browser
