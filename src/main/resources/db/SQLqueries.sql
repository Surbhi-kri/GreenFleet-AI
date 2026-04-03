--  Covers: CRUD, Search, Joins, Statistics, Top-N, Use-Cases
-- =============================================================


-- =============================================================
-- SECTION 1: FULL CRUD — ALL TABLES
-- =============================================================

-- ----------------------
-- 1. COMPANY
-- ----------------------

-- CREATE
INSERT INTO company (company_name, industry)
VALUES ('Hyderabad Logistics Pvt Ltd', 'Logistics');

INSERT INTO company (company_name, industry)
VALUES ('GreenMove Transport', 'Transportation');

-- READ (single)
SELECT company_id, company_name, industry, created_at
FROM company
WHERE company_id = 1;

-- READ (all)
SELECT company_id, company_name, industry, created_at
FROM company
ORDER BY created_at DESC;

-- UPDATE
UPDATE company
SET company_name = 'Hyderabad Logistics Ltd',
    industry     = 'Supply Chain'
WHERE company_id = 1;

-- DELETE
DELETE FROM company
WHERE company_id = 1;


-- ----------------------
-- 2. VEHICLE
-- ----------------------

-- CREATE
INSERT INTO vehicle (company_id, registration_no, vehicle_type, fuel_capacity_l, fuel_type)
VALUES (1, 'TS09AB1234', 'sedan', 60.00, 'petrol');

INSERT INTO vehicle (company_id, registration_no, vehicle_type, fuel_capacity_l, fuel_type)
VALUES (1, 'TS10CD5678', 'van', 80.00, 'diesel');

INSERT INTO vehicle (company_id, registration_no, vehicle_type, fuel_capacity_l, fuel_type)
VALUES (2, 'TS11EF9012', 'truck', 0.00, 'electric');

-- READ (single with company name)
SELECT
    v.vehicle_id,
    v.registration_no,
    v.vehicle_type,
    v.fuel_capacity_l,
    v.fuel_type,
    c.company_name
FROM vehicle v
         JOIN company c ON c.company_id = v.company_id
WHERE v.vehicle_id = 1;

-- READ (all)
SELECT
    v.vehicle_id,
    v.registration_no,
    v.vehicle_type,
    v.fuel_type,
    c.company_name
FROM vehicle v
         JOIN company c ON c.company_id = v.company_id
ORDER BY v.vehicle_id;

-- UPDATE
UPDATE vehicle
SET fuel_type       = 'electric',
    fuel_capacity_l = 0.00
WHERE vehicle_id = 1;

-- DELETE
DELETE FROM vehicle
WHERE vehicle_id = 1;


-- ----------------------
-- 3. DRIVER
-- ----------------------

-- CREATE
INSERT INTO driver (company_id, full_name, license_no, email)
VALUES (1, 'Ravi Kumar', 'DL-2024-001234', 'ravi.kumar@fleet.com');

INSERT INTO driver (company_id, full_name, license_no, email)
VALUES (1, 'Priya Sharma', 'DL-2024-005678', 'priya.sharma@fleet.com');

INSERT INTO driver (company_id, full_name, license_no, email)
VALUES (2, 'Arjun Reddy', 'DL-2024-009012', 'arjun.reddy@fleet.com');

-- READ (single)
SELECT
    d.driver_id,
    d.full_name,
    d.license_no,
    d.email,
    c.company_name
FROM driver d
         JOIN company c ON c.company_id = d.company_id
WHERE d.driver_id = 1;

-- READ (all)
SELECT
    d.driver_id,
    d.full_name,
    d.license_no,
    d.email,
    c.company_name
FROM driver d
         JOIN company c ON c.company_id = d.company_id
ORDER BY d.driver_id;

-- UPDATE
UPDATE driver
SET email     = 'ravi.new@fleet.com',
    full_name = 'Ravi Kumar Singh'
WHERE driver_id = 1;

-- DELETE
DELETE FROM driver
WHERE driver_id = 1;


-- ----------------------
-- 4. VEHICLE ASSIGNMENT
-- ----------------------

-- CREATE (assign driver to vehicle)
INSERT INTO vehicle_assignment (vehicle_id, driver_id, assigned_on)
VALUES (1, 1, CURRENT_DATE);

