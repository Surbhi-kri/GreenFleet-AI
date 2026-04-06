# JDBCExecutor Execute Overloads Comparison

## Context
`JDBCExecutor` in this project exposes two update methods:

- `execute(String query, Object... args)`
- `execute(String query, Consumer<PreparedStatement> binder)`

Both are valid and both use prepared statements internally.

## (a) `execute(String query, Object... args)`

### Advantages
- Cleaner abstraction for common inserts/updates/deletes.
- Less boilerplate at call sites.
- Keeps `PreparedStatement` details hidden from service/DAO callers.
- Easier to keep usage consistent across the route subsystem.

### Disadvantages
- Limited flexibility for advanced statement configuration.
- Harder to handle custom JDBC types or conditional parameter binding logic.

## (b) `execute(String query, Consumer<PreparedStatement> binder)`

### Advantages
- Maximum flexibility for complex parameter binding.
- Caller can access full `PreparedStatement` API when needed.
- Useful for special cases where `Object... args` is not enough.

### Disadvantages
- Leaks JDBC detail to caller, reducing abstraction.
- More verbose and error-prone (index mistakes, missed parameters).
- Harder to enforce consistent style if used everywhere.

## Decision In This Project
For the route subsystem, `Object... args` is used as the default for standard CRUD operations.
`Consumer<PreparedStatement>` is kept as an escape hatch for complex SQL updates if required later.

