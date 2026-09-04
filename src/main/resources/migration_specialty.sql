INSERT INTO specialties (name, active, created_at, updated_at)
SELECT DISTINCT specialization, true, NOW(), NOW()
FROM doctors
WHERE specialization IS NOT NULL
  AND specialization NOT IN (SELECT name FROM specialties);

UPDATE doctors d
SET specialty_id = s.id
FROM specialties s
WHERE d.specialization = s.name;


UPDATE doctors
SET consultation_fee = 0
WHERE consultation_fee IS NULL;

