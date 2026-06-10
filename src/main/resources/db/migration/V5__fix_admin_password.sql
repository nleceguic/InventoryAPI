-- Fix admin password hash (Admin1234!)
UPDATE users
SET password = '$2a$10$q8/D8NXyJprrd1HthPuUTuAC9zaXuhHRu40Y8cUvs8Nlfd8zvVWeC'
WHERE email = 'admin@inventory.com';
