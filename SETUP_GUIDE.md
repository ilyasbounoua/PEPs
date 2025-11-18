# PEPs - Backend/Frontend Connection Setup Guide

## Application Overview

This application connects an Angular frontend with a Java Spring backend to fetch and update real data from a PostgreSQL database.

### Application Architecture:

#### Backend (Java Spring)

The backend provides REST API endpoints for data access and manipulation:

**Controllers:**
- `DashBoardController.java` - Provides dashboard statistics
- `InteractionController.java` - Manages interaction history
- `ModuleController.java` - Manages module data and configuration
- `SoundController.java` - Manages sound library
- `DailyStatsController.java` - Provides hourly statistics

**Data Transfer Objects:**
- `DashboardStats.java` - Dashboard statistics structure
- `InteractionDTO.java` - Interaction data structure
- `ModuleDTO.java`, `ModuleConfigDTO.java` - Module data structure
- `SoundDTO.java` - Sound data structure
- `DailyDataDTO.java` - Daily statistics structure

**Configuration:**
- `web.xml` - Servlet mapping configured to handle REST endpoints at root path

#### Frontend (Angular)

The frontend is a single-page application with the following features:

**Main Components:**
- Dashboard with real-time statistics
- Interaction history with date filtering
- Module management with configuration
- Sound library display

**Features:**
- Real-time data synchronization with backend
- Dynamic date filtering for current time periods
- Module configuration persistence
- CSV export functionality

---

## System Requirements

- PostgreSQL 12 or higher
- Java JDK 8 or higher
- Apache Maven 3.6 or higher
- Node.js 18 or higher with npm
- Application Server (Apache Tomcat 9+ or GlassFish 5+)
- Modern web browser (Chrome, Firefox, Edge)

---

## Installation Guide

### Step 1: Database Setup

1. Start your PostgreSQL service
   ```bash
   # Windows
   pg_ctl -D "C:\Program Files\PostgreSQL\XX\data" start
   
   # Linux/Mac
   sudo service postgresql start
   ```

2. Create and populate the database
   ```bash
   # Connect to PostgreSQL
   psql -U postgres -d postgres
   
   # Execute table creation script
   \i sql/requete creation tables.sql
   
   # Execute test data script
   \i sql/Creation données test.sql
   
   # Exit psql
   \q
   ```

3. Verify database setup
   ```bash
   psql -U postgres -d postgres -c "\dt"
   psql -U postgres -d postgres -c "SELECT COUNT(*) FROM interaction;"
   ```

**Database Configuration:**
If your PostgreSQL credentials differ from defaults (user: postgres, password: postgres, port: 5432), update the connection settings in:
```
back/PEPs_back/src/main/resources/META-INF/persistence.xml
```

### Step 2: Backend Deployment

1. Stop any running application server instance

2. Navigate to backend directory
   ```bash
   cd back\PEPs_back
   ```

3. Build the application
   ```bash
   mvn clean install
   ```

4. Deploy the WAR file
   - Locate the WAR file: `target/PEPs_back-0.1.war`
   - Copy to your application server's deployment directory:
     - Tomcat: `<TOMCAT_HOME>/webapps/`
     - GlassFish: Deploy via admin console or CLI
   - Ensure deployment context path is `/PEPs_back`

5. Start the application server

6. Verify backend functionality
   
   Test each endpoint in your browser or with curl:
   
   **Dashboard Statistics:**
   ```
   http://localhost:8080/PEPs_back/dashboard
   ```
   Expected response:
   ```json
   {"totalInteractions":4,"activeModules":2,"lastInteraction":"2025-01-18 10:30:45"}
   ```
   
   **Interactions List:**
   ```
   http://localhost:8080/PEPs_back/interactions
   ```
   
   **Modules List:**
   ```
   http://localhost:8080/PEPs_back/modules
   ```
   
   **Sounds List:**
   ```
   http://localhost:8080/PEPs_back/sounds
   ```
   
   **Daily Statistics:**
   ```
   http://localhost:8080/PEPs_back/daily-stats
   ```

### Step 3: Frontend Setup

1. Navigate to frontend directory
   ```bash
   cd front\pepsfront
   ```

2. Install dependencies
   ```bash
   npm install
   ```

3. Start development server
   ```bash
   npm start
   ```

4. Access the application
   ```
   http://localhost:4200
   ```

### Step 4: Application Usage

1. **Login**
   - Password: `admin`
   - The system uses SHA-256 password hashing

2. **Dashboard View**
   - Displays total interaction count
   - Shows number of active modules
   - Shows most recent interaction timestamp
   - Displays hourly interaction distribution chart

3. **Interactions Page**
   - View complete interaction history
   - Filter by time period:
     - **Toutes**: All interactions
     - **Aujourd'hui**: Today's interactions
     - **Hier**: Yesterday's interactions
     - **Semaine**: Last 7 days
   - Export data to CSV format

