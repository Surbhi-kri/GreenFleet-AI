-- Compound-index benchmark (PostgreSQL)
-- Index order matters: (from_city, to_city, calculated_at)

CREATE INDEX IF NOT EXISTS idx_route_from_to_calc ON route(from_city, to_city, calculated_at);
ANALYZE route;

-- A) Full column set (best match)
EXPLAIN ANALYZE
SELECT route_id, total_distance
FROM route
WHERE from_city = 'City_10'
  AND to_city = 'City_17'
  AND calculated_at >= NOW() - INTERVAL '90 days';

-- B) Left-prefix partial set (still uses index effectively)
EXPLAIN ANALYZE
SELECT route_id, total_distance
FROM route
WHERE from_city = 'City_10'
  AND to_city = 'City_17';

-- C) First column only (can still use index)
EXPLAIN ANALYZE
SELECT route_id, total_distance
FROM route
WHERE from_city = 'City_10';

-- D) Non-left-prefix only (typically poor index usage)
EXPLAIN ANALYZE
SELECT route_id, total_distance
FROM route
WHERE to_city = 'City_17';
