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

INSERT INTO company (company_id, company_name, industry, created_at) VALUES
                                                                         (1, 'Hyderabad Logistics Pvt Ltd', 'Logistics',       '2026-01-10 09:00:00'),
                                                                         (2, 'GreenMove Transport',         'Transportation',   '2026-02-15 10:30:00'),

                                                                         (3, 'EcoFleet India',              'Supply Chain',     '2026-03-20 11:00:00'),
                                                                         (4, 'SwiftCargo Solutions',        'Logistics',        '2026-03-05 08:45:00'),
                                                                         (5, 'BlueLine Deliveries',         'E-Commerce',       '2026-02-12 14:00:00');


-- =============================================================
-- 2. VEHICLE
-- =============================================================

INSERT INTO vehicle (vehicle_id, company_id, registration_no, vehicle_type, fuel_capacity_l, fuel_type) VALUES
-- Company 1 vehicles
(1,  1, 'TS09AB1234', 'sedan',  60.00, 'petrol'),
(2,  1, 'TS09CD5678', 'van',    80.00, 'diesel'),
(3,  1, 'TS09EF9012', 'truck', 120.00, 'diesel'),

-- Company 2 vehicles
(4,  2, 'TS10GH3456', 'sedan',   0.00, 'electric'),
(5,  2, 'TS10IJ7890', 'van',     0.00, 'electric'),

-- Company 3 vehicles
(6,  3, 'TS11KL1234', 'van',    75.00, 'diesel'),
(7,  3, 'TS11MN5678', 'truck', 110.00, 'diesel'),

-- Company 4 vehicles
(8,  4, 'TS12OP9012', 'sedan',  55.00, 'petrol'),
(9,  4, 'TS12QR3456', 'van',    85.00, 'diesel'),

-- Company 5 vehicles
(10, 5, 'TS13ST7890', 'sedan',   0.00, 'electric');


-- =============================================================
-- 3. DRIVER
-- =============================================================

INSERT INTO driver (driver_id, company_id, full_name, license_no, email) VALUES
-- Company 1 drivers
(1,  1, 'Ravi Kumar',     'DL-2024-001234', 'ravi.kumar@fleet.com'),
(2,  1, 'Priya Sharma',   'DL-2024-005678', 'priya.sharma@fleet.com'),
(3,  1, 'Suresh Reddy',   'DL-2024-009012', 'suresh.reddy@fleet.com'),

-- Company 2 drivers
(4,  2, 'Arjun Nair',     'DL-2024-013456', 'arjun.nair@greenmove.com'),
(5,  2, 'Sneha Patel',    'DL-2024-017890', 'sneha.patel@greenmove.com'),

-- Company 3 drivers
(6,  3, 'Mohammed Ali',   'DL-2024-021234', 'mohammed.ali@ecofleet.com'),
(7,  3, 'Divya Menon',    'DL-2024-025678', 'divya.menon@ecofleet.com'),

-- Company 4 drivers
(8,  4, 'Kiran Joshi',    'DL-2024-029012', 'kiran.joshi@swiftcargo.com'),
(9,  4, 'Anita Singh',    'DL-2024-033456', 'anita.singh@swiftcargo.com'),

-- Company 5 drivers
(10, 5, 'Rahul Verma',    'DL-2024-037890', 'rahul.verma@blueline.com');


-- =============================================================
-- 4. VEHICLE ASSIGNMENT
-- =============================================================
-- relieved_on = NULL means currently active assignment

