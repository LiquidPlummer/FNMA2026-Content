# Gradle for Java Developers — Lesson Outline

**Audience:** Skilled professional Java developers, new to Gradle
**Goal:** Basic working fluency — init a project, manage dependencies, run builds
**Depth:** Minimal Groovy, no deep DSL theory

---

## 1. What Gradle Is
- Build tool: compiles, tests, packages, resolves dependencies
- Compared briefly to Maven (declarative XML vs. Groovy/Kotlin DSL) — just enough for orientation
- Note: we'll use locally installed `gradle` throughout, not the wrapper

## 2. Project Initialization
- `gradle init` walkthrough (application type, Java, DSL choice, test framework)
- Resulting structure: `settings.gradle`, `app/build.gradle`, `src/main/java`, `src/test/java`
- **Gotcha:** build file lives in `app/`, not root, for `application` type
- Note: init also generates `gradlew`/`gradlew.bat` (the wrapper) — brief mention of what it is and why it exists (version pinning, no-install-needed for others), even though we won't use it here

## 3. Anatomy of `build.gradle` (minimal Groovy needed)
- `plugins {}` block — what a plugin is, why `java`/`application` matters (unlocks `testImplementation`, source sets, etc.)
- `repositories {}` block — where dependencies come from
- `dependencies {}` block — `implementation` vs `testImplementation`
- Just enough Groovy: blocks are closures, `=` vs method-call syntax, nothing more

## 4. Pointing at Our Internal Repository
- Brief: why we replace `mavenCentral()` with our internal repo (no public repo access here) — team can go deeper post-training

## 5. Verifying Configuration
- `gradle build --refresh-dependencies`
- Using `--info` to confirm which repo actually served a dependency
- Quick sanity test: comment out the repo block, confirm the build fails (proves it's load-bearing)

## 6. Common Environment Gotchas (from our own setup)
- `JAVA_HOME` pointing to JRE instead of JDK → no compiler found
- Wrong plugin applied (e.g. `maven-publish` alone doesn't give you `testImplementation`)
- Deprecation warnings vs. Gradle 9 compatibility — where to look

## 7. Running the Basics
- `gradle build`
- `gradle test`
- `gradle run` (for `application` plugin projects)
- `gradle dependencies` — inspecting the resolved graph

