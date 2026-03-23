# PEPs - Intelligent Module Monitoring System

**Version:** 1.2 (March 2026)

PEPs is a comprehensive web-based monitoring and management system designed for intelligent IoT modules. It provides real-time statistics, historical data analysis, audio management, and seamless integration with ESP32 devices for IoT applications.

## 🚀 Features

- **Real-time Monitoring**: Track module interactions, statuses, and configurations in real-time
- **Historical Analytics**: Comprehensive dashboard with daily statistics and interaction history
- **Audio Management**: Upload, stream, and manage audio files using MinIO object storage
- **IoT Integration**: Direct communication with ESP32 modules for sensor data and control
- **User Management**: Secure authentication with admin user management
- **Containerized Deployment**: Full Docker support for easy deployment and scaling
- **Multi-environment Support**: Development, staging, and production configurations

## 🛠 Technology Stack

### Backend
- **Java**: 17
- **Framework**: Spring MVC
- **Server**: Apache Tomcat 9+
- **Database**: PostgreSQL 12+
- **Cache**: Redis 7
- **Object Storage**: MinIO (S3-compatible)

### Frontend
- **Framework**: Angular 20+
- **UI Library**: Angular Material
- **Build Tool**: Angular CLI

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Reverse Proxy**: Nginx
- **Database Scripts**: SQL migrations

## 📋 Prerequisites

Before running PEPs, ensure you have the following installed:

- **Java**: JDK 17 or higher
- **Node.js**: 18+ with npm
- **PostgreSQL**: 12+ (or Docker)
- **Redis**: 7+ (or Docker)
- **MinIO**: (or Docker)
- **Maven**: 3.6+
- **Docker**: (optional, for containerized deployment)

## 🏗 Installation & Setup

### Quick Start (Docker Recommended)

1. **Clone the repository**:
   ```bash
   git clone https://github.com/ilyasbounoua/PEPs.git
   cd PEPs
   ```

2. **Configure environment**:
   - Copy `.env.example` to `.env` (if available) or create `.env` based on `DEFINITIVE_GUIDE.md`
   - Set database, Redis, MinIO, and other service credentials

3. **Start with Docker Compose**:
   ```bash
   # For development
   docker-compose --env-file .env.dev up -d

   # For production
   docker-compose --env-file .env.prod up -d
   ```

4. **Access the application**:
   - Frontend: `http://localhost:80` (or configured port)
   - MinIO Console: `http://localhost:9001`
   - Default admin credentials: `admin` / (password from .env)

### Manual Setup

For detailed manual installation instructions, see:
- [Quick Start Guide](QUICKSTART.md)
- [Definitive Deployment Guide](DEFINITIVE_GUIDE.md)

#### Backend Setup
```bash
cd back/PEPs_back
mvn clean install
# Deploy target/PEPs_back-0.1.war to Tomcat webapps
```

#### Frontend Setup
```bash
cd front/pepsfront
npm install
npm start  # Development server on http://localhost:4200
```

#### Database Setup
```bash
# Start PostgreSQL and run migrations
psql -U postgres -d postgres -f "sql/00_schema.sql"
psql -U postgres -d postgres -f "sql/01_create_tables.sql"
# Optional: psql -U postgres -d postgres -f "sql/99_restore_data.sql"
```

## 📖 Usage

### Web Interface
- **Dashboard**: View global statistics and active modules
- **Modules**: Monitor and configure IoT modules
- **Interactions**: Browse historical interaction data
- **Sounds**: Manage audio files for modules

### IoT Integration
PEPs supports direct integration with ESP32 devices. See [IoT Integration Guide](IoT.md) for:
- Backend modifications for ESP32 communication
- API endpoints for module registration and data submission
- Example cURL commands for testing

### API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/dashboard` | Global statistics (interactions, active modules) |
| GET | `/interactions` | Full history of module interactions |
| GET | `/modules` | List all modules and their statuses |
| PUT | `/modules/{id}` | Update module config (volume, IP, mode) |
| POST | `/modules` | Register new module (IoT endpoint) |
| GET | `/sounds` | List available audio files |
| POST | `/sounds` | Upload new audio file |
| GET | `/sounds/{id}/file` | Stream audio file |
| DELETE | `/sounds/{id}` | Delete audio file |

## 🔧 Development

### Project Structure
```
PEPs/
├── back/PEPs_back/          # Java Spring backend
├── front/pepsfront/          # Angular frontend
├── nginx/                    # Reverse proxy config
├── sql/                      # Database migrations
├── scripts/                  # Deployment scripts
├── docker-compose.*.yml      # Docker configurations
└── docs/                     # Documentation
```

### Testing
```bash
# Backend tests
cd back/PEPs_back
mvn test

# Frontend tests
cd front/pepsfront
npm test
```

### Code Quality
- **SonarQube**: Configured for code analysis
- **Prettier**: Frontend code formatting
- **ESLint**: JavaScript/TypeScript linting

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow existing code style and conventions
- Add tests for new features
- Update documentation as needed
- Ensure Docker compatibility

## 📄 Documentation

- [Quick Start Guide](docs/QUICKSTART.md)
- [Definitive Deployment Guide](docs/DEFINITIVE_GUIDE.md)
- [IoT Integration Guide](docs/IoT.md)
- [Redis Pipeline Guide](docs/REDIS_PIPELINE_AND_PROFILES.md)
- [Security Environment](docs/SECURITY_ENV.md)
- [VPS Stabilization Report](docs/VPS_STABILIZATION_REPORT.md)
- [Changelog](CHANGELOG.md)

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Ilyas BOUNOUA**
- **Santiago Alexander Rodriguez**
- **Haytam BEN**
- **Anas EL HOUDI**
- **Mohamadou DIA**
- **Clément VAZEILLE**

## 🙏 Acknowledgments

- Built for intelligent IoT module management
- Special thanks to the open-source community for the amazing tools and libraries used in this project

---

**Repository**: [ilyasbounoua/PEPs](https://github.com/ilyasbounoua/PEPs)