INSERT INTO vehicle_assignment (assignment_id, vehicle_id, driver_id, assigned_on, relieved_on) VALUES
-- Active assignments
(1,  1,  1, '2024-06-01', NULL),       -- Ravi Kumar     → sedan  TS09AB1234 (active)
(2,  2,  2, '2024-06-01', NULL),       -- Priya Sharma   → van    TS09CD5678 (active)
(3,  3,  3, '2024-06-01', NULL),       -- Suresh Reddy   → truck  TS09EF9012 (active)
(4,  4,  4, '2024-06-15', NULL),       -- Arjun Nair     → sedan  TS10GH3456 (active)
(5,  5,  5, '2024-06-15', NULL),       -- Sneha Patel    → van    TS10IJ7890 (active)
(6,  6,  6, '2024-07-01', NULL),       -- Mohammed Ali   → van    TS11KL1234 (active)
(7,  8,  8, '2024-07-10', NULL),       -- Kiran Joshi    → sedan  TS12OP9012 (active)
(8,  10, 10,'2024-08-01', NULL),       -- Rahul Verma    → sedan  TS13ST7890 (active)

-- Historical assignments (relieved)
(9,  1,  2, '2024-01-01', '2024-05-31'), -- Priya was on vehicle 1 before Ravi
(10, 4,  5, '2024-03-01', '2024-06-14'); -- Sneha was on vehicle 4 before Arjun

-- Note: vehicle 7, 9 have no active driver (unassigned — useful for UC10 query)


-- =============================================================
-- 5. ROUTE
-- =============================================================

INSERT INTO route (route_id, origin, destination, distance_km, estimated_emission_kg, algorithm_used) VALUES
-- Hyderabad → Bangalore (3 algorithm variants)
(1,  'Hyderabad', 'Bangalore',  570.00, 132.500, 'dijkstra'),
(2,  'Hyderabad', 'Bangalore',  610.00, 118.200, 'astar'),
(3,  'Hyderabad', 'Bangalore',  595.00, 125.800, 'greedy'),

-- Hyderabad → Chennai
(4,  'Hyderabad', 'Chennai',    630.00, 145.800, 'dijkstra'),
(5,  'Hyderabad', 'Chennai',    650.00, 138.400, 'astar'),

-- Hyderabad → Mumbai
(6,  'Hyderabad', 'Mumbai',     710.00, 165.400, 'astar'),
(7,  'Hyderabad', 'Mumbai',     740.00, 172.100, 'dijkstra'),

-- Bangalore → Mumbai
(8,  'Bangalore', 'Mumbai',     980.00, 225.300, 'dijkstra'),
(9,  'Bangalore', 'Mumbai',     960.00, 210.700, 'astar'),

-- Hyderabad → Delhi
(10, 'Hyderabad', 'Delhi',     1500.00, 348.600, 'dijkstra'),
(11, 'Hyderabad', 'Delhi',     1480.00, 332.900, 'astar'),

-- Chennai → Bangalore
(12, 'Chennai',   'Bangalore',  340.00,  78.500, 'dijkstra'),
(13, 'Chennai',   'Bangalore',  350.00,  72.300, 'astar');


-- =============================================================
-- 6. TRIP
-- =============================================================

INSERT INTO trip (trip_id, driver_id, vehicle_id, route_id, started_at, ended_at, actual_fuel_used_l) VALUES
-- Completed trips
(1,  1,  1,  1,  '2026-01-05 08:00:00', '2026-01-05 16:30:00', 48.50),
(2,  2,  2,  4,  '2026-01-06 07:30:00', '2026-01-06 17:00:00', 63.20),
(3,  3,  3,  6,  '2026-01-07 06:00:00', '2026-01-07 20:00:00', 95.40),
(4,  4,  4,  2,  '2026-01-08 09:00:00', '2026-01-08 17:30:00', NULL),   -- electric, no fuel
(5,  5,  5,  5,  '2026-01-09 08:00:00', '2026-01-09 18:00:00', NULL),   -- electric, no fuel
(6,  6,  6,  4,  '2026-01-10 07:00:00', '2026-01-10 17:30:00', 58.70),
(7,  1,  1,  3,  '2026-01-15 08:00:00', '2026-01-15 16:00:00', 46.80),
(8,  2,  2,  8,  '2026-01-20 06:00:00', '2026-01-20 22:00:00', 88.30),
(9,  8,  8,  12, '2026-02-01 09:00:00', '2026-02-01 15:00:00', 32.10),
(10, 10, 10, 2,  '2026-02-05 08:30:00', '2026-02-05 17:00:00', NULL),   -- electric
(11, 3,  3,  10, '2026-02-10 05:00:00', '2026-02-11 09:00:00', 145.60),
(12, 6,  6,  5,  '2026-02-15 07:00:00', '2026-02-15 17:30:00', 61.40),
(13, 1,  1,  1,  '2026-03-01 08:00:00', '2026-03-01 16:30:00', 49.20),
(14, 4,  4,  9,  '2026-03-05 07:00:00', '2026-03-05 21:00:00', NULL),   -- electric
(15, 2,  2,  7,  '2026-03-10 06:00:00', '2026-03-10 20:00:00', 79.80),

