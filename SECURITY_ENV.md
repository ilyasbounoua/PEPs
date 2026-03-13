# Security Configuration - Environment Variables

## ⚠️ IMPORTANT - CRITICAL SECURITY

This project uses environment variables to manage sensitive configurations. **NEVER** commit `.env` files with real credentials to the repository.

## Configuration Files

### 📁 Files in Repository

1. **`.env.example`** - Empty template (NO default values)
   - Only variable names
   - Use as reference to create `.env`

2. **`.env.prod.example`** - Empty template for production
   - Only variable names
   - Use as reference to create `.env` in production

3. **`.env.dev`** - Safe values for local development (IGNORED)
   - 🚫 Now ignored by .gitignore
   - Use as reference to create your local `.env`
   - Contains development values that are NOT secrets

### 🚫 Files BLOCKED by .gitignore

The following files should **NEVER** be committed:

- `.env` - Real configuration file
- `.env.local` - Local configuration
- `.env.*.local` - Any local variant
- `.env.production` - Production configuration

## How to Configure

### For Local Development

**Option 1: Use safe development values (Recommended)**
```bash
# Copy development file
cp .env.dev .env

# Project will start with safe development values
```

**Option 2: Configure manually**
```bash
# Copy empty template
cp .env.example .env

# Edit .env with your values
nano .env
```

### For Production

```bash
# Copy production template
cp .env.prod.example .env

# Edit with REAL production credentials
nano .env

# VERIFY that .env is NOT in git
git status
# .env should NOT appear in the list
```

## Required Values

### Mandatory Variables

```bash
# Proxy Configuration
PROXY_PORT=
PROXY_SSL_PORT=

# Backend Configuration
SPRING_PROFILE=
DB_HOST=
DB_PORT=
DB_NAME=
DB_USER=
DB_PASSWORD=

# Frontend Configuration
API_URL=

# Resources Information
BACKEND_MEMORY_LIMIT=
BACKEND_CPU_LIMIT=
FRONTEND_MEMORY_LIMIT=
FRONTEND_CPU_LIMIT=
PROXY_MEMORY_LIMIT=
PROXY_CPU_LIMIT=
```

## Security Verification

### ✅ Checklist Before Commit

1. **Verify .env is not staged**
   ```bash
   git status
   # .env should NOT appear
   ```

2. **Verify .gitignore**
   ```bash
   cat .gitignore | grep .env
   # Should show exclusions
   ```

3. **Search for credentials in committed files**
   ```bash
   # Search for passwords in staged files
   git diff --cached | grep -i password
   # Should NOT show real passwords
   ```

## File Structure

```
PEPs/
├── .env.example          # ✅ Empty template (committed)
├── .env.prod.example     # ✅ Empty production template (committed)
├── .env.dev              # 🚫 Now IGNORED (template for local dev)
├── .env                  # 🚫 YOUR real configuration (DO NOT commit)
├── .gitignore            # ✅ Blocks .env and .env.dev (committed)
└── docker-compose.yml    # ✅ No credentials (committed)
```

## Frequently Asked Questions

### Why is .env.dev ignored?

Although it contains safe development values, it is now ignored to prevent local configuration changes from being accidentally committed, keeping the repository clean. Use it as a template to create your own `.env`.

### What if I accidentally commit .env?

1. **IMMEDIATELY** change all credentials in production
2. Remove file from Git history:
   ```bash
   git rm --cached .env
   git commit -m "Remove .env from repository"
   git push
   ```
3. Rotate all exposed credentials

### How do I know if my credentials are secure?

```bash
# Verify .env is not in repository
git ls-files | grep .env
# Should only show: .env.dev, .env.example, .env.prod.example

# Verify no credentials in docker-compose
grep -i password docker-compose*.yml
# Should NOT show real passwords, only ${DB_PASSWORD}
```

## Best Practices

1. ✅ **Use environment variables** - Never hardcode credentials
2. ✅ **Rotate credentials** - Change passwords regularly
3. ✅ **Different credentials** - Dev vs Prod must be different
4. ✅ **Verify before commit** - Always check `git status`
5. ✅ **Use .env.dev for development** - Don't create .env manually if not necessary

## Support

If you have questions about security configuration, consult:
- [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) - Deployment guide
- [.env.example](.env.example) - Available variables
- [.env.dev](.env.dev) - Development configuration example

---

**⚠️ FINAL REMINDER**: Never commit `.env` files with real credentials. Always verify with `git status` before committing.
