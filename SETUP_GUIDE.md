# PEPs - Backend/Frontend Connection Setup Guide

## Update Overview

We updated application to connect the Angular frontend with the Java Spring backend to fetch real data from PostgreSQL database instead of using mock data.

### Changes Made:

#### 1. Backend Changes (Java Spring)

**File: `back/PEPs_back/src/main/java/peps/peps_back/controllers/DashBoardController.java`**
- Updated to fetch real data from the database using JPA repositories
- Now returns JSON format with proper structure: `{ totalInteractions, activeModules, lastInteraction }`
- Counts total interactions from the database
- Counts active modules (where `actif` is true)
- Gets the most recent interaction timestamp

**File: `back/PEPs_back/src/main/java/peps/peps_back/controllers/DashboardStats.java`** (NEW)
- Created DTO class to structure the dashboard response data
- Matches the `StatCard` interface expected by Angular frontend

**File: `back/PEPs_back/pom.xml`**
- Added Jackson dependency for JSON serialization: `jackson-databind` version 2.13.4.2
- This allows Spring to automatically convert Java objects to JSON

#### 2. Frontend Changes (Angular)

**File: `front/pepsfront/src/app/app.config.ts`**
- Added `provideHttpClient(withFetch())` to enable HTTP requests in the Angular app
- This is required for Angular 20+ standalone applications

**File: `front/pepsfront/src/app/app.ts`**
- Already had the HTTP call implemented (no changes needed)
- Calls `http://localhost:8080/PEPs_back/dashboard` when logged in
- Displays loading state and handles errors

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
We added persisistence.xml to .gitignore to avoid committing sensitive info.

### Step 2: Build and Deploy Backend

Run the Peps_baxk project in Netbeans, or via command line:

1. Navigate to backend directory:
   ```bash
   cd back\PEPs_back
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

**Note:** For command line deploy the generated WAR file:
   - Find the WAR file in `target/PEPs_back-0.1.war`
   - Deploy it to your application server (Tomcat/GlassFish)
   - Make sure it's deployed at context path `/PEPs_back`
   - Verify backend is running: `http://localhost:8080/PEPs_back/dashboard`
   - You should see JSON like: `{"totalInteractions":4,"activeModules":2,"lastInteraction":"2025-01-18 10:30:45"}`

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

1. Login with password: `admin` (SHA-256 hash is already configured)
2. You should see the Dashboard with real data from the database:
   - **Total Interactions**: Count from database
   - **Active Modules**: Count of modules where actif=true
   - **Last Interaction**: Timestamp of most recent interaction
3. Click the refresh button to reload data from the backend

---

## Troubleshooting

### Backend Issues

**Problem: Cannot connect to database**
- Check PostgreSQL is running: `pg_ctl status`
- Verify credentials in `persistence.xml`
- Check database exists: `psql -U postgres -l`

**Problem: 404 error on /dashboard endpoint**
- Verify WAR is deployed correctly
- Check application server logs
- Ensure context path is `/PEPs_back`

**Problem: Backend returns null or errors**
- Check application server logs
- Verify tables exist: `psql -U postgres -d postgres -c "\dt"`
- Verify data exists: `psql -U postgres -d postgres -c "SELECT COUNT(*) FROM interaction;"`

### Frontend Issues

**Problem: CORS errors**
- Backend already has `@CrossOrigin(origins = "http://localhost:4200")`
- If using different port, update the annotation in `DashBoardController.java`

**Problem: "Erreur de connexion" message**
- Check backend is running and accessible
- Open browser console (F12) to see detailed error
- Verify URL is correct: `http://localhost:8080/PEPs_back/dashboard`

**Problem: Still showing mock data**
- Clear browser cache
- Hard refresh: Ctrl+Shift+R
- Check browser console for HTTP errors

---

## API Endpoints

Currently implemented:
- **GET** `/dashboard` - Returns dashboard statistics

Expected Response:
```json
{
  "totalInteractions": 4,
  "activeModules": 2,
  "lastInteraction": "2025-01-18 10:30:45"
}
```

---

## Next Steps (Optional Enhancements)

To fetch more real data instead of mock data, you could add these endpoints:

1. **GET** `/interactions` - Return list of interactions
2. **GET** `/interactions?filter=today` - Filter interactions by date
3. **GET** `/modules` - Return list of modules
4. **GET** `/modules/{id}` - Get specific module details
5. **PUT** `/modules/{id}` - Update module configuration

These would replace the mock data in `app.ts` for interactions and modules.

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
