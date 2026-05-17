# Quick Start Guide

Get LabLend running locally in **5 minutes**. This guide uses Docker Compose for the simplest setup.

## Prerequisites

Ensure you have these installed:

- **Docker** (v20+) and **Docker Compose** (v2+)
- **Git** (to clone the repository)

**Optional** (for development without Docker):
- Java 21 JDK
- Node.js 18+
- MySQL 8.0

## Run with Docker Compose

### Step 1: Clone the Repository

```bash
git clone https://github.com/BSPQ25-26/BSPQ26-E7.git
cd BSPQ26-E7/lablend-api
```

### Step 2: Start All Services

```bash
docker-compose up -d
```

This command starts:
- **Frontend** — React app on http://localhost:3000
- **Backend** — Spring Boot API on http://localhost:8080
- **Database** — MySQL on localhost:3307 (username: root, password: root)

### Step 3: Verify Services are Running

```bash
docker-compose ps
```

You should see three containers with "healthy" or "running" status.

### Step 4: Access the Application

Open your browser:

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Javadoc**: view the generated Javadoc in the built site under `javadoc/index.html`

### Step 5: Login

Use the default admin credentials to log in:

- **Email**: `admin@lablend.com`
- **Password**: `admin` (or as configured in `database/init/schema.sql`)

**For Students**: Create a student account through the login page.

### Step 6: Stop Services

When done, stop all containers:

```bash
docker-compose down
```

To remove all data:

```bash
docker-compose down -v
```
## Verifying Installation

### Test Backend API

```bash
# Get all equipment
curl http://localhost:8080/api/equipment

# Login (get JWT token)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@lablend.com",
    "password": "admin"
  }'
```

Expected response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "role": "ADMIN"
}
```

### Test Frontend

1. Go to http://localhost:3000 (Docker) or http://localhost:5173 (local)
2. Click "Login"
3. Enter admin credentials
4. You should see the dashboard with equipment list

---

## Common Issues

### Port Already in Use

If port 3000, 8080, or 3307 is already in use:

**Docker Compose**: Edit `docker-compose.yml` and change port mappings:
```yaml
ports:
  - "3000:3000"  # Change first 3000 to unused port
```

**Local Setup**: Change in `backend/src/main/resources/application.yml`:
```yaml
server:
  port: 8081  # Use different port
```

### Database Connection Error

**With Docker Compose**:
```bash
# Check database logs
docker-compose logs db

# Restart database container
docker-compose restart db
```

**With Local MySQL**:
```bash
# Verify MySQL is running
mysql -u root -p -e "SELECT 1"

# Check connection string in application.yml
grep spring.datasource.url backend/src/main/resources/application.yml
```

### Frontend Can't Connect to Backend

**Check backend is running**:
```bash
curl http://localhost:8080/api/equipment
```

**Check CORS configuration** in `backend/src/main/java/.../config/SecurityConfig.java` — should allow frontend origin.

---

## Next Steps

### For Development
- [Backend Architecture](../backend/architecture/overview.md) — Understand the code structure
- [Frontend Overview](../frontend/overview.md) — React component organization

### For API Integration
- See the backend code and generated Javadoc in the built site for API details.

### For Testing
- Testing guide is not included in this build; run `mvn test` for backend tests and `npm test` for frontend tests.

### For Deployment
- [Docker Compose Setup](../deployment/docker-compose.md) — Understanding the setup
- [Docker Compose Setup](../deployment/docker-compose.md) — Understanding the setup
 - Production deployment guide is not included in this build.

---

## Troubleshooting

**Check logs**:
```bash
# Docker Compose
docker-compose logs backend
docker-compose logs frontend
docker-compose logs db
```

**Verify configuration**:
```bash
# Backend config file
cat lablend-api/backend/src/main/resources/application.yml
```

---

**Next**: Log in and explore the application, then read the [Backend Architecture](../backend/architecture/overview.md) to understand the codebase.
