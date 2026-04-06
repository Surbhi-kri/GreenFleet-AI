-- Bulk load script for indexing benchmarks (PostgreSQL)
-- Target: couple million rows in route table

INSERT INTO route (from_city, to_city, path, total_distance, total_fuel_l, total_emission, algorithm_used, calculated_at)
SELECT
  'City_' || (g % 1000),
  'City_' || ((g + 7) % 1000),
  'City_' || (g % 1000) || ' -> City_' || ((g + 7) % 1000),
  (50 + (g % 950))::numeric(8,2),
  (3 + (g % 70) / 10.0)::numeric(8,2),
  (1 + (g % 300) / 5.0)::numeric(8,3),
  CASE WHEN g % 2 = 0 THEN 'FASTEST' ELSE 'ECO_FRIENDLY' END,
  NOW() - (g % 3650) * INTERVAL '1 day'
FROM generate_series(1, 2000000) g;

ANALYZE route;
