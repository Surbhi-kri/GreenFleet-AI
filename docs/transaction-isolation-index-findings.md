# Transaction, Isolation, and Index Findings (Route/Persistence Subsystem)

## Scope
This practical task is implemented in persistence/DB subsystem only.

## 1) Consistency (C in ACID): with vs without transaction

### Case
A trip creation flow should create:
1. `trip` row
2. related `emission_log` row

If failure happens after step 1 and before step 2:
- Without transaction: trip remains without emission row (inconsistent business state)
- With transaction: both operations rollback together

### Demo class
- `org.example.benchmark.TransactionConsistencyDemo`

### Actual observed behavior
Output:
- `WITHOUT TRANSACTION -> trip_exists=true, emission_exists=false`
- `WITH TRANSACTION -> trip_exists=false, emission_exists=false`

Interpretation:
- Without transaction, partial write happened (inconsistent state).
- With transaction, rollback prevented partial commit.

## 2) Isolation level case

### Case
Non-repeatable read on `route.total_emission`:
- Reader transaction reads value
- Writer transaction commits update
- Reader reads again

### Demo class
- `org.example.benchmark.IsolationLevelDemo`

### Actual observed behavior
Output:
- `READ COMMITTED -> first_read=20.000 second_read=25.000`
- `REPEATABLE READ -> first_read=25.000 second_read=25.000`

Interpretation:
- Default `READ COMMITTED` allowed non-repeatable read.
- `REPEATABLE READ` gave stable repeated read in same transaction.

## 3) Index benchmark (single index)

### Scripts
- `src/main/resources/db/load_big_data.sql`
- `src/main/resources/db/index_benchmark.sql`

### Data volume loaded
- Inserted: 2,000,000 rows into `route`
- Load duration: ~7 seconds

### Query
```sql
SELECT route_id, from_city, to_city, total_emission
FROM route
WHERE calculated_at >= NOW() - INTERVAL '30 days';
```

### Actual results
- Before index: Parallel Seq Scan, `Execution Time: 85.889 ms`
- After `idx_route_calculated_at`: Bitmap Index + Heap Scan, `Execution Time: 3.254 ms`

### Improvement
- ~26.4x faster (`85.889 / 3.254`)

## 4) Compound (multi-column) index benchmark

### Script
- `src/main/resources/db/compound_index_benchmark.sql`

### Index
`(from_city, to_city, calculated_at)` as `idx_route_from_to_calc`

### Actual query observations
1. Full-column filter (`from_city + to_city + calculated_at`)
- Plan: `Index Scan using idx_route_from_to_calc`
- Execution: `0.125 ms`

2. Left-prefix partial filter (`from_city + to_city`)
- Plan: `Index Scan using idx_route_from_to_calc`
- Execution: `4.405 ms`

3. First-column only (`from_city`)
- Plan: `Bitmap Index Scan using idx_route_from_to_calc`
- Execution: `0.903 ms`

4. Non-left-prefix only (`to_city`)
- Plan used `idx_route_cities` (existing index), not the new compound index as primary left-prefix match
- Execution: `6.102 ms`

Interpretation:
- Compound index works best when predicates follow left-prefix order.
- Full predicate match is fastest.
- Non-left-prefix predicates are less effective for the compound index.

## Conclusion
- Transactions are mandatory for multi-step write consistency.
- Isolation level should be selected by anomaly risk, not default blindly.
- Indexes significantly improve targeted query performance at large scale.
- Compound indexes are most effective when query predicates align with left-prefix order.
