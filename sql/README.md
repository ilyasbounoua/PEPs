# Database — SQL Scripts

This directory contains all SQL scripts for the PostgreSQL database.
Docker automatically runs the numbered scripts at container startup **in alphanumeric order**.

> ⚠️ **The schema is frozen.** No structural changes are planned. Do not modify these scripts unless you know what you are doing.

---

## How It Works (Fresh Install)

When you run `docker compose up` for the first time (or after `docker compose down -v`), Docker mounts the entire `./sql` directory into the container's `/docker-entrypoint-initdb.d/` folder.  
PostgreSQL executes every `.sql` file it finds there, **in alphanumeric order**, only on a **fresh/empty volume**.

> 🔁 If the volume already exists (i.e., you have already run the project before), Docker will **not** re-run these scripts. To force a re-initialization, destroy the volume first:
> ```bash
> docker compose down -v
> docker compose up
> ```

---

## Script Execution Order

| File | Run by Docker? | Purpose |
|------|---------------|---------|
| `00_schema.sql` | ✅ Auto | Creates the `peps` schema |
| `01_create_tables.sql` | ✅ Auto | Creates all tables (Module, Sound, Interaction, Users, audit_logs) |
| `02_migration_owner.sql` | ✅ Auto | Adds `owner_role` column — historical migration, safe to re-run (uses ALTER) |
| `03_trigger.sql` | ✅ Auto | Creates the cascade trigger for `owner_role` on user role changes |
| `04_correction.sql` | ✅ Auto | Data correction: normalizes legacy `admin` → `aras` role values |
| `05_normalization.sql` | ✅ Auto | Normalizes all `owner_role` values to lowercase |
| `99_restore_data.sql` | ✅ Auto | Inserts the base dataset (sanitized export from remote DB) |
| `100_archive_audit.sql` | ✅ Auto | Creates archive/audit test data |
| `101_archive_test.sql` | ✅ Auto | Inserts additional archive test records |

> 📌 Files ending in `.disabled` or without a `.sql` extension are **ignored** by Docker.

---

## Table Overview

```
peps.users          → Application users (login, password_hash, role, enabled)
peps.Module         → IoT modules (IP, status, volume, mode, owner_role)
peps.Sound          → Sound files (name, type, path, owner_role)
peps.Interaction    → Logs of module/sound interactions (owner_role)
peps.audit_logs     → Audit trail for archive feature (action, entity, user, timestamp)
```

Key design decisions:
- All tables live under the **`peps` schema** (not `public`)
- Ownership is tracked via **`owner_role`** (role-based, not user-id-based) — see `02_migration_owner.sql` for the migration history
- `Module` and `Sound` have a **`version` column** for optimistic locking in the Java backend
- A **trigger** (`trigger_cascade_owner_role`) automatically propagates role changes from `users` to all dependent tables

---

## Validating After Startup

After running `docker compose up`, you can verify the DB is correctly initialized:

```bash
# Connect to the running container
docker exec -it peps-database psql -U <DB_USER> -d <DB_NAME>

# Inside psql — check all tables exist
\dt peps.*

# Check some data loaded correctly
SELECT COUNT(*) FROM peps.users;
SELECT COUNT(*) FROM peps.Module;
SELECT COUNT(*) FROM peps.Sound;

# Check trigger exists
SELECT trigger_name FROM information_schema.triggers WHERE trigger_name = 'trigger_cascade_owner_role';

# Exit
\q
```

Replace `<DB_USER>` and `<DB_NAME>` with the values from your `.env` file.

---

## Migration History (For Reference Only)

The following scripts document **past migrations** applied to the remote database.
They are included in the auto-run sequence and are **idempotent** (safe to run on an empty DB), but they were originally created to patch a live database:

| Script | What happened |
|--------|--------------|
| `02_migration_owner.sql` | Replaced `owner_id` (FK to users) with `owner_role` (string role) across all tables |
| `04_correction.sql` | Fixed legacy `admin` role values that should have been `aras` |
| `05_normalization.sql` | Forced all `owner_role` values to lowercase (backend expects lowercase) |

---

## The Data File (`99_restore_data.sql`)

This file is a **sanitized dump** from the remote production database. It was processed to:
1. Remap all `public.` schema references → `peps.`
2. Fix sequence names (`user_id_seq` → `users_id_user_seq`)
3. Remove `SET transaction_timeout` (incompatible with PostgreSQL 15)

This file should **not be manually edited**. If a fresh data export is needed in the future, follow the same sanitization steps.
