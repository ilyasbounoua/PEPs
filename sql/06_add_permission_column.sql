-- ============================================================
-- Migration: Add 'permission' column to users table
-- This column was missing from 01_create_tables.sql and is
-- required by the User.java entity.
--  - viewer  : read-only
--  - editor  : read + write
--  - admin   : full access + user management
-- ============================================================
ALTER TABLE peps.users
    ADD COLUMN IF NOT EXISTS permission varchar(20) NOT NULL DEFAULT 'viewer';