4. **Modules Page**
   - View all registered modules
   - Access module configuration:
     - Volume control (0-100%)
     - Operation mode (Manuel/Automatique)
     - Active status toggle
     - Sound enable/disable
   - Save configuration changes to database

5. **Sounds Library**
   - Browse available sounds
   - View sound names and types

6. **Refresh Data**
   - Use the refresh button to reload data from backend
   - All pages automatically fetch current data on load

---

## API Documentation

The backend exposes the following REST API endpoints:

### Dashboard Statistics
**Endpoint:** `GET /dashboard`

**Description:** Returns overall system statistics

**Response:**
```json
{
  "totalInteractions": 4,
  "activeModules": 2,
  "lastInteraction": "2025-01-18 10:30:45"
}
```

### Interactions List
**Endpoint:** `GET /interactions`

**Description:** Returns all interaction records sorted by date (newest first)

**Response:**
```json
[
  {
    "id": 1,
    "date": "2025-01-18T10:25:00",
    "module": "Module Perchoir 1",
    "type": "Bec"
  },
  {
    "id": 2,
    "date": "2025-01-18T10:20:00",
    "module": "Module Nid 2",
    "type": "Patte"
  }
]
```

### Modules List
**Endpoint:** `GET /modules`

**Description:** Returns all module configurations

**Response:**
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

### Module Details
**Endpoint:** `GET /modules/{id}`

**Description:** Returns specific module details

**Parameters:**
- `id` (path parameter): Module identifier

**Response:** Same structure as single module object from modules list

### Module Update
**Endpoint:** `PUT /modules/{id}`

**Description:** Updates module configuration

**Parameters:**
- `id` (path parameter): Module identifier

**Request Body:**
```json
{
  "id": 1,
  "name": "Module Perchoir 1",
  "location": "Enclos Nord",
  "status": "Actif",
  "ip": "192.168.1.10",
  "config": {
    "volume": 85,
    "mode": "Manuel",
    "actif": true,
    "son": true
  }
}
```

**Response:** Updated module object

### Sounds List
**Endpoint:** `GET /sounds`

**Description:** Returns all available sounds

**Response:**
```json
[
  {
    "id": 1,
    "name": "Cri Ara Bleu",
    "type": "Vocal"
  },
  {
    "id": 2,
    "name": "Chant Foret Amazonienne",
    "type": "Ambiance"
  }
]
```

### Daily Statistics
**Endpoint:** `GET /daily-stats`

**Description:** Returns interaction counts grouped by 2-hour intervals for the current day

**Response:**
```json
[
  {"time": "8h", "count": 0},
  {"time": "10h", "count": 2},
  {"time": "12h", "count": 1},
  {"time": "14h", "count": 0},
  {"time": "16h", "count": 0},
  {"time": "18h", "count": 1}
]
```

---

## Troubleshooting Guide

### Backend Issues

#### Database Connection Failure

**Symptoms:**
- Application fails to start
- Connection timeout errors
- Authentication failures

**Solutions:**
1. Verify PostgreSQL service is running
   ```bash
   # Windows
   sc query postgresql-x64-XX
   
   # Linux/Mac
   sudo service postgresql status
   ```

2. Check database credentials in `persistence.xml`
   - Username: postgres
   - Password: postgres
   - Port: 5432
   - Database: postgres

3. Test database connectivity
   ```bash
   psql -U postgres -d postgres -c "SELECT 1"
   ```

4. Verify database exists and contains tables
   ```bash
   psql -U postgres -l
   psql -U postgres -d postgres -c "\dt"
   ```

#### HTTP 404 Errors on API Endpoints

**Symptoms:**
- Browser shows "404 Not Found"
- API endpoints not accessible

**Solutions:**
1. Verify WAR file is properly deployed
   - Check application server's webapps directory
   - Confirm deployment context path is `/PEPs_back`

2. Check servlet mapping in `web.xml`
   - Should be configured with `<url-pattern>/</url-pattern>`
   - Not `<url-pattern>*.do</url-pattern>`

3. Review application server logs
   - Tomcat: `<TOMCAT_HOME>/logs/catalina.out`
   - GlassFish: `<GLASSFISH_HOME>/glassfish/domains/domain1/logs/server.log`

4. Verify application server is running
   ```bash
   # Check if port 8080 is listening
   netstat -an | findstr "8080"
   ```

#### Empty or Null Data Responses

**Symptoms:**
- API returns empty arrays
- Null values in responses

**Solutions:**
1. Verify test data was inserted
   ```bash
   psql -U postgres -d postgres -c "SELECT COUNT(*) FROM interaction;"
   psql -U postgres -d postgres -c "SELECT COUNT(*) FROM module;"
   psql -U postgres -d postgres -c "SELECT COUNT(*) FROM sound;"
   ```

2. Check JPA entity mappings match database schema

3. Review application server logs for exceptions

### Frontend Issues

#### CORS (Cross-Origin) Errors

**Symptoms:**
- Browser console shows CORS policy errors
- Network requests blocked