-- READ (active assignment for a vehicle)
SELECT
    va.assignment_id,
    va.assigned_on,
    d.full_name       AS driver_name,
    d.license_no,
    v.registration_no
FROM vehicle_assignment va
         JOIN driver  d ON d.driver_id  = va.driver_id
         JOIN vehicle v ON v.vehicle_id = va.vehicle_id
WHERE va.vehicle_id  = 1
  AND va.relieved_on IS NULL;

-- READ (full assignment history)
SELECT
    va.assignment_id,
    va.assigned_on,
    va.relieved_on,
    d.full_name AS driver_name
FROM vehicle_assignment va
         JOIN driver d ON d.driver_id = va.driver_id
WHERE va.vehicle_id = 1
ORDER BY va.assigned_on DESC;

-- UPDATE (relieve driver from vehicle)
UPDATE vehicle_assignment
SET relieved_on = CURRENT_DATE
WHERE vehicle_id  = 1
  AND relieved_on IS NULL;

-- DELETE
DELETE FROM vehicle_assignment
WHERE assignment_id = 1;


-- ----------------------
-- 5. ROUTE
-- ----------------------

-- CREATE
INSERT INTO route (origin, destination, distance_km, estimated_emission_kg, algorithm_used)
VALUES ('Hyderabad', 'Bangalore', 570.00, 132.500, 'dijkstra');

INSERT INTO route (origin, destination, distance_km, estimated_emission_kg, algorithm_used)
VALUES ('Hyderabad', 'Bangalore', 610.00, 118.200, 'astar');

INSERT INTO route (origin, destination, distance_km, estimated_emission_kg, algorithm_used)
VALUES ('Hyderabad', 'Chennai', 630.00, 145.800, 'greedy');

-- READ (single)
SELECT route_id, origin, destination, distance_km, estimated_emission_kg, algorithm_used
FROM route
WHERE route_id = 1;

-- READ (all)
SELECT route_id, origin, destination, distance_km, estimated_emission_kg, algorithm_used
FROM route
ORDER BY route_id;

-- UPDATE
UPDATE route
SET distance_km           = 565.00,
    estimated_emission_kg = 128.750
WHERE route_id = 1;

-- DELETE
DELETE FROM route
WHERE route_id = 1;


-- ----------------------
-- 6. TRIP
-- ----------------------

-- CREATE (start a trip)
INSERT INTO trip (driver_id, vehicle_id, route_id, started_at)
VALUES (1, 1, 1, NOW());

-- READ (single with all joined details)
SELECT
    t.trip_id,
    t.started_at,
    t.ended_at,
    t.actual_fuel_used_l,
    d.full_name       AS driver_name,
    v.registration_no AS vehicle_reg,
    v.fuel_type,
    c.company_name,
    r.origin,
    r.destination,
    r.distance_km,
    r.estimated_emission_kg
FROM trip t
         JOIN driver  d ON d.driver_id  = t.driver_id
         JOIN vehicle v ON v.vehicle_id = t.vehicle_id
         JOIN company c ON c.company_id = v.company_id
         JOIN route   r ON r.route_id   = t.route_id
WHERE t.trip_id = 1;

-- READ (all)
SELECT
    t.trip_id,
    t.started_at,
    t.ended_at,
    d.full_name       AS driver_name,
    v.registration_no AS vehicle_reg,
    r.origin,
    r.destination
FROM trip t
         JOIN driver  d ON d.driver_id  = t.driver_id
         JOIN vehicle v ON v.vehicle_id = t.vehicle_id
         JOIN route   r ON r.route_id   = t.route_id
ORDER BY t.started_at DESC;

-- UPDATE (complete a trip)
UPDATE trip
SET ended_at           = NOW(),
    actual_fuel_used_l = 48.50
WHERE trip_id = 1;

-- DELETE
DELETE FROM trip
WHERE trip_id = 1;


-- ----------------------
-- 7. EMISSION LOG
-- ----------------------

-- CREATE
INSERT INTO emission_log (trip_id, co2_kg, nox_g)
VALUES (1, 28.750, 4.200);

INSERT INTO emission_log (trip_id, co2_kg, nox_g)
VALUES (1, 31.100, 4.800);

-- READ (all logs for a trip)
SELECT
    el.log_id,
    el.co2_kg,
    el.nox_g,
    el.recorded_at,
    r.origin,
    r.destination,
    d.full_name AS driver_name
