-- ─── 1. company ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS company (
                                       company_id   BIGSERIAL    PRIMARY KEY,
                                       company_name VARCHAR(255) NOT NULL,
    industry     VARCHAR(100),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
    );


-- ─── 2. vehicle ─────────────────────────────────────────────
-- ONE company owns MANY vehicles  →  One-to-Many
CREATE TABLE IF NOT EXISTS vehicle (
                                       vehicle_id       BIGSERIAL    PRIMARY KEY,
                                       company_id       BIGINT       NOT NULL
                                       REFERENCES company(company_id)
    ON DELETE CASCADE,
    registration_no  VARCHAR(50)  NOT NULL UNIQUE,
    vehicle_type     VARCHAR(50)  NOT NULL,
    fuel_capacity_l  NUMERIC(6,2) NOT NULL,
    fuel_type        VARCHAR(30)  NOT NULL
    );


-- ─── 3. driver ──────────────────────────────────────────────
-- ONE company employs MANY drivers  →  One-to-Many
CREATE TABLE IF NOT EXISTS driver (
                                      driver_id   BIGSERIAL    PRIMARY KEY,
                                      company_id  BIGINT       NOT NULL
                                      REFERENCES company(company_id)
    ON DELETE CASCADE,
    full_name   VARCHAR(255) NOT NULL,
    license_no  VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE
    );


-- ─── 4. vehicle_assignment ──────────────────────────────────
-- ONE vehicle has ONE active driver at a time  →  One-to-One
-- relieved_on = NULL means assignment is currently active
-- The UNIQUE constraint on (vehicle_id, relieved_on) where
-- relieved_on IS NULL enforces only one active driver per vehicle
CREATE TABLE IF NOT EXISTS vehicle_assignment (
                                                  assignment_id  BIGSERIAL  PRIMARY KEY,
                                                  vehicle_id     BIGINT     NOT NULL
                                                  REFERENCES vehicle(vehicle_id)
    ON DELETE CASCADE,
    driver_id      BIGINT     NOT NULL
    REFERENCES driver(driver_id)
    ON DELETE CASCADE,
    assigned_on    DATE       NOT NULL DEFAULT CURRENT_DATE,
    relieved_on    DATE,
    CONSTRAINT uq_one_active_driver_per_vehicle
    UNIQUE (vehicle_id, relieved_on)
    );


-- ─── 5. route ───────────────────────────────────────────────
-- Stores computed routes (output of Strategy Pattern)
CREATE TABLE IF NOT EXISTS route (
                                     route_id               BIGSERIAL     PRIMARY KEY,
                                     origin                 VARCHAR(255)  NOT NULL,
    destination            VARCHAR(255)  NOT NULL,
    distance_km            NUMERIC(8,2)  NOT NULL,
    estimated_emission_kg  NUMERIC(8,3)  NOT NULL,
    algorithm_used         VARCHAR(50)   NOT NULL
    );


-- ─── 6. trip ────────────────────────────────────────────────
-- MANY drivers can take MANY routes  →  Many-to-Many junction table
-- A trip = one driver, one vehicle, one route, on one date
CREATE TABLE IF NOT EXISTS trip (
                                    trip_id             BIGSERIAL    PRIMARY KEY,
                                    driver_id           BIGINT       NOT NULL
                                    REFERENCES driver(driver_id)
    ON DELETE RESTRICT,
    vehicle_id          BIGINT       NOT NULL
    REFERENCES vehicle(vehicle_id)
    ON DELETE RESTRICT,
    route_id            BIGINT       NOT NULL
    REFERENCES route(route_id)
    ON DELETE RESTRICT,
    started_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    ended_at            TIMESTAMP,
    actual_fuel_used_l  NUMERIC(6,2)
    );


-- ─── 7. emission_log ────────────────────────────────────────
-- ONE trip produces MANY emission records  →  One-to-Many
CREATE TABLE IF NOT EXISTS emission_log (
                                            log_id       BIGSERIAL    PRIMARY KEY,
                                            trip_id      BIGINT       NOT NULL
                                            REFERENCES trip(trip_id)
    ON DELETE CASCADE,
    co2_kg       NUMERIC(8,3) NOT NULL,
    nox_g        NUMERIC(8,3),
    recorded_at  TIMESTAMP    NOT NULL DEFAULT NOW()
    );


-- ============================================================
-- INDEXES
-- Created on all FK columns used in frequent queries.
-- IF NOT EXISTS = safe to re-run without errors.
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_vehicle_company    ON vehicle(company_id);
CREATE INDEX IF NOT EXISTS idx_driver_company     ON driver(company_id);
CREATE INDEX IF NOT EXISTS idx_assignment_vehicle ON vehicle_assignment(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_assignment_driver  ON vehicle_assignment(driver_id);
CREATE INDEX IF NOT EXISTS idx_trip_driver        ON trip(driver_id);
CREATE INDEX IF NOT EXISTS idx_trip_vehicle       ON trip(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_trip_route         ON trip(route_id);
CREATE INDEX IF NOT EXISTS idx_emission_trip      ON emission_log(trip_id);


-- ============================================================
-- SEED DATA (optional — useful for testing from day one)
-- ============================================================

INSERT INTO company (company_name, industry)
VALUES ('Tata Logistics', 'Logistics'),
       ('GreenMove Corp', 'Transportation')
    ON CONFLICT DO NOTHING;

INSERT INTO vehicle (company_id, registration_no, vehicle_type, fuel_capacity_l, fuel_type)
VALUES (1, 'MH12AB1234', 'truck',  80.0, 'diesel'),
       (1, 'MH12CD5678', 'van',    50.0, 'petrol'),
       (2, 'KA01EF9999', 'sedan',  45.0, 'electric')
    ON CONFLICT DO NOTHING;

INSERT INTO driver (company_id, full_name, license_no, email)
VALUES (1, 'Ravi Kumar',   'DL-1234567', 'ravi@tatalogistics.com'),
       (1, 'Priya Sharma', 'DL-7654321', 'priya@tatalogistics.com'),
       (2, 'Amit Singh',   'DL-1122334', 'amit@greenmove.com')
    ON CONFLICT DO NOTHING;