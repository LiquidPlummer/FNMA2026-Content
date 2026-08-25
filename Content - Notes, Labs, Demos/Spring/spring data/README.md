# FNMA2026 - Spring Starter
This simple project has all of the basic boilerplate setup for spring boot web applications with a SQLite db.

Database file already created, database.db, and used in application.

Spring sec is still disabled, and the test implementations are commented out. See `build.gradle`.

You will need to edit the repositories section of `build.gradle` to point to an internal location for libraries.
```kotlin
// This needs to be swapped to the internal repo
repositories {
	mavenCentral()
}
```