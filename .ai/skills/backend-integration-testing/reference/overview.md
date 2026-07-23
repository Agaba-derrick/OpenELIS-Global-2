# OpenELIS Global 2: Backend Integration Testing Guide

This document defines the mandatory patterns and infrastructure for writing and
running backend integration tests in OpenELIS Global 2. These tests focus on the
Service, DAO, and Controller layers using a real database container.

## 1. Core Technology Stack

- **JUnit 4**: The standard test runner (NOT JUnit 5).
- **Spring Test**: `@RunWith(SpringRunner.class)` with `@ContextConfiguration`.
- **Testcontainers**: Spawns a PostgreSQL 14.4 container for database isolation.
- **DBUnit**: Loads XML flat datasets for data seeding.
- **Liquibase**: Applies schema migrations to the test container.
- **MockMvc**: For REST controller and web layer testing.
- **Mockito**: For mocking external dependencies (FHIR, Odoo, etc.).

## 2. Mandatory Base Classes

### `BaseWebContextSensitiveTest`

All integration tests SHOULD extend this class. It provides:

- Full Spring Web context loading.
- Database connection management via Testcontainers.
- `executeDataSetWithStateManagement(String xmlPath)`: Truncates relevant tables
  and loads XML data.
- Default authentication: `ROLE_ADMIN` and `ROLE_RESULTS`.
- `mockMvc` instance for controller testing.

## 3. Configuration Classes

### `BaseTestConfig`

Handles the infrastructure layer:

- Starts `PostgreSQLContainer`.
- Configures `DataSource` pointing to the container.
- Runs `SpringLiquibase` to initialize the schema.
- Sets up `LocalContainerEntityManagerFactoryBean` (JPA/Hibernate).

### `AppTestConfig`

Handles the application context and isolation:

- **Selective Component Scanning**: Uses `excludeFilters` (Regex) to prevent
  automatic loading of large controller packages. This avoids circular
  dependencies and satisfies the "Traditional Spring MVC" singleton
  requirements.
- **Manual Bean Wiring**: Controllers are often manually registered as `@Bean`s
  at the bottom of this file. If you are testing a controller, you must ensure
  it is either scanned or explicitly defined here.
- **Infrastructure Mocks**: Provides `@Profile("test")` mocks for external
  services to ensure complete isolation from the network.

## 4. Bean Isolation & Configuration Standards

To maintain test stability and performance, we follow a "Selective Manual
Wiring" pattern:

### A. The "Exclude All" Strategy

We intentionally exclude entire controller packages from the base component
scan. This prevents Spring from trying to instantiate the entire web layer for
every test.

- **When to add a Bean**: If your test requires a class that is in an excluded
  package, you must manually add a `@Bean` for it in `AppTestConfig`.

### B. External Service Isolation

The following services are **MANDATORY** to mock in `AppTestConfig` to prevent
external side effects:

- **Odoo**: `OdooClient`, `OdooConnection` (Prevents ERP calls).
- **FHIR**: `FhirUtil`, `FhirConfig`, `FhirContext` (Prevents HAPI FHIR server
  calls).
- **Communication**: `JavaMailSender`, `OzekiMessageOutService` (Prevents real
  emails/SMS).
- **Security**: `TruststoreService`, `TextEncryptor` (Prevents real
  certificate/encryption logic).

### C. The `test` Profile

All test-specific mocks must be annotated with `@Profile("test")`. This ensures
they never accidentally leak into production code.

## 5. Writing an Integration Test

### Step 1: Create the Test Class

Naming convention: `*IntegrationTest.java` or `*Test.java` (if it extends the
base class).

```java
package org.openelisglobal.menu;

import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openelisglobal.BaseWebContextSensitiveTest;
import org.openelisglobal.menu.service.MenuService;
import org.openelisglobal.menu.valueholder.Menu;
import org.springframework.beans.factory.annotation.Autowired;

public class MenuServiceTest extends BaseWebContextSensitiveTest {

    @Autowired
    private MenuService menuService;

    @Before
    public void init() throws Exception {
        // Load data specific to this test suite
        executeDataSetWithStateManagement("testdata/menu.xml");
    }

    @Test
    public void getAllActiveMenus_shouldReturnOnlyActiveMenus() {
        List<Menu> activeMenus = menuService.getAllActiveMenus();

        // Avoid weak assertions like assertNotNull or assertFalse(isEmpty)
        // Assert exact size and exact values to guarantee correct state
        Assert.assertEquals(2, activeMenus.size());
        Assert.assertEquals("Active Menu A", activeMenus.get(0).getName());
        Assert.assertEquals("Active Menu B", activeMenus.get(1).getName());
        Assert.assertTrue(activeMenus.stream().allMatch(Menu::getIsActive));
    }
}
```

### Step 2: Create the Dataset (XML)

DBUnit XML datasets are stored in `src/test/resources/testdata/` (note:
`src/test/resources/fixtures/` holds raw SQL, not DBUnit XML). Use DBUnit Flat
XML format with bare table names (do NOT prefix with `clinlims.`; the database
schema prefix is handled automatically by the loader).

```xml
<dataset>
    <organization id="100" name="Test Lab" ... />
    <system_user id="100" login_name="tester" ... />
</dataset>
```

#### Hardened Loader Invariants & Constraints:

- **Never Declare `reference_tables`**: Do not declare `reference_tables` in
  your dataset fixture (it is a protected seed table).
- **System User ID 1**: The user `system_user` with `id=1` is a protected seed
  and is automatically re-seeded after every load.
- **Use IDs >= 100**: Always use IDs >= 100 for rows in tables that the
  test/application might insert into, to prevent primary key/ID conflicts with
  seed data.
- **Sequence Resync**: When using explicit IDs in fixtures, use the
  `resyncSequence(sequence, table)` helper to sync PostgreSQL sequences and
  avoid duplicate key exceptions on subsequent inserts.

## 6. Running Tests

### Via Maven

- **All tests**: `mvn test`
- **Specific test**: `mvn test -Dtest=MyFeatureIntegrationTest`
- **Skip all tests**: `mvn clean install -DskipTests -Dmaven.test.skip=true`
  (MANDATORY flags for skipping).

## 7. Best Practices

- **Transactional State**: Use `@Transactional` at the test level to ensure
  rollbacks, or use `executeDataSetWithStateManagement` to manually reset state
  if propagation is not supported.
- **Eager Fetching**: Ensure services used in tests eagerly fetch all required
  data to avoid `LazyInitializationException` outside of service transactions.
- **Mocking**: Only mock external systems or extremely expensive components.
  Prefer real DAOs and Services for integration tests.
- **No Hardcoded IDs**: Prefer using data loaded from fixtures and avoid relying
  on database-generated IDs that might change.
- **Harden Assertions**: Avoid weak assertions (e.g. `assertNotNull`,
  `assertNull`, or check-empty). Assert aggressively on specific values, exact
  list sizes, and field states.

## 8. Debugging & Troubleshooting

To effectively maintain integration tests, the model must recognize and resolve
these common issues:

### A. Common Exceptions

- **`LazyInitializationException`**: Occurs if a test tries to access an entity
  relationship (e.g., `sample.getAnalyses()`) outside of the Service
  transaction.
  - **Fix**: Use `JOIN FETCH` in the DAO or ensure all data is eagerly loaded by
    the Service before it returns to the Controller/Test.
- **`NoSuchTableException` (DBUnit)**: Occurs if the XML dataset references a
  table that does not exist in the database schema or is misspelled.
  - **Fix**: Verify table spelling against the database schema or JPA Entity. Do
    NOT use the `clinlims.` schema prefix, as the schema prefix is resolved
    automatically by the DBUnit connection loader.
- **`ConstraintViolationException`**: Often caused by stale data or foreign key
  issues.
  - **Fix**: Ensure `executeDataSetWithStateManagement` is called in the
    `@Before` block to truncate and reset tables.

### B. SQL Debugging

If you suspect the wrong data is being queried or updated:

1. Open `src/test/resources/hibernate/test-hibernate.cfg.xml`.
2. Change `<property name="show_sql">false</property>` to `true`.
3. Re-run the test to see the raw SQL in the console.

### C. Testcontainers / Docker Issues

If the database fails to start:

- **Check Docker Status**: Ensure the Docker daemon is running and has enough
  memory (at least 4GB).
- **Inspect Logs**: Look for `PostgreSQLContainer` in the Maven output. It will
  show if the image was pulled and if the port binding succeeded.

### D. Liquibase Failures

If the schema fails to apply:

- Check `src/main/resources/liquibase/base-changelog.xml` (loaded onto the test
  classpath via `BaseTestConfig`) for syntax errors or missing changesets.
- Note: The test database is ephemeral; sometimes a manual `mvn clean` is needed
  to reset the local Testcontainers state.

## 9. Enforcing Aggressive Assertions

When writing or reviewing backend integration tests, developers and agents MUST
NOT use weak assertions that only verify nullness or emptiness. These weak
checks often pass when incorrect or incomplete data is returned.

### Comparison of Assertion Styles

| Weak/Prohibited Assertion             | Aggressive/Mandatory Assertion                           | Why?                                                                                 |
| :------------------------------------ | :------------------------------------------------------- | :----------------------------------------------------------------------------------- |
| `assertNotNull(list);`                | `assertEquals(2, list.size());`                          | Nullness or emptiness checks pass on incorrect lists containing arbitrary elements.  |
| `assertFalse(list.isEmpty());`        | `assertEquals("expectedValue", list.get(0).getValue());` | Ensuring the exact values are mapped prevents silent data truncation/leaks.          |
| `assertNotNull(savedEntity.getId());` | `assertEquals("custom-id-99", savedEntity.getName());`   | Having an ID generated doesn't guarantee properties were correctly persisted/mapped. |
| `assertTrue(result);`                 | `assertEquals(ExpectedEnum.STATUS, entity.getStatus());` | Asserting specific state enums/values validates business logic correctness.          |
