-- Single-column / simple-index benchmark (PostgreSQL)

-- 1) Baseline query without dedicated date index
EXPLAIN ANALYZE
SELECT route_id, from_city, to_city, total_emission
FROM route
WHERE calculated_at >= NOW() - INTERVAL '30 days';

-- 2) Create index
CREATE INDEX IF NOT EXISTS idx_route_calculated_at ON route(calculated_at);
ANALYZE route;

-- 3) Re-run same query
EXPLAIN ANALYZE
SELECT route_id, from_city, to_city, total_emission
FROM route
WHERE calculated_at >= NOW() - INTERVAL '30 days';
