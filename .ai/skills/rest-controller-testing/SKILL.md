---
name: rest-controller-testing
description: >
  REST controller integration testing skill for OpenELIS Global 2. Covers
  writing MockMvc-based controller tests that drive real HTTP endpoints through
  the full Spring MVC stack with a live Testcontainers database. Use when
  writing tests for @RestController classes, security/authorization checks,
  request validation, or end-to-end HTTP behaviour that cannot be verified at
  the service layer alone. Distinct from the backend-integration-testing skill
  which focuses on service/DAO layers with DBUnit.
---

# Skill: REST Controller Integration Testing

## Context

Controller tests in OpenELIS Global 2 verify HTTP behaviour end-to-end: routing,
request deserialization, validation, security, response shape, and status codes.
They use the real Spring context (Testcontainers database) with MockMvc and
differ from service-layer tests in two important ways:

1. **No DBUnit required** — controllers that do not touch persistent state can be
   tested without a dataset at all. Only seed data when the endpoint under test
   reads or writes database rows.
2. **Session auth is always required** — every secured endpoint needs a
   MockHttpSession carrying a UserSessionData with a valid sytemUserId.

## Trigger

- User asks to "write a test" or "add tests" for a *RestController.
- User mentions testing HTTP status codes, request validation, or security on a
  REST endpoint.
- User asks to reproduce a regression that manifests as a wrong HTTP status
  (e.g. 500 instead of 409, 400 instead of 200).

## Behavior

1. **Extend BaseWebContextSensitiveTest** — provides mockMvc, Testcontainers
   database, and Spring context identical to service tests.
2. **Omit @RunWith** — BaseWebContextSensitiveTest already declares it.
3. **Override setUp()** — always call super.setUp() first, then build a
   MockHttpSession with UserSessionData.
4. **Seed data only when needed** — use JdbcTemplate (preferred) or
   executeDataSetWithStateManagement for DBUnit XML. Always add an @After
   cleanup method mirroring the inserts so tests never leak state.
5. **Mock only auth helpers** — services like UserModuleService and
   UserRoleService that inspect roles can be mocked via
   ReflectionTestUtils.setField when it is impractical to seed full role data.
   Never mock the service under test.
6. **Assert aggressively on HTTP + JSON** — use status().is*(), jsonPath()
   with exact scalar values, and jsonPath().exists() / doesNotExist(). Avoid
   assertNotNull / assertNull.
7. **Cover both happy-path and error-path** — every write endpoint needs at
   least one success case and one client-error case (bad input, conflict, not
   found).
8. **Naming** — use *RestControllerIntegrationTest when the test seeds and
   reads real database state; *RestControllerTest for stateless / mock-heavy
   tests; *RestControllerSecurityTest for authorization-only tests.
9. **Run and verify** — after generating the test run:
   mvn test -Dtest=<NewTest> to confirm it passes before submitting.

## Reference

- [Overview](reference/overview.md) — patterns, session setup, data strategies.
- [Template (with DB)](templates/rest-controller-integration-test.java.template)
- [Template (no DB)](templates/rest-controller-stateless-test.java.template)
- [Example — SampleTypeManagement](examples/sample-type-management/SampleTypeManagementRestControllerIntegrationTest.java)
  — shows JdbcTemplate seeding, @After cleanup, conflict regression test.
