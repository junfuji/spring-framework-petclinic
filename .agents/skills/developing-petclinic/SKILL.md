---
name: developing-petclinic
description: Orientation for working in this repo — plain Spring Framework 7 / Jakarta EE 11 WAR on JDK 25 with XML configuration, three interchangeable persistence layers, and JSP views. Use for build/test commands, where configuration lives, and the constraints that break the build if ignored.
---

# Developing spring-framework-petclinic

Plain **Spring Framework** (no Spring Boot), XML-configured, packaged as a **WAR** with JSP views.
The point of the repo is the classic 3-layer architecture — presentation → service → repository —
with the repository layer swappable at runtime.

For building the WAR and testing it in a browser on Tomcat, see the `testing-petclinic-war` skill.

## Commands

```bash
./mvnw clean verify        # compile + all 62 tests + WAR + jacoco  (the full gate)
./mvnw clean test          # tests only
./mvnw test -Dtest=OwnerControllerTests
./mvnw generate-resources  # wro4j: LESS -> resources/css/petclinic.css only
```

There is no separate lint or formatter — `verify` is the whole check. `.editorconfig` rules the
formatting: 4-space indent for `.java`/`.xml`, LF, UTF-8, final newline, no trailing whitespace.
Every Java file carries the Apache 2.0 license header.

`./mvnw` requires **JDK 25 (Temurin)** and Maven wrapper >= 3.9 (`maven.compiler.release=25`;
maven-resources-plugin needs Maven 3.6.3+). Anything older fails before javac runs.

## Where things live

| What | Where |
|---|---|
| Bean wiring (services, profiles, `<jpa:repositories>`, tx) | `src/main/resources/spring/business-config.xml` |
| DataSource, `<jdbc:initialize-database>`, property placeholders | `src/main/resources/spring/datasource-config.xml` |
| MVC: component scan, conversion service, exception resolver | `src/main/resources/spring/mvc-core-config.xml` |
| View resolvers, JAXB marshaller for `/vets.xml` | `src/main/resources/spring/mvc-view-config.xml` |
| Cache manager (JCache/EhCache 3) + JMX | `src/main/resources/spring/tools-config.xml` |
| Servlet bootstrap (replaces `web.xml`) | `PetclinicInitializer` |
| Per-vendor schema + seed SQL | `src/main/resources/db/{h2,hsqldb,mysql,postgresql}` |
| JSPs and tag files | `src/main/webapp/WEB-INF/{jsp,tags}` |
| LESS sources and the wro4j model | `src/main/webapp/resources/less`, `src/main/wro/wro.xml` |

Config is **XML, not annotations** — adding a bean usually means editing one of the files above,
not writing a `@Configuration` class. Schema locations are intentionally versionless
(`spring-beans.xsd`, not `spring-beans-7.0.xsd`) so upgrades don't touch every file.

## Three repository implementations

`repository/` has one interface per aggregate (`OwnerRepository`, `PetRepository`,
`VetRepository`, `VisitRepository`) and three implementations selected by Spring profile:

| Profile | Package | Style |
|---|---|---|
| `jpa` (default) | `repository/jpa` | `EntityManager` + JPQL |
| `jdbc` | `repository/jdbc` | `NamedParameterJdbcTemplate`, hand-assembled aggregates |
| `spring-data-jpa` | `repository/springdatajpa` | Spring Data interfaces + `@Query` |

**Any change to a repository interface must be made in all three implementations**, and each has a
service-layer test (`ClinicServiceJdbcTests`, `ClinicServiceJpaTests`,
`ClinicServiceSpringDataJpaTests`) that all extend `AbstractClinicServiceTests` — so one new test
method there runs against all three. The default profile is hard-coded in `PetclinicInitializer`.

## Database profiles

H2 in-memory is the default and is what tests and `verify` use. Maven profiles `H2`, `HSQLDB`,
`MySQL`, `PostgreSQL` just swap the `jdbc.*` / `jpa.database` properties filtered into
`spring/data-access.properties`, e.g. `./mvnw clean package -P MySQL`. Only H2 is exercised by CI;
the other vendors need a running server.

## Constraints that bite

- **Jakarta only.** Everything is `jakarta.*` (servlet, persistence, validation, xml.bind). The one
  legitimate `javax.` left is `javax.sql.DataSource` (JDK) plus the `javax.cache:cache-api`
  coordinates — JSR-107 was never renamed. A new `javax.servlet`/`javax.persistence` import is a bug.
- **`<parameters>true</parameters>` is required** on maven-compiler-plugin. Spring 6+ dropped
  `LocalVariableTableParameterNameDiscoverer`, so without it `@PathVariable int petId` fails at
  request time with *"Name for argument of type [int] not specified"*.
- **Spring 7 pins the ecosystem**: Hibernate ORM 7.1+ (JPA 3.2), Hibernate Validator 9 (Bean
  Validation 3.1), and spring-test 7 needs the **JUnit 6** API — JUnit 5 fails with
  `NoSuchMethodError` on `ExtensionContext.Store.computeIfAbsent`. Don't downgrade any of these.
- **No `Mockito.mock` straight from XML.** Spring 6 removed the generic-return-type inference it
  relied on; the mock bean typed as `Object` and MVC contexts failed with
  `NoSuchBeanDefinition: ClinicService`. `src/test/resources/spring/mvc-test-config.xml` goes through
  the typed `ClinicServiceMockFactory` instead.
- **`messages_*.properties` must be UTF-8**, not ISO-8859-1 — the resources plugin fails the build
  with `MalformedInputException` otherwise.
- **`src/main/resources/cache/ehcache.xml` is the EhCache 3 format** (`http://www.ehcache.org/v3`)
  with a `<jsr107:defaults>` service, wired via `JCacheCacheManager`. The Spring EhCache 2 support
  classes no longer exist.
- CSS is generated: edit the LESS under `src/main/webapp/resources/less` (entry point
  `petclinic.less`); `resources/css/petclinic.css` only exists inside the built WAR.

## Devin Secrets Needed

None — the default build and tests run entirely against in-memory H2.
