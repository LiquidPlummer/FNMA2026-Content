# Verifying Configuration

A build that succeeds isn't proof the repository swap worked — Gradle caches every downloaded artifact (in `~/.gradle/caches/`), so a "successful" build may be running entirely off yesterday's downloads. Before trusting the configuration, we verify it three ways, cheapest to most conclusive.

---

## 1. Force Real Resolution

```console
$ gradle build --refresh-dependencies
```

*`--refresh-dependencies` makes Gradle revalidate every dependency against the declared repositories instead of trusting the cache.*

This is the honest test: if the internal repository URL is wrong, unreachable, or missing an artifact, *this* build fails — where a plain `gradle build` might quietly succeed from cache. Expect it to be slower; that's the point. (It's also the standard first move whenever dependencies behave strangely — before the folk remedy of deleting `~/.gradle/caches` wholesale.)

---

## 2. Confirm Who Actually Served It

`--info` raises Gradle's log level enough to show resolution traffic — grep it for our repository's host:

```console
$ gradle build --refresh-dependencies --info | grep -i "repo.example-corp"

Downloading https://repo.example-corp.internal/maven/com/google/guava/guava/33.0.0-jre/guava-33.0.0-jre.pom
Downloading https://repo.example-corp.internal/maven/com/google/guava/guava/33.0.0-jre/guava-33.0.0-jre.jar
```

*The receipts: every `Downloading` line names the URL that served the artifact — all of them should be the internal host.*

This answers the precise question "which repo did this jar come from?" — invaluable when multiple repositories are declared somewhere (an init script, a stray fallback) and something is being served from the wrong place. Any `Downloading` line pointing at an unexpected host is a finding.

---

## 3. Prove the Block Is Load-Bearing

The quickest way to be *certain* the build depends on our configuration — and not some cached or ambient fallback — is to break it on purpose:

```groovy
// repositories {
//     maven {
//         name = 'internal'
//         url = 'https://repo.example-corp.internal/maven'
//     }
// }
```

*Comment out the repositories block, then rerun with `--refresh-dependencies`.*

```console
$ gradle build --refresh-dependencies

FAILURE: ...
> Could not resolve com.google.guava:guava:33.0.0-jre.
  > Cannot resolve external dependency com.google.guava:guava:33.0.0-jre
    because no repositories are defined.
```

*The expected failure: no repositories, no resolution — proving the block we wrote is the one doing the work.*

Restore the block, rerun, watch it pass. This negative test takes thirty seconds and eliminates a whole category of false confidence ("it works, but I don't know why"). The same technique — break it, confirm the failure names your suspect, fix it — generalizes to most configuration debugging.

With configuration verified, the remaining friction is environmental — the handful of machine-setup issues that produce misleading errors, collected in the next lesson.
