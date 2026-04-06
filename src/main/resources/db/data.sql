TRUNCATE TABLE emission_log,
               trip,
               route,
               vehicle_assignment,
               driver,
               vehicle,
               company
RESTART IDENTITY CASCADE;
-- =============================================================
-- 1. COMPANY
-- =============================================================

-- ── Seed: companies ───────────────────────────────────────────
INSERT INTO company (company_name, industry) VALUES
                                                 ('Tata Logistics',  'Logistics'),
                                                 ('GreenMove Corp',  'Transportation')
    ON CONFLICT DO NOTHING;
-- =============================================================
-- 2. VEHICLE
-- =============================================================

-- ── Seed: vehicles ────────────────────────────────────────────
INSERT INTO vehicle (company_id, registration_no, vehicle_type, fuel_efficiency_kmpl, fuel_type) VALUES
                                                                                                     (1, 'MH12AB1234', 'truck',  8.0,  'diesel'),
                                                                                                     (1, 'MH12CD5678', 'van',   12.0,  'petrol'),
                                                                                                     (2, 'KA01EF9999', 'sedan', 15.0,  'petrol'),
                                                                                                     (2, 'KA01GH1111', 'truck',  0.0,  'electric')
    ON CONFLICT DO NOTHING;
-- =============================================================
-- 3. DRIVER
-- =============================================================

INSERT INTO driver (driver_id,company_id, full_name, license_no, email)
VALUES(1, 1, 'Ravi Kumar', 'LIC001', 'ravi@mail.com'),
                                                                              (2, 1, 'Priya Sharma', 'LIC002', 'priya@mail.com'),
                                                                              (3, 1, 'Suresh Reddy', 'LIC003', 'suresh@mail.com'),
                                                                              (4, 1, 'Arjun Nair', 'LIC004', 'arjun@mail.com'),
                                                                              (5, 1, 'Sneha Patel', 'LIC005', 'sneha@mail.com'),
                                                                              (6, 1, 'Mohammed Ali', 'LIC006', 'ali@mail.com'),
                                                                              (8, 1, 'Kiran Joshi', 'LIC008', 'kiran@mail.com'),
                                                                              (10,1, 'Rahul Verma', 'LIC010', 'rahul@mail.com')
ON CONFLICT DO NOTHING;
INSERT INTO route
(from_city, to_city, path, total_distance, total_fuel_l, total_emission, algorithm_used)
VALUES

-- Hyderabad → Bangalore
('Hyderabad', 'Bangalore', 'Hyderabad → ORR → NH44 → Bangalore', 570, 48.5, 132.5, 'dijkstra'),
('Hyderabad', 'Bangalore', 'Hyderabad → Expressway → Bangalore', 550, 45.2, 118.2, 'astar'),

-- Hyderabad → Chennai
('Hyderabad', 'Chennai', 'Hyderabad → NH205 → Chennai', 630, 52.3, 145.8, 'dijkstra'),
('Hyderabad', 'Chennai', 'Hyderabad → Coastal Route → Chennai', 650, 50.0, 138.4, 'astar'),

-- Hyderabad → Mumbai
('Hyderabad', 'Mumbai', 'Hyderabad → NH65 → Mumbai', 710, 60.2, 165.4, 'dijkstra'),
('Hyderabad', 'Mumbai', 'Hyderabad → Expressway → Mumbai', 740, 58.5, 160.1, 'astar'),

-- Bangalore → Mumbai
('Bangalore', 'Mumbai', 'Bangalore → NH48 → Mumbai', 980, 80.5, 225.3, 'dijkstra'),
('Bangalore', 'Mumbai', 'Bangalore → Expressway → Mumbai', 960, 78.0, 210.7, 'astar')
    ON CONFLICT DO NOTHING;


-- =============================================================
-- 4. VEHICLE ASSIGNMENT
-- =============================================================
-- relieved_on = NULL means currently active assignment

INSERT INTO vehicle_assignment (assignment_id, vehicle_id, driver_id, assigned_on, relieved_on) VALUES
-- Active assignments
(1,  1,  1, '2024-06-01', NULL),       -- Ravi Kumar     → sedan  TS09AB1234 (active)
(2,  2,  2, '2024-06-01', NULL),       -- Priya Sharma   → van    TS09CD5678 (active)
(3,  3,  3, '2024-06-01', NULL),       -- Suresh Reddy   → truck  TS09EF9012 (active)
(4,  4,  4, '2024-06-15', NULL)      -- Arjun Nair     → sedan  TS10GH3456 (active)
ON CONFLICT DO NOTHING;
-- Note: vehicle 7, 9 have no active driver (unassigned — useful for UC10 query)


-- =============================================================
-- 5. ROUTE
-- =============================================================