-- Ongoing trips (no ended_at)
(16, 7,  7,  6,  '2026-03-24 07:00:00', NULL, NULL),
(17, 9,  9,  11, '2026-03-24 06:00:00', NULL, NULL);


-- =============================================================
-- 7. EMISSION LOG
-- =============================================================

INSERT INTO emission_log (log_id, trip_id, co2_kg, nox_g, recorded_at) VALUES
-- Trip 1 logs
(1,  1,  28.750,  4.200, '2026-01-05 16:30:00'),
(2,  1,  19.500,  3.100, '2026-01-05 12:00:00'),

-- Trip 2 logs
(3,  2,  35.200,  5.400, '2026-01-06 17:00:00'),
(4,  2,  28.100,  4.200, '2026-01-06 12:00:00'),

-- Trip 3 logs
(5,  3,  52.300,  8.100, '2026-01-07 20:00:00'),
(6,  3,  43.100,  6.800, '2026-01-07 13:00:00'),

-- Trip 4 — electric vehicle (very low emissions)
(7,  4,   2.100,  0.100, '2026-01-08 17:30:00'),

-- Trip 5 — electric vehicle
(8,  5,   2.400,  0.120, '2026-01-09 18:00:00'),

-- Trip 6 logs
(9,  6,  31.400,  4.800, '2026-01-10 17:30:00'),
(10, 6,  27.300,  3.900, '2026-01-10 12:00:00'),

-- Trip 7 logs
(11, 7,  26.900,  3.800, '2026-01-15 16:00:00'),
(12, 7,  19.900,  2.900, '2026-01-15 11:00:00'),

-- Trip 8 logs
(13, 8,  48.200,  7.200, '2026-01-20 22:00:00'),
(14, 8,  40.100,  5.900, '2026-01-20 14:00:00'),

-- Trip 9 logs
(15, 9,  18.500,  2.800, '2026-02-01 15:00:00'),


-- Trip 10 — electric
(16, 10,  1.900,  0.090, '2026-02-05 17:00:00'),

-- Trip 11 logs (long Hyderabad → Delhi trip)
(17, 11, 82.300, 12.500, '2026-02-11 09:00:00'),
(18, 11, 63.300,  9.800, '2026-02-10 18:00:00'),

-- Trip 12 logs
(19, 12, 33.100,  4.900, '2026-02-15 17:30:00'),
(20, 12, 28.300,  4.100, '2026-02-15 12:00:00'),

-- Trip 13 logs
(21, 13, 29.100,  4.300, '2026-03-01 16:30:00'),
(22, 13, 20.100,  3.200, '2026-03-01 12:00:00'),

-- Trip 14 — electric
(23, 14,  3.200,  0.150, '2026-03-05 21:00:00'),

-- Trip 15 logs
(24, 15, 43.800,  6.600, '2026-03-10 20:00:00'),
(25, 15, 36.000,  5.400, '2026-03-10 13:00:00');

-- Note: Trips 16 and 17 are ongoing — no emission logs yet


