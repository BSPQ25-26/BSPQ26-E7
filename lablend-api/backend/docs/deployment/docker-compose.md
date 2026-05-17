# Docker Compose Deployment

Production-ready Docker Compose setup for LabLend with containerized services.

## Services Configuration

**File**: `docker-compose.yml`

```yaml
version: '3.8'

services:
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: lablend-frontend
    ports:
      - "3000:3000"
    environment:
      - VITE_API_URL=http://localhost:8080/api
    depends_on:
      - backend
    networks:
      - lablend-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: lablend-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/lablend
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root
      - JWT_SECRET=your-jwt-secret-key
    depends_on:
      db:
        condition: service_healthy
    networks:
      - lablend-network
    volumes:
      - ./logs:/app/logs

  db:
    image: mysql:8.0
    container_name: lablend-db
    ports:
      - "3307:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=lablend
    volumes:
      - ./database/init:/docker-entrypoint-initdb.d
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 5s
      retries: 5
    networks:
      - lablend-network

networks:
  lablend-network:
    driver: bridge

volumes:
  mysql-data:
```

## Service Details

### Frontend Service

```yaml
frontend:
  build:
    context: ./frontend      # Build from Dockerfile
    dockerfile: Dockerfile
  container_name: lablend-frontend
  ports:
    - "3000:3000"           # Host:Container port mapping
  environment:
    - VITE_API_URL=http://localhost:8080/api
  depends_on:
    - backend               # Wait for backend to start
  networks:
    - lablend-network       # Connect to shared network
```

**Dockerfile** (`frontend/Dockerfile`):
```dockerfile
# Build stage
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
```

### Backend Service

```yaml
backend:
  build:
    context: ./backend
    dockerfile: Dockerfile
  container_name: lablend-backend
  ports:
    - "8080:8080"
  environment:
    - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/lablend
    - SPRING_DATASOURCE_USERNAME=root
    - SPRING_DATASOURCE_PASSWORD=root
    - JWT_SECRET=your-jwt-secret-key
  depends_on:
    db:
      condition: service_healthy  # Wait for DB health check
  volumes:
    - ./logs:/app/logs            # Persist logs
```

**Dockerfile** (`backend/Dockerfile`):
```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:download-sources -DskipTests
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/lablend-backend.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

### Database Service

```yaml
db:
  image: mysql:8.0
  container_name: lablend-db
  ports:
    - "3307:3306"              # External:Internal
  environment:
    - MYSQL_ROOT_PASSWORD=root
    - MYSQL_DATABASE=lablend
  volumes:
    - ./database/init:/docker-entrypoint-initdb.d  # Run init scripts
    - mysql-data:/var/lib/mysql                     # Persist data
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
    timeout: 5s
    retries: 5
```

## Common Commands

```bash
# Start all services
docker-compose up

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f db

# Stop services
docker-compose stop

# Stop and remove containers
docker-compose down

# Remove containers and volumes
docker-compose down -v

# Rebuild images
docker-compose build --no-cache

# Access database from host
mysql -h localhost -P 3307 -u root -p

# Execute command in container
docker-compose exec backend bash
docker-compose exec frontend sh
docker-compose exec db mysql -u root -p lablend
```

## Networking

All services are on the same network (`lablend-network`):

- Frontend → Backend: `http://backend:8080/api`
- Backend → Database: `jdbc:mysql://db:3306/lablend`
- Host → Services: Mapped ports (3000, 8080, 3307)

## Environment Variables

### Production Setup

Create `.env.prod`:

```
JWT_SECRET=super-secure-secret-key-min-32-chars
SPRING_DATASOURCE_USERNAME=dbuser
SPRING_DATASOURCE_PASSWORD=dbpassword
VITE_API_URL=https://api.example.com/api
```

Then run:
```bash
docker-compose --env-file .env.prod up
```

## Volumes & Persistence

### MySQL Data

```yaml
volumes:
  mysql-data:
    driver: local
```

Creates a Docker volume to persist database between restarts:

```bash
# List volumes
docker volume ls

# Inspect volume
docker volume inspect lablend_mysql-data
```

### Application Logs

```yaml
volumes:
  - ./logs:/app/logs  # Mount local logs directory
```

Backend logs are written to `./logs/` directory on host.

## Health Checks

Database service includes health check:

```yaml
healthcheck:
  test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
  timeout: 5s
  retries: 5
```

Backend waits for database to be healthy before starting:

```yaml
depends_on:
  db:
    condition: service_healthy
```

## Scaling

To run multiple backend instances:

```bash
docker-compose up --scale backend=3
```

Use nginx reverse proxy (in frontend) to load balance.

## Monitoring

```bash
# Monitor resource usage
docker stats

# View container details
docker-compose ps

# Inspect container
docker-compose exec backend ps aux
```

---

**Next**: Production deployment guide is not included in this build.
