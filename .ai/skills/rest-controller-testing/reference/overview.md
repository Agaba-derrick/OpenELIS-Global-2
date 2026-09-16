# REST Controller Testing — Patterns & Reference

## Infrastructure

All controller tests inherit from `BaseWebContextSensitiveTest`, which provides:

- A real **Testcontainers PostgreSQL** instance with full Liquibase migrations.
- A pre-configured **MockMvc** instance (`this.mockMvc`) wired to the real
  Spring DispatcherServlet.
- Helper method `executeDataSetWithStateManagement(path)` for DBUnit XML seeding.

## Session Authentication

Every secured endpoint requires a session attribute. Build it in `setUp()`:

```java
@Before
@Override
public void setUp() throws Exception {
    super.setUp();
    UserSessionData usd = new UserSessionData();
    usd.setSytemUserId(1);
    session = new MockHttpSession();
    session.setAttribute(IActionConstants.USER_SESSION_DATA, usd);
}
```

Then pass `.session(session)` to every `mockMvc.perform(...)` call.

## Data Strategy: Three Options

### Option 1 — No data (pure logic / stateless)
If the controller returns computed or config data and never touches the DB, skip
seeding entirely. Fastest; preferred.

### Option 2 — JdbcTemplate inserts (recommended for small datasets)
Use raw SQL for precision and speed. Always clean up in `@After`:

```java
@Autowired private javax.sql.DataSource dataSource;
private JdbcTemplate jdbc;

@Before @Override public void setUp() throws Exception {
    super.setUp();
    jdbc = new JdbcTemplate(dataSource);
    cleanup();
    jdbc.update("INSERT INTO clinlims.my_table (id, ...) VALUES (99901, ...)");
    // ... more inserts
}

@After public void tearDown() { cleanup(); }

private void cleanup() {
    jdbc.update("DELETE FROM clinlims.my_table WHERE id IN (99901)");
}
```

Use IDs in the 99000–99999 range to avoid conflicts with seed data.

### Option 3 — DBUnit XML dataset
Use `executeDataSetWithStateManagement("testdata/my-controller.xml")` when the
dataset is shared with other tests or already exists. Declare every table that
could be polluted from prior tests as an empty element to guarantee a clean
slate:

```xml
<?xml version='1.0' encoding='UTF-8'?>
<dataset>
    <my_table/>                <!-- clears table before seeding -->
    <my_table id="99901" .../>
</dataset>
```

## Mocking Auth Services

When the controller uses `UserModuleService` or `UserRoleService` to check
permissions, and seeding full role data is impractical:

```java
@Autowired private MyRestController myController;
private UserModuleService userModuleServiceMock;

@Before @Override public void setUp() throws Exception {
    super.setUp();
    userModuleServiceMock = Mockito.mock(UserModuleService.class);
    ReflectionTestUtils.setField(myController, "userModuleService", userModuleServiceMock);
    when(userModuleServiceMock.isSessionValid(any())).thenReturn(true);
}
```

**Never** mock the service under test. Only mock auth/infrastructure helpers.

## Assertion Patterns

```java
// Status only
.andExpect(status().isOk())
.andExpect(status().isConflict())
.andExpect(status().isBadRequest())
.andExpect(status().isNoContent())

// JSON body — always use exact scalar values
.andExpect(jsonPath("$.success").value(true))
.andExpect(jsonPath("$.data.name").value("Expected Name"))
.andExpect(jsonPath("$.data.items").isArray())
.andExpect(jsonPath("$.data.items.length()").value(3))
.andExpect(jsonPath("$.message").exists())
```

## Naming Conventions

| Suffix | When to use |
|---|---|
| `*RestControllerIntegrationTest` | Seeds / reads real DB rows |
| `*RestControllerTest` | Stateless or mock-heavy; minimal DB interaction |
| `*RestControllerSecurityTest` | Authorization checks only (roles, sessions) |

## Running a Single Controller Test

```bash
mvn test -Dtest=MyRestControllerIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false
```
