# DataSource vs DriverManager

## What They Are

- `DriverManager`: static factory approach (`DriverManager.getConnection(...)`) to create JDBC connections.
- `DataSource`: an object-based connection factory, usually injected/configured once and reused.

## Key Differences

- Lifecycle:
  - `DriverManager`: connection creation is typically done ad hoc in code.
  - `DataSource`: connection creation policy is centralized in one component.
- Configuration:
  - `DriverManager`: URL/user/password are often hardcoded or loaded manually.
  - `DataSource`: cleaner external configuration (properties, container, framework).
- Pooling:
  - `DriverManager`: no built-in pooling.
  - `DataSource`: often combined with connection pools (`HikariCP`, app server pools).
- Testability:
  - `DriverManager`: tests may require direct URL switching in code.
  - `DataSource`: easier to swap implementations per environment.

## Advantages and Disadvantages

### DriverManager

Advantages:
- Very simple for small console projects.
- Minimal setup.

Disadvantages:
- No pooling by default.
- Harder to scale and tune.
- Encourages scattered connection creation across classes.

### DataSource

Advantages:
- Better separation of concerns.
- Easier production tuning (pool size, timeout, metrics).
- Cleaner environment switching (dev/test/prod).

Disadvantages:
- Slightly more setup.
- May feel heavy for tiny or one-off programs.

## Project Note

Current route subsystem still uses `DriverManager` through `DatabaseUtil`.
For production-grade scaling, moving `DatabaseUtil` to a pooled `DataSource` is the next logical improvement.