FROM emission_log el
         JOIN trip   t ON t.trip_id   = el.trip_id
         JOIN route  r ON r.route_id  = t.route_id
         JOIN driver d ON d.driver_id = t.driver_id
WHERE el.trip_id = 1
ORDER BY el.recorded_at ASC;

-- READ (total emission summary for a trip)
SELECT
    el.trip_id,
    COUNT(el.log_id)         AS total_logs,
    ROUND(SUM(el.co2_kg), 3) AS total_co2_kg,
    ROUND(SUM(el.nox_g),  3) AS total_nox_g
FROM emission_log el
WHERE el.trip_id = 1
GROUP BY el.trip_id;

-- UPDATE
UPDATE emission_log
SET co2_kg = 30.100,
    nox_g  = 4.500
WHERE log_id = 1;

-- DELETE
DELETE FROM emission_log
WHERE log_id = 1;


-- =============================================================
-- SECTION 2: SEARCH WITH DYNAMIC FILTERS, PAGINATION & SORTING
-- =============================================================

-- Search vehicles by company, fuel_type, vehicle_type
-- Sorted by registration_no ASC | Page 1 (10 per page)
SELECT
    v.vehicle_id,
    v.registration_no,
    v.vehicle_type,
    v.fuel_capacity_l,
    v.fuel_type,
    c.company_name
FROM vehicle v
         JOIN company c ON c.company_id = v.company_id
WHERE
    v.company_id   = 1              -- dynamic filter: company
  AND v.fuel_type     = 'diesel'  -- dynamic filter: fuel type
  AND v.vehicle_type  = 'van'     -- dynamic filter: vehicle type
ORDER BY v.registration_no ASC     -- dynamic sort
    LIMIT 10 OFFSET 0;                 -- page 1 | page 2 → OFFSET 10


-- Search drivers by company and name keyword
-- Sorted by full_name ASC | Page 1
SELECT
    d.driver_id,
    d.full_name,
    d.license_no,
    d.email,
    c.company_name
FROM driver d
         JOIN company c ON c.company_id = d.company_id
WHERE
    d.company_id = 1                      -- dynamic filter: company
  AND d.full_name ILIKE '%ravi%'        -- dynamic filter: name search
ORDER BY d.full_name ASC
    LIMIT 10 OFFSET 0;


-- Search trips by driver, date range, status (completed or ongoing)
-- Sorted by started_at DESC | Page 1
SELECT
    t.trip_id,
    t.started_at,
    t.ended_at,
    t.actual_fuel_used_l,
    d.full_name       AS driver_name,
    v.registration_no AS vehicle_reg,
    r.origin,
    r.destination
FROM trip t
         JOIN driver  d ON d.driver_id  = t.driver_id
         JOIN vehicle v ON v.vehicle_id = t.vehicle_id
         JOIN route   r ON r.route_id   = t.route_id
WHERE
    t.driver_id  = 1                        -- dynamic filter: driver
  AND t.started_at >= '2026-01-01'        -- dynamic filter: date from
  AND t.started_at <  '2026-12-31'        -- dynamic filter: date to
  AND t.ended_at IS NOT NULL              -- dynamic filter: completed trips
ORDER BY t.started_at DESC
    LIMIT 10 OFFSET 0;


-- Search routes by origin and destination
-- Sorted by estimated_emission_kg ASC | Page 1
SELECT
    route_id,
    origin,
    destination,
    distance_km,
    estimated_emission_kg,
    algorithm_used
FROM route
WHERE
    origin      ILIKE '%hyderabad%'    -- dynamic filter
    AND destination ILIKE '%bangalore%' -- dynamic filter
ORDER BY estimated_emission_kg ASC     -- sort: lowest emission first
    LIMIT 10 OFFSET 0;


-- =============================================================
-- SECTION 3: SEARCH WITH JOINED DATA (USE-CASE QUERIES)
-- =============================================================

