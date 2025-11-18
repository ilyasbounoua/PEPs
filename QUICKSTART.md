# PEPs Application - Quick Start Guide

## What is PEPs?

PEPs is a web-based monitoring and management system designed for tracking interactions with intelligent modules. The application provides real-time statistics, historical data analysis, and configuration management capabilities.

## Quick Setup (5 Minutes)

### 1. Database (2 minutes)
```bash
# Start PostgreSQL
pg_ctl start

# Create database structure
psql -U postgres -d postgres -f "sql/requete creation tables.sql"

# Load sample data
psql -U postgres -d postgres -f "sql/Creation données test.sql"
```

### 2. Backend (2 minutes)
```bash
# Build application
cd back\PEPs_back
mvn clean install

# Deploy to application server
# Copy target/PEPs_back-0.1.war to your server's webapps folder

# Verify deployment
curl http://localhost:8080/PEPs_back/dashboard
```

### 3. Frontend (1 minute)
```bash
# Start development server
cd front\pepsfront
npm install
npm start

# Access application
# Open http://localhost:4200
```

## Default Credentials

- **Username:** (none required)
- **Password:** `admin`

## Main Features

### Dashboard
- Real-time interaction statistics
- Active module count
- Last interaction timestamp
- Hourly activity chart

### Interactions
- Complete interaction history
- Date-based filtering
- CSV export capability

### Modules
- Module status monitoring
- Configuration management
- Real-time updates

### Sound Library
- Browse available sounds
- View sound metadata

## Default Ports

- Backend API: `http://localhost:8080`
- Frontend UI: `http://localhost:4200`
- Database: `localhost:5432`

## Minimum Requirements

- PostgreSQL 12+
- Java 8+
- Node.js 18+
- 2GB RAM
- Modern web browser

## Common Issues

**Can't connect to backend?**
- Ensure Tomcat/GlassFish is running
- Verify deployment at `/PEPs_back` context

**No data showing?**
- Run the test data SQL script
- Check backend endpoints return data

**CORS errors?**
- Frontend must run on port 4200
- Or update CORS settings in backend controllers

## Project Structure

```
PEPs/
├── back/PEPs_back/         # Java Spring backend
├── front/pepsfront/        # Angular frontend  
├── sql/                    # Database scripts
└── SETUP_GUIDE.md          # Detailed documentation
```

## Next Steps

After successful setup:
1. Login with default credentials
2. Explore the dashboard
3. Review sample data in Interactions page
4. Configure a test module
5. Review SETUP_GUIDE.md for advanced configuration

## Support

For detailed troubleshooting and configuration options, refer to SETUP_GUIDE.md in the project root directory.

---

**Version:** 1.0  
**Last Updated:** 2025-11-18
