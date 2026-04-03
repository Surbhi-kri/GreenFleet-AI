# Connection Pooling Findings (Route Subsystem)

## Scope
Connection pooling is configured only for the Route subsystem.
Other modules continue with existing project setup.

## Why pooling is needed
- Opening DB connections is expensive.
- Under concurrent workload, repeatedly creating connections increases latency.
- Pooling keeps reusable open connections and improves throughput.

## Advantages
- Lower latency under load.
- Better throughput for concurrent requests.
- Configurable limits and timeouts.

## Disadvantages
- Extra dependency and configuration.
- Incorrect pool size can hurt performance.
- Connection leaks become more dangerous if close discipline is weak.

## Workload
- Multi-threaded benchmark using `SELECT pg_sleep(?)`.
- Compared:
  - single cached connection (`SingleConnectionDataSource`)
  - pooled datasource (`HikariCP`)

## Result Template
| Threads | Sleep(s) | Single DS total(ms) | Pooled DS total(ms) |
|---|---:|---:|---:|
| 10 | 2 | _fill after run_ | _fill after run_ |

## Interpretation
- Single cached connection serializes work and total time grows roughly with thread count.
- Pooling allows parallel query execution up to pool size, reducing total time.

## Notes
- Route subsystem now supports `RouteService(DataSource)` constructor.
- App uses pooled datasource for route subsystem only.