-- UC: Fetch full trip detail for dashboard (all joined data in one query)
SELECT
    t.trip_id,
    t.started_at,
    t.ended_at,
    t.actual_fuel_used_l,
    d.full_name                          AS driver_name,
    d.license_no                         AS driver_license,
    v.registration_no                    AS vehicle_reg,
    v.vehicle_type,
    v.fuel_type,
    c.company_name,
    r.origin,
    r.destination,
    r.distance_km,
    r.estimated_emission_kg,
    r.algorithm_used,
    ROUND(SUM(el.co2_kg), 3)             AS actual_co2_kg,
    ROUND(SUM(el.nox_g),  3)             AS actual_nox_g
FROM trip t
         JOIN driver      d  ON d.driver_id  = t.driver_id
         JOIN vehicle     v  ON v.vehicle_id = t.vehicle_id
         JOIN company     c  ON c.company_id = v.company_id
         JOIN route       r  ON r.route_id   = t.route_id
         LEFT JOIN emission_log el ON el.trip_id = t.trip_id
GROUP BY
    t.trip_id, t.started_at, t.ended_at, t.actual_fuel_used_l,
    d.full_name, d.license_no,
    v.registration_no, v.vehicle_type, v.fuel_type,
    c.company_name,
    r.origin, r.destination, r.distance_km,
    r.estimated_emission_kg, r.algorithm_used;


-- UC: Get active driver assignment for a vehicle (1:1 join)
SELECT
    va.assignment_id,
    va.assigned_on,
    d.full_name       AS driver_name,
    d.license_no,
    d.email,
    v.registration_no AS vehicle_reg,
    v.vehicle_type,
    c.company_name
FROM vehicle_assignment va
         JOIN driver  d ON d.driver_id  = va.driver_id
         JOIN vehicle v ON v.vehicle_id = va.vehicle_id
         JOIN company c ON c.company_id = v.company_id
WHERE va.vehicle_id  = 1
  AND va.relieved_on IS NULL;


-- UC: Get all vehicles with no active driver assigned
SELECT
    v.vehicle_id,
    v.registration_no,
    v.vehicle_type,
    v.fuel_type,
    c.company_name
FROM vehicle v
         JOIN company c ON c.company_id = v.company_id
WHERE v.vehicle_id NOT IN (
    SELECT vehicle_id
    FROM vehicle_assignment
    WHERE relieved_on IS NULL
)
ORDER BY v.vehicle_id;


-- UC: Get most eco-friendly route between two locations
SELECT
    route_id,
    origin,
    destination,
    distance_km,
    estimated_emission_kg,
    algorithm_used
FROM route
WHERE origin      = 'Hyderabad'
  AND destination = 'Bangalore'
ORDER BY estimated_emission_kg ASC
    LIMIT 1;


-- UC: Compare estimated vs actual CO2 emission per trip
SELECT
    t.trip_id,
    d.full_name              AS driver_name,
    r.origin,
    r.destination,
    r.estimated_emission_kg,
    ROUND(SUM(el.co2_kg), 3) AS actual_co2_kg,
    ROUND(SUM(el.co2_kg) - r.estimated_emission_kg, 3) AS difference_kg
FROM trip t
         JOIN driver      d  ON d.driver_id = t.driver_id
         JOIN route       r  ON r.route_id  = t.route_id
         JOIN emission_log el ON el.trip_id = t.trip_id
GROUP BY t.trip_id, d.full_name, r.origin, r.destination, r.estimated_emission_kg
ORDER BY difference_kg DESC;


-- =============================================================
-- SECTION 4: STATISTIC QUERIES
-- =============================================================

-- Stat 1: Total trips, distance and fuel used per driver
SELECT
    d.driver_id,
    d.full_name,
    COUNT(t.trip_id)                  AS total_trips,
    ROUND(SUM(r.distance_km), 2)      AS total_distance_km,
    ROUND(SUM(t.actual_fuel_used_l), 2) AS total_fuel_used_l,
    ROUND(SUM(el.co2_kg), 3)          AS total_co2_kg
FROM driver d
         LEFT JOIN trip         t  ON t.driver_id = d.driver_id
         LEFT JOIN route        r  ON r.route_id  = t.route_id
         LEFT JOIN emission_log el ON el.trip_id  = t.trip_id
GROUP BY d.driver_id, d.full_name
ORDER BY total_trips DESC;


-- Stat 2: Emission summary per company
SELECT
    c.company_id,
    c.company_name,
    COUNT(DISTINCT v.vehicle_id)  AS total_vehicles,
    COUNT(DISTINCT t.trip_id)     AS total_trips,
    ROUND(SUM(el.co2_kg), 3)      AS total_co2_kg,
    ROUND(AVG(el.co2_kg), 3)      AS avg_co2_per_log
