# Maven

**Maven** is the elder statesman of Java build tools: XML-configured, heavily convention-driven, and still the build tool behind a huge share of enterprise Java. Its core philosophy is **convention over configuration** — Maven has one standard way to lay out a project and one standard build process, and as long as we follow the conventions, our configuration stays small. We describe *what* our project is; Maven already knows *how* to build it.

---

## The Standard Directory Layout

Every Maven project uses the same structure. This is the convention that makes Maven projects instantly navigable — open any one of them, anywhere, and we already know where everything lives:

```
my-app/
├── pom.xml                      # the build file — one per project
├── src/
│   ├── main/
│   │   ├── java/                # application source code
│   │   └── resources/           # config files, properties, etc.
│   └── test/
│       ├── java/                # test source code
│       └── resources/           # test-only resources
└── target/                      # build output (generated — never commit this)
```

Production code goes under `src/main/java`, tests under `src/test/java`, and everything Maven generates — compiled classes, the final jar — lands in `target/`. We don't configure any of these paths; Maven simply assumes them. Deviating is possible but almost never worth it.

---

## The `pom.xml`

The heart of a Maven project is its **POM** (Project Object Model), a file named `pom.xml` at the project root. It describes the project's identity, its dependencies, and any customizations to the build. A small but realistic example:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <!-- Coordinates: this project's identity -->
    <groupId>com.example</groupId>
    <artifactId>order-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.17.1</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### Coordinates: `groupId`, `artifactId`, `version`

The first three elements are the project's **coordinates** — the same group/name/version scheme every artifact in the Java world uses:

- **`groupId`** — the publishing organization or team, by convention a reversed domain name (`com.example`, `org.apache.commons`).
- **`artifactId`** — the project's name, which becomes the jar's filename.
- **`version`** — the release version. A version ending in **`-SNAPSHOT`** means "still in development": Maven treats snapshots as changeable and re-checks repositories for newer copies, whereas a release version like `1.0.0` is immutable forever.

Together these are often written shorthand as `com.example:order-service:1.0.0-SNAPSHOT`. Note the symmetry: our project *declares* coordinates, and our dependencies are just *other projects'* coordinates.

### Dependencies and Scopes

Each `<dependency>` block names one library by its coordinates. Maven downloads it — along with its full transitive graph — and puts it on the classpath. The optional **`<scope>`** element controls *which* classpath, and whether the dependency travels with our artifact:

| Scope | Available when | Typical use |
|---|---|---|
| `compile` (default) | Compiling, testing, and at runtime | Most libraries — Jackson, Guava |
| `test` | Compiling and running tests only | JUnit, Mockito, AssertJ |
| `provided` | Compiling and testing, but **not** packaged | APIs the runtime environment supplies (e.g., a servlet container) |
| `runtime` | Testing and runtime, but not compilation | JDBC drivers — code compiles against the JDBC API, not the driver |
| `import` | Special — used to pull in a BOM's dependency versions | Managing version sets like `spring-boot-dependencies` |

Getting scopes right keeps our published artifact lean and our test libraries out of production code. The most common mistake is forgetting `<scope>test</scope>` on a test library, which quietly ships JUnit inside the application.

### Plugins and Goals

Here's the thing about Maven: the core of Maven does almost nothing by itself. All real work — compiling, testing, packaging — is performed by **plugins**, and each plugin exposes one or more **goals** (individual tasks). Compilation is the `compile` goal of the `maven-compiler-plugin`; test execution is the `test` goal of `maven-surefire-plugin`; jar creation is `maven-jar-plugin`. These defaults are bound automatically, which is why a bare `pom.xml` still produces a working build.

We add a `<build><plugins>` section only when we need something beyond the defaults — for example, building an executable "fat jar" with all dependencies included:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.2</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals><goal>shade</goal></goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## The Build Lifecycle

Maven's build process is a fixed sequence of **phases**. The main ones, in order:

```
validate → compile → test → package → verify → install → deploy
```

- **`validate`** — check the project structure and `pom.xml` are sane.
- **`compile`** — compile `src/main/java` into `target/classes`.
- **`test`** — compile and run the unit tests; a failing test fails the build.
- **`package`** — bundle the compiled code into a jar (or war) in `target/`.
- **`verify`** — run any integration tests and quality checks.
- **`install`** — copy the artifact into the local repository (`~/.m2`) so other projects on this machine can depend on it.
- **`deploy`** — publish the artifact to a remote repository for others to use.

The crucial rule: **running a phase runs every phase before it**. `mvn package` validates, compiles, and tests first — there's no way to package code that hasn't passed its tests (short of explicitly skipping them). Plugin goals are bound to phases, which is how the lifecycle actually does anything: when the `test` phase runs, the surefire plugin's `test` goal fires.

---

## Repositories

Maven looks for dependencies in a fixed order:

1. **The local repository** — `~/.m2/repository` on our machine. Every artifact Maven ever downloads (or that we `install`) is cached here, organized by coordinates. First build of a project downloads a lot; subsequent builds hit the cache.
2. **Maven Central** — the default remote repository (`https://repo.maven.apache.org`), hosting essentially every public Java library.
3. **Custom/private repositories** — declared in the `pom.xml` or in `~/.m2/settings.xml`. Teams use a private repository manager (Nexus, Artifactory) to host internal artifacts and to proxy Central. Credentials and mirror configuration belong in `settings.xml`, which stays on the machine — never in the `pom.xml`, which is committed.

If a build behaves strangely after a network hiccup, a corrupted download in `~/.m2` is a classic culprit — deleting the offending directory from the local repository forces a clean re-download.

---

## Common Commands

Maven is invoked as `mvn` followed by phases and/or goals:

```bash
mvn compile          # compile main source code
mvn test             # compile + run unit tests
mvn package          # compile + test + build the jar into target/
mvn install          # all of the above + copy jar to ~/.m2
mvn clean            # delete target/ — start fresh
mvn clean install    # the everyday "full clean build"
```

`clean` isn't part of the default lifecycle — it's a separate one that wipes `target/` — which is why we chain it explicitly. A few more flags worth knowing:

```bash
mvn clean install -DskipTests    # build without running tests (use sparingly)
mvn dependency:tree              # print the full transitive dependency graph
mvn package -X                   # debug output, for when the build misbehaves
```

`mvn dependency:tree` deserves a special mention: when two libraries drag in conflicting versions of the same dependency, this command is how we see exactly where each version is coming from.
