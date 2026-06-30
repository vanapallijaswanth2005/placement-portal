# Running the Placement Portal Locally

## Prerequisites
- Java 21+
- MySQL Server running with database `placement_portal`
- Maven (or use `mvnw.cmd` wrapper included)

## Option 1: Run via Maven CLI (Recommended for testing)

```powershell
# Set environment variables (one-time in PowerShell session)
$env:DB_USERNAME='root'
$env:DB_PASSWORD='Jaswanth@2005'
$env:JWT_SECRET='dev-secret-0123456789abcdef01234567'

# Run the app
.\mvnw.cmd spring-boot:run
```

**Access:**
- App: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui/index.html

## Option 2: Run via IntelliJ IDE (No Setup Required!)

Just click the green Run button (Shift+F10). The app uses fallback defaults in `application.properties`:
- DB username: `root`
- DB password: `Jaswanth@2005` 
- JWT secret: `dev-secret-0123456789abcdef01234567`

**Optional:** For explicit dev profile, go to `Run` → `Edit Configurations...` → add VM option: `-Dspring.profiles.active=dev`

## Option 3: Production Run (with env vars)

For production or CI/CD environments, use environment variables:

```powershell
$env:DB_USERNAME='your-username'
$env:DB_PASSWORD='your-password'
$env:JWT_SECRET='your-secure-jwt-secret-min-32-bytes'

.\mvnw.cmd spring-boot:run
```

The `application.properties` will use `${DB_USERNAME}`, `${DB_PASSWORD}`, and `${JWT_SECRET}` placeholders, which Spring resolves from environment variables.

## Database Setup

Before running, create the database:

```sql
CREATE DATABASE IF NOT EXISTS placement_portal;
```

The app will auto-create tables on startup (via Hibernate `ddl-auto: update`).

## Testing Endpoints

### Get Swagger UI
```
http://localhost:8081/swagger-ui/index.html
```

### Example: Register a Student
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "securePassword123",
    "rollNumber": "21BCS001"
  }'
```

### Example: Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "securePassword123"
  }'
```

The response will contain a JWT token—use it for authenticated requests:
```bash
curl -H "Authorization: Bearer <your-jwt-token>" \
  http://localhost:8081/api/student/profile
```

## Troubleshooting

**Issue: "Access denied for user '${DB_USERNAME}'@'localhost'"**
- The dev profile was not loaded
- Solution: Ensure `-Dspring.profiles.active=dev` is set, or set environment variables manually

**Issue: "Connection refused" on port 8081**
- Another app is using port 8081
- Check `application.properties` or `application-dev.properties` for `server.port` setting

**Issue: JWT secret validation error on startup**
- JWT secret must be at least 32 bytes (256 bits)
- Dev default is: `dev-secret-0123456789abcdef01234567` (42 bytes)

## Security Notes

⚠️ **IMPORTANT FOR DEVELOPERS:**
- **Default credentials in `application.properties` are for LOCAL DEVELOPMENT ONLY**
- Both `application.properties` and `application-dev.properties` contain dev credentials
- This makes it easy to run locally, but **MUST be overridden in production**
- Always use environment variables (`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`) in production/CI-CD
- Never commit real database passwords or secrets to version control

**These credentials are already exposed in git history as of recent changes. For production:**
1. Use strong, unique credentials
2. Rotate database password immediately
3. Deploy with environment variables (docker, k8s, cloud platforms all support this)
4. Use a secrets manager (AWS Secrets Manager, HashiCorp Vault, etc.)
5. Consider using git-filter-repo to scrub history (breaking change, requires coordination)