FROM company c
         LEFT JOIN vehicle      v  ON v.company_id = c.company_id
         LEFT JOIN trip         t  ON t.vehicle_id = v.vehicle_id
         LEFT JOIN emission_log el ON el.trip_id   = t.trip_id
GROUP BY c.company_id, c.company_name
ORDER BY total_co2_kg DESC;


-- Stat 3: Route usage statistics
SELECT
    r.route_id,
    r.origin,
    r.destination,
    r.algorithm_used,
    COUNT(t.trip_id)                    AS times_used,
    ROUND(AVG(t.actual_fuel_used_l), 2) AS avg_fuel_used_l,
    ROUND(AVG(el.co2_kg), 3)            AS avg_co2_per_log
FROM route r
         LEFT JOIN trip         t  ON t.route_id = r.route_id
         LEFT JOIN emission_log el ON el.trip_id = t.trip_id
GROUP BY r.route_id, r.origin, r.destination, r.algorithm_used
ORDER BY times_used DESC;


-- Stat 4: Vehicles with total emission and trips count
SELECT
    v.vehicle_id,
    v.registration_no,
    v.vehicle_type,
    v.fuel_type,
    c.company_name,
    COUNT(DISTINCT t.trip_id)     AS total_trips,
    ROUND(SUM(el.co2_kg), 3)      AS total_co2_kg
FROM vehicle v
         JOIN company c ON c.company_id = v.company_id
         LEFT JOIN trip         t  ON t.vehicle_id = v.vehicle_id
         LEFT JOIN emission_log el ON el.trip_id   = t.trip_id
GROUP BY v.vehicle_id, v.registration_no, v.vehicle_type, v.fuel_type, c.company_name
ORDER BY total_co2_kg DESC;


-- =============================================================
-- SECTION 5: TOP-SOMETHING QUERIES
-- =============================================================

-- Top 5 drivers with highest total CO2 emissions
SELECT
    d.driver_id,
    d.full_name,
    COUNT(t.trip_id)         AS total_trips,
    ROUND(SUM(el.co2_kg), 3) AS total_co2_kg
FROM driver d
         JOIN trip          t  ON t.driver_id = d.driver_id
         JOIN emission_log  el ON el.trip_id  = t.trip_id
GROUP BY d.driver_id, d.full_name
ORDER BY total_co2_kg DESC
    LIMIT 5;


-- Top 5 most fuel-efficient routes (lowest fuel per km)
SELECT
    r.route_id,
    r.origin,
    r.destination,
    r.distance_km,
    r.algorithm_used,
    ROUND(AVG(t.actual_fuel_used_l), 2)                              AS avg_fuel_l,
    ROUND(AVG(t.actual_fuel_used_l) / NULLIF(r.distance_km, 0), 4)  AS fuel_per_km
FROM route r
         JOIN trip t ON t.route_id = r.route_id
WHERE t.actual_fuel_used_l IS NOT NULL
GROUP BY r.route_id, r.origin, r.destination, r.distance_km, r.algorithm_used
ORDER BY fuel_per_km ASC
    LIMIT 5;


-- Top 3 companies by total number of trips
SELECT
    c.company_id,
    c.company_name,
    COUNT(t.trip_id) AS total_trips
FROM company c
         JOIN vehicle v ON v.company_id = c.company_id
         JOIN trip    t ON t.vehicle_id = v.vehicle_id
GROUP BY c.company_id, c.company_name
ORDER BY total_trips DESC
    LIMIT 3;


-- Top 5 most used routes (highest trip count)
SELECT
    r.route_id,
    r.origin,
    r.destination,
    r.algorithm_used,
    COUNT(t.trip_id) AS total_trips
FROM route r
         JOIN trip t ON t.route_id = r.route_id
GROUP BY r.route_id, r.origin, r.destination, r.algorithm_used
ORDER BY total_trips DESC
    LIMIT 5;


-- =============================================================
-- SECTION 6: ALL USE-CASE QUERIES (OOD MODULE COVERAGE)
-- =============================================================

-- 1: Register a new company
INSERT INTO company (company_name, industry)
VALUES ('EcoFleet India', 'Logistics');

