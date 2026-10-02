---
name: testing-petclinic-war
description: How to build, deploy and smoke-test the plain-Spring (non-Boot) Petclinic JSP/WAR on Tomcat 11, including switching the jpa / jdbc / spring-data-jpa persistence profiles. Use when runtime-testing this repo instead of only running unit tests.
---

# Runtime-testing the Petclinic WAR

This project is **plain Spring Framework with JSP views packaged as a WAR** — not Spring Boot.
There is no embedded server and (since the Jakarta EE 11 migration) no `jetty:run-war` goal, so
the only way to test end-to-end is to deploy the WAR into a servlet container yourself.

## Build

```bash
cd <repo> && ./mvnw clean package        # -> target/petclinic.war (~50 MB)
```

Requires JDK 25 (Temurin). `maven.compiler.release=25` and `wro4j-maven-plugin` needs >= 17.
wro4j generates `resources/css/petclinic.css` inside the WAR at build time — if the deployed app
looks unstyled, suspect wro4j rather than the container.

## Deploy on Tomcat 11

Tomcat 11 is the right container: it implements Servlet 6.1 / Jakarta EE 11. Tomcat 9 or older
will NOT work (javax namespace). Download from `https://dlcdn.apache.org/tomcat/tomcat-11/vX.Y.Z/bin/`
— only the two most recent patch versions are hosted there, so list the directory first:

```bash
curl -s https://dlcdn.apache.org/tomcat/tomcat-11/ | grep -o 'v11[0-9.]*' | tail -2
V=11.0.24
cd ~ && curl -sLO https://dlcdn.apache.org/tomcat/tomcat-11/v$V/bin/apache-tomcat-$V.tar.gz
tar xzf apache-tomcat-$V.tar.gz
rm -rf apache-tomcat-$V/webapps/ROOT                       # drop the stock landing page
cp <repo>/target/petclinic.war apache-tomcat-$V/webapps/ROOT.war
apache-tomcat-$V/bin/startup.sh                            # http://localhost:8080/
```

Startup takes ~5 s; watch `logs/catalina.out` for
`Deployment of web application archive [...ROOT.war] has finished`.

## Persistence profiles

`PetclinicInitializer` defaults to the `jpa` profile; `jdbc` and `spring-data-jpa` are declared in
`src/main/resources/spring/business-config.xml`. Switch with a JVM arg, which for Tomcat means
`CATALINA_OPTS` (not `JAVA_OPTS`, which is also read but is overwritten by some setups):

```bash
cd ~/apache-tomcat-$V
./bin/shutdown.sh && sleep 8 && rm -f logs/catalina.out
CATALINA_OPTS="-Dspring.profiles.active=jdbc" ./bin/startup.sh   # or spring-data-jpa
```

Deleting `catalina.out` between profiles is what makes "no startup errors for THIS profile" a real
assertion. Confirm the flag actually reached the JVM by grepping the log for
`Command line argument: -Dspring.profiles.active=`. All three profiles run against the same H2
in-memory DB seeded at startup, so the same data assertions hold for each.

Log-hygiene grep that catches Jakarta-migration breakage:

```bash
grep -nE "NoClassDefFoundError|ClassNotFoundException|javax\.(servlet|persistence|validation|xml)|SEVERE|JasperException|ERROR|Exception" logs/catalina.out
```

Benign warnings you can ignore: EhCache 3 `sun.misc.Unsafe` deprecation on JDK 25, Hibernate
`HHH90000025` H2Dialect notice, `MvcNamespaceHandler` XML-config deprecation, and the spring-jcl
commons-logging discovery message.

## UI paths (all extensionless)

`/` · `/owners/find` · `/owners?lastName=<x>` · `/owners/{id}` · `/owners/new` ·
`/owners/{id}/edit` · `/owners/{id}/pets/new` · `/owners/{id}/pets/{petId}/visits/new` ·
`/vets` · `/vets.xml` (JAXB) · `/vets.json` · `/oups` (deliberate RuntimeException → styled page).

**`.html` suffixes 404.** `/vets.html` and `/oups.html` do not resolve — Spring 6+ removed suffix
pattern matching. The app's own navbar has always used the extensionless URLs, so this is expected,
not a migration bug. Don't waste time chasing it.

Seed data useful for assertions: searching last name `Davis` returns exactly Betty Davis
(638 Cardinal Ave., Sun Prairie) and Harold Davis (563 Friendly St., Windsor); `/vets` lists 6 vets.
Under `spring-data-jpa` the vets come back in id order instead of sorted by last name — same 6 rows.

## Gotcha: the pet/visit date field

The birth-date/visit-date inputs use a jQuery datepicker that **strips typed `-` characters**, so
typing `2024-05-01` yields `20240501` and the server rejects it with `invalid date` (this is correct
server-side validation, not a bug). Pick the date from the calendar widget instead — it writes
`2024/05/01`, which the `LocalDate` formatter accepts.

## Devin Secrets Needed

None — everything runs locally against the in-memory H2 database.