-- INSERT INTO road (from_city, to_city, distance_km, road_type, traffic_level, speed_limit) VALUES
--
-- -- Mumbai → Pune
-- ('Mumbai', 'Pune',    148, 'EXPRESSWAY', 'HIGH',   120),
-- ('Mumbai', 'Pune',    155, 'HIGHWAY',    'MEDIUM',  80),
--
-- -- Mumbai → Nagpur
-- ('Mumbai', 'Nagpur',  820, 'HIGHWAY',    'LOW',     80),
-- ('Mumbai', 'Nagpur',  800, 'EXPRESSWAY', 'HIGH',   120),
--
-- -- Pune → Hyderabad
-- ('Pune', 'Hyderabad', 560, 'HIGHWAY',    'LOW',     80),
-- ('Pune', 'Hyderabad', 545, 'EXPRESSWAY', 'HIGH',   120),
--
-- -- Nagpur → Delhi
-- ('Nagpur', 'Delhi',   580, 'HIGHWAY',    'LOW',     80),
-- ('Nagpur', 'Delhi',   560, 'EXPRESSWAY', 'HIGH',   120),
--
-- -- Nagpur → Hyderabad
-- ('Nagpur', 'Hyderabad', 500, 'HIGHWAY',    'MEDIUM',  80),
-- ('Nagpur', 'Hyderabad', 480, 'EXPRESSWAY', 'HIGH',   120),
--
-- -- Hyderabad → Bangalore
-- ('Hyderabad', 'Bangalore', 570, 'HIGHWAY',    'MEDIUM',  80),
-- ('Hyderabad', 'Bangalore', 550, 'EXPRESSWAY', 'HIGH',   120),
--
-- -- Delhi → Bangalore
-- ('Delhi', 'Bangalore', 1200, 'HIGHWAY',    'LOW',    80),
-- ('Delhi', 'Bangalore', 1180, 'EXPRESSWAY', 'HIGH',  120),
--
-- -- Delhi → Hyderabad
-- ('Delhi', 'Hyderabad', 1500, 'HIGHWAY',    'LOW',    80),
-- ('Delhi', 'Hyderabad', 1480, 'EXPRESSWAY', 'HIGH',  120)
INSERT INTO road (from_city, to_city, distance_km, road_type, traffic_level, speed_limit) VALUES

-- Mumbai → Pune
('Mumbai', 'Pune',    148, 'EXPRESSWAY', 'HIGH',   120),
('Mumbai', 'Pune',    155, 'HIGHWAY',    'LOW',     80),

-- Mumbai → Nagpur
('Mumbai', 'Nagpur',  820, 'HIGHWAY',    'LOW',     80),
('Mumbai', 'Nagpur',  800, 'EXPRESSWAY', 'HIGH',   120),

-- Pune → Hyderabad
('Pune', 'Hyderabad', 560, 'HIGHWAY',    'LOW',     80),
('Pune', 'Hyderabad', 545, 'EXPRESSWAY', 'HIGH',   120),

-- Nagpur → Delhi
('Nagpur', 'Delhi',   580, 'HIGHWAY',    'LOW',     80),
('Nagpur', 'Delhi',   560, 'EXPRESSWAY', 'HIGH',   120),

-- Nagpur → Hyderabad
('Nagpur', 'Hyderabad', 500, 'HIGHWAY',  'HIGH',   80),
('Nagpur', 'Hyderabad', 480, 'EXPRESSWAY','HIGH',  120),

-- Hyderabad → Bangalore
('Hyderabad', 'Bangalore', 570, 'HIGHWAY',   'LOW',    80),
('Hyderabad', 'Bangalore', 550, 'EXPRESSWAY','HIGH',  120),

-- Delhi → Bangalore
('Delhi', 'Bangalore', 1200, 'HIGHWAY',    'LOW',    80),
('Delhi', 'Bangalore', 1180, 'EXPRESSWAY', 'HIGH',  120),

-- Delhi ↔ Hyderabad
-- LOW traffic direct road = FASTEST will prefer this
-- but longer = EcoFriendly will avoid this
('Delhi', 'Hyderabad', 1480, 'EXPRESSWAY', 'LOW',   120),
('Delhi', 'Hyderabad', 1500, 'HIGHWAY',    'MEDIUM', 80)


    ON CONFLICT DO NOTHING;
-- =============================================================
-- 6. TRIP
-- =============================================================

INSERT INTO trip (trip_id, driver_id, vehicle_id, route_id, started_at, ended_at, actual_fuel_used_l) VALUES
-- Completed trips
(1,  1,  1,  1,  '2026-01-05 08:00:00', '2026-01-05 16:30:00', 48.50),
(2,  2,  2,  4,  '2026-01-06 07:30:00', '2026-01-06 17:00:00', 63.20),
(3,  3,  3,  6,  '2026-01-07 06:00:00', '2026-01-07 20:00:00', 95.40),
(4,  4,  4,  2,  '2026-01-08 09:00:00', '2026-01-08 17:30:00', NULL)   -- electric, no fuel
ON CONFLICT DO NOTHING;

-- =============================================================
-- 7. EMISSION LOG
-- =============================================================

INSERT INTO emission_log (log_id, trip_id, co2_kg, nox_g, recorded_at) VALUES
-- Trip 1 logs
(1,  1,  28.750,  4.200, '2026-01-05 16:30:00'),
(2,  1,  19.500,  3.100, '2026-01-05 12:00:00'),

-- Trip 2 logs
(3,  2,  35.200,  5.400, '2026-01-06 17:00:00'),
(4,  2,  28.100,  4.200, '2026-01-06 12:00:00')
    ON CONFLICT DO NOTHING;

