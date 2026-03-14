-- Reset admin password to 'admin'
UPDATE peps.users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    enabled = true,
    role = 'admin',
    permission = 'admin'
WHERE login = 'admin';
