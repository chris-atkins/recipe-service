# CLAUDE.md - Recipe Service (Backend)

## Stack
- Java 17, Dropwizard 1.3.29, Gradle
- PostgreSQL 16 via raw JDBC (no ORM)
- Flyway 11.10 for database migrations
- AWS SDK for DigitalOcean Spaces (S3-compatible image storage)

## Commands
```bash
gradle build      # Compile + run all tests
gradle test       # Tests only
gradle shadowJar  # Fat JAR for deployment
```

## Architecture

### Layered Structure
```
API Endpoint (JAX-RS @Path, @Timed)
  → Translator (Api* ↔ Domain conversion)
    → Domain Model (value objects, business logic)
      → Repository Interface → PostgresXxxRepository
```

### Source Layout
```
src/main/java/com/poorknight/
  api/              # JAX-RS endpoints
  recipe/           # Recipe domain: model, translator, repository, search
  recipebook/       # RecipeBook domain: model, translator, repository
  user/             # User domain: model, translator, repository
  image/            # Image domain: S3 storage + DB metadata
  application/      # App bootstrap, config, filters, DB setup
src/main/resources/
  db/migration/     # Flyway SQL migrations (V0–V4)
```

### Key Patterns

**Translator Pattern**: Every domain has a `*Translator` class converting between `Api*` objects (API layer) and domain objects. Example: `RecipeTranslator` converts `ApiRecipe` ↔ `Recipe`.

**Value Object IDs**: IDs are wrapped in nested static classes — use `Recipe.RecipeId` and `Recipe.UserId`, not raw strings in domain code. Translators handle string ↔ value object conversion.

**Repository Interface**: Each domain defines a repository interface (e.g., `RecipeRepository`) with a Postgres implementation (e.g., `PostgresRecipeRepository`). Repositories use raw JDBC with `DriverManager.getConnection()`.

**Endpoint Registration**: Endpoints are instantiated manually in `RecipeServiceApplication.run()` with their dependencies — no DI framework.

**Auth**: `RequestingUser` header carries user ID. Endpoints extract it via `@HeaderParam("RequestingUser")`. Null/empty checks throw `WebApplicationException` with 401.

### Database
- Schema: `recipe` (set via search_path)
- Migrations: `src/main/resources/db/migration/V*.sql`
- Tables: recipe, recipe_user, recipe_book, image
- IDs: `CHAR(24)` via `RandomStringUtils.randomAlphanumeric(24)`
- Image IDs: UUID (`VARCHAR(36)`)
- Full-text search: tsvector column + GIN index on recipe table

### Adding a New Feature
1. Write Flyway migration if schema change needed (next V number)
2. Create/update domain model with value objects
3. Create/update Translator with toApi/toDomain methods
4. Create/update Repository interface + PostgresXxxRepository
5. Create/update API endpoint with @Path, @Timed, proper auth checks
6. Register endpoint in RecipeServiceApplication if new
7. Write all tests BEFORE implementation (TDD)

## Testing

### Framework
- JUnit 5 (`@Test`, `@ExtendWith`)
- Mockito 5.18 (`@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`)
- AssertJ (`assertThat(...).isEqualTo(...)`) — preferred for all new tests
- Hamcrest exists in older tests — do not introduce in new tests

### Test Types

**Unit tests** (endpoints, translators, search): Mockito mocks for collaborators. Fast, no external deps.

**Integration tests** (repositories): TestContainers with PostgreSQL 16-alpine. Pattern:
```java
@BeforeAll static void setup() {
    PostgresTestHelper.startPostgresAndMigrateTables();
}
@AfterAll static void teardown() {
    PostgresTestHelper.stopPostgres();
}
@AfterEach void cleanup() {
    PostgresTestHelper.deleteAllRecipes(); // or appropriate table
}
```

### Test Conventions
- Location: Mirror source structure under `src/test/java/com/poorknight/`
- Class suffix: `*Test` (e.g., `RecipeEndpointTest`)
- Helper: `com.poorknight.mongo.setup.PostgresTestHelper`
- Method names: descriptive of behavior being tested
- Each test verifies one behavior
- Edge cases: nulls, empty strings, invalid IDs, unauthorized users, not-found scenarios

## Code Style
- **Indentation**: Tabs (not spaces)
- **Fields**: `private final` with constructor injection
- **Method params**: Use `final` keyword
- **Imports**: Specific imports, no wildcards (except static Mockito/AssertJ in tests)