**Solutions:**
1. Verify CORS configuration in controllers
   - Should include: `@CrossOrigin(origins = "http://localhost:4200")`

2. If using non-standard port, update CORS origins in all controllers

3. Clear browser cache and restart development server

#### Connection Errors

**Symptoms:**
- "Erreur de connexion" message
- "Chargement..." never completes
- Empty data displays

**Solutions:**
1. Verify backend is running and accessible
   ```bash
   curl http://localhost:8080/PEPs_back/dashboard
   ```

2. Check browser console (F12) for detailed errors
   - Network tab: Verify request URLs
   - Console tab: Look for JavaScript errors

3. Verify BASE_URL constant in `app.ts`
   ```typescript
   private readonly BASE_URL = 'http://localhost:8080/PEPs_back';
   ```

4. Test API endpoints directly in browser

#### Data Not Updating

**Symptoms:**
- Old data persists
- Changes not reflected
- Empty lists

**Solutions:**
1. Clear browser cache
   - Chrome: Ctrl+Shift+Delete
   - Firefox: Ctrl+Shift+Delete
   - Edge: Ctrl+Shift+Delete

2. Hard refresh page
   - Chrome/Firefox/Edge: Ctrl+Shift+R

3. Check browser console for HTTP errors

4. Verify backend endpoints return current data

5. Clear browser local storage
   ```javascript
   // In browser console
   localStorage.clear();
   sessionStorage.clear();
   ```

#### Date Filtering Issues

**Symptoms:**
- Filters show incorrect data
- "Aujourd'hui" shows no results when data exists

**Solutions:**
1. Verify system clock is correct

2. Check database timestamps
   ```bash
   psql -U postgres -d postgres -c "SELECT time_lancement FROM interaction ORDER BY time_lancement DESC LIMIT 5;"
   ```

3. Verify date format consistency between frontend and backend

4. Check browser console for date parsing errors

### Performance Issues

#### Slow Page Load

**Solutions:**
1. Check database query performance
   - Add indexes on frequently queried columns

2. Verify network connectivity
   - Test latency between frontend and backend

3. Monitor application server resources
   - CPU usage
   - Memory consumption

#### Large Data Sets

**Solutions:**
1. Implement pagination for large lists

2. Add date range filters to limit query results

3. Consider implementing lazy loading

---

## Database Schema Reference

### Module Table
```sql
CREATE TABLE module (
  idmodule SERIAL PRIMARY KEY,
  nom VARCHAR(255) NOT NULL,
  ip_adress VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL,
  volume INTEGER NOT NULL,
  current_mode VARCHAR(50) NOT NULL,
  actif BOOLEAN NOT NULL,
  last_seen TIMESTAMP NOT NULL
);
```

### Interaction Table
```sql
CREATE TABLE interaction (
  idinteraction SERIAL PRIMARY KEY,
  idsound INTEGER REFERENCES sound(idsound),
  idmodule INTEGER REFERENCES module(idmodule),
  typeInteraction VARCHAR(50) NOT NULL,
  time_lancement TIMESTAMP NOT NULL
);
```

### Sound Table
```sql
CREATE TABLE sound (
  idsound SERIAL PRIMARY KEY,
  nom VARCHAR(255) NOT NULL,
  type_son VARCHAR(50) NOT NULL
);
```

---

## Development Notes

### Project Structure

**Backend:**
```
back/PEPs_back/
├── src/main/java/peps/peps_back/
│   ├── controllers/       # REST API controllers
│   ├── items/             # JPA entities
│   ├── repositories/      # Data access layer
│   └── resources/         # Configuration files
├── src/main/webapp/
│   └── WEB-INF/
│       ├── web.xml        # Servlet configuration
│       ├── dispatcher-servlet.xml
│       └── applicationContext.xml
└── pom.xml                # Maven dependencies
```

**Frontend:**
```
front/pepsfront/
├── src/app/
│   ├── app.ts             # Main component logic
│   ├── app.html           # Main component template
│   ├── app.css            # Component styles
│   ├── app.config.ts      # Application configuration
│   └── app.routes.ts      # Routing configuration
└── package.json           # NPM dependencies
```

### Technology Stack

**Backend:**
- Java 8+
- Spring Framework 5.3
- Spring Data JPA
- PostgreSQL JDBC Driver
- Jackson for JSON serialization

**Frontend:**
- Angular 20+
- Angular Material 20+
- TypeScript 5+
- RxJS for reactive programming

### Build Tools
- Maven 3.6+ (Backend)
- npm (Frontend)

---

## Support and Maintenance

For issues and questions:
1. Check application server logs for detailed error messages
2. Review PostgreSQL logs for database-related issues
3. Use browser developer tools (F12) to inspect network requests and console errors
4. Verify all services are running on expected ports
5. Test API endpoints individually to isolate issues

Regular maintenance tasks:
- Monitor database size and performance
- Review and archive old interaction records
- Update dependencies for security patches
- Backup database regularly
- Monitor application server resources
