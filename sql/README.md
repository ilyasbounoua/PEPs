# Database Initialization & Migration

This directory contains the SQL scripts used to initialize the PostgreSQL Docker container.

## Script Order & Structure

Docker runs scripts in alphanumeric order. We have organized them as follows to ensure dependencies are met:

*   **`00_schema.sql`**: Sets up schemas (`peps`, `extensions`).
*   **`01_create_tables.sql`**: Creates the base tables.
    *   *Includes manual fixes for `audit_logs`, `Module.version`, and `Sound.version`.*
*   **`02_migration_owner.sql`**: Sets up roles/owners.
*   **`03_trigger.sql`**: Creates database triggers.
*   **`99_restore_data.sql`**: **(Generated)** Contains the "clean" data imported from the remote environment.
*   **`100_archive_*.sql`**: Scripts related to the new "Archive" feature (test data).

## Known Fixes & deviations from `main`

To make the local Docker environment work with the latest Java backend, the following manual patches were applied to `01_create_tables.sql`:

1.  **Missing `audit_logs` Table**:
    *   The `Archive` feature (Java) requires this table, but the SQL was missing from the `main` branch.
    *   It has been manually added to `01_create_tables.sql`.
2.  **Missing `version` Columns**:
    *   `Module` and `Sound` tables now include a `version integer DEFAULT 0` column.
    *   Required by the Backend entities for optimistic locking.

## Data Restoration Process

The file `99_restore_data.sql` is a **sanitized dump** from the remote database. checking:
1.  **Schema Mapping**: Converted `public` schema refs to `peps`.
2.  **Sequence Fixes**: Mapped `user_id_seq` to `users_id_user_seq`.
3.  **Cleanup**: Removed hardcoded `SET transaction_timeout` (incompatible with Postgres 15).

### How to updating data (If needed)

If you need to refresh data from remote:
1.  Export data: `pg_dump -a ... > data.sql`
2.  **CRITICAL**: You must run a cleanup script to remap schemas and remove incompatible headers before placing it here.
    *   *See the PowerShell one-liner used in the migration task history.*

## Maintenance Warning

⚠️ **Current Workflow Limitation**:
There is currently **no automated migration pipeline**.
If the Backend Java entities change (e.g., adding a new field), you **MUST** manually update `01_create_tables.sql` or the container will crash.
long-term solution: Implement a migration tool (Flyway, Liquibase) or specific Doctrine migration scripts.