-- 2: Register a vehicle under a company
INSERT INTO vehicle (company_id, registration_no, vehicle_type, fuel_capacity_l, fuel_type)
VALUES (1, 'TS12GH3456', 'van', 75.00, 'diesel');

-- 3: Register a new driver under a company
INSERT INTO driver (company_id, full_name, license_no, email)
VALUES (1, 'Sneha Patel', 'DL-2025-112233', 'sneha.patel@fleet.com');

-- 4: Assign a driver to a vehicle
INSERT INTO vehicle_assignment (vehicle_id, driver_id, assigned_on)
VALUES (2, 3, CURRENT_DATE);

-- 5: Relieve a driver from a vehicle
UPDATE vehicle_assignment
SET relieved_on = CURRENT_DATE
WHERE vehicle_id  = 2
  AND relieved_on IS NULL;

-- 6: Store a computed route
INSERT INTO route (origin, destination, distance_km, estimated_emission_kg, algorithm_used)
VALUES ('Hyderabad', 'Mumbai', 710.00, 165.400, 'astar');

-- 7: Start a new trip
INSERT INTO trip (driver_id, vehicle_id, route_id, started_at)
VALUES (2, 2, 2, NOW());

-- 8: Complete a trip
UPDATE trip
SET ended_at           = NOW(),
    actual_fuel_used_l = 52.30
WHERE trip_id = 2;

-- 9: Log emission after a trip
INSERT INTO emission_log (trip_id, co2_kg, nox_g)
VALUES (2, 33.200, 5.100);

-- 10: Get all available vehicles (no active driver)
SELECT
    v.vehicle_id,
    v.registration_no,
    v.vehicle_type,
    v.fuel_type,
    c.company_name
FROM vehicle v
         JOIN company c ON c.company_id = v.company_id
WHERE v.vehicle_id NOT IN (
    SELECT vehicle_id
    FROM vehicle_assignment
    WHERE relieved_on IS NULL
)
ORDER BY v.vehicle_id;

-- 11: Get all ongoing trips
SELECT
    t.trip_id,
    d.full_name       AS driver_name,
    v.registration_no AS vehicle_reg,
    r.origin,
    r.destination,
    t.started_at
FROM trip t
         JOIN driver  d ON d.driver_id  = t.driver_id
         JOIN vehicle v ON v.vehicle_id = t.vehicle_id
         JOIN route   r ON r.route_id   = t.route_id
WHERE t.ended_at IS NULL
ORDER BY t.started_at ASC;

-- 12: Find most eco-friendly route between two cities
SELECT
    route_id,
    origin,
    destination,
    distance_km,
    estimated_emission_kg,
    algorithm_used
FROM route
WHERE origin      = 'Hyderabad'
  AND destination = 'Mumbai'
ORDER BY estimated_emission_kg ASC
    LIMIT 1;

-- 13: Get full emission history for a vehicle
SELECT
    t.trip_id,
    t.started_at,
    t.ended_at,
    el.co2_kg,
    el.nox_g,
    el.recorded_at,
    r.origin,
    r.destination
FROM emission_log el
         JOIN trip  t ON t.trip_id  = el.trip_id
         JOIN route r ON r.route_id = t.route_id
WHERE t.vehicle_id = 1
ORDER BY el.recorded_at DESC;

-- 14: Compare estimated vs actual emission per trip
SELECT
    t.trip_id,
    r.origin,
    r.destination,
    r.estimated_emission_kg,
    ROUND(SUM(el.co2_kg), 3)   AS actual_co2_kg,
    ROUND(SUM(el.co2_kg) - r.estimated_emission_kg, 3)   AS difference_kg
FROM trip t
         JOIN route         r  ON r.route_id = t.route_id
         JOIN emission_log  el ON el.trip_id = t.trip_id
GROUP BY t.trip_id, r.origin, r.destination, r.estimated_emission_kg
ORDER BY difference_kg DESC;

-- 15: Get drivers with no trips assigned
SELECT
    d.driver_id,
    d.full_name,
    d.license_no,
    c.company_name
FROM driver d
         JOIN company c ON c.company_id = d.company_id
         LEFT JOIN trip t ON t.driver_id = d.driver_id
WHERE t.trip_id IS NULL
ORDER BY d.driver_id;