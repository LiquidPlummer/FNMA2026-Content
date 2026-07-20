# Lab: String Workout — cleaning text and the string pool

In this lab we build a small **text cleanup utility**, the **String Workout**, that takes messy, human-typed input — a name with stray spacing and shouty case, a comma-separated list of tags, a ticket code with a placeholder suffix — and turns it into something presentable. Along the way we cover the everyday `String` API (`strip`, case conversion, `substring`, `indexOf`, `replace`, `split`, `String.join`), then pause to *see* how `==` and `.equals()` behave differently on string literals versus strings built at runtime — the string pool, in action rather than in theory. We finish with `String.format` for a one-line report.

---

## Prerequisites

| Software | Required Version |
|---|---|
| JDK (Temurin or any OpenJDK build) | 21 (LTS) |
| Apache Maven | 3.9.x |

Verify both with `java -version` and `mvn -version`. Then, from this lab's folder (the one containing `pom.xml`), compile and run:

```console
mvn -q compile exec:java
```

You should see `=== String Workout ===` followed by `Done.`. That's the starter skeleton in [src/main/java/com/curriculum/labs/StringWorkout.java](src/main/java/com/curriculum/labs/StringWorkout.java) — open it now and find the numbered `[MARKER]` comments. Each part below tells you which marker(s) to work at. Re-run the command above after every part to see your progress.

---

## Part 1 — Cleaning a name: `strip`, case, and `substring`

Our first raw field is a name typed carelessly: extra spaces around it, and the caps lock key clearly involved. We want a clean, single capitalized word.

At **`[MARKER 6]`** — below `main`, as a separate method — we'll add:

```java
static String cleanName(String raw) {
    String trimmed = raw.strip();
    String lower = trimmed.toLowerCase();
    String firstLetter = lower.substring(0, 1).toUpperCase();
    String rest = lower.substring(1);
    return firstLetter + rest;
}
```

*Trim the whitespace, lowercase everything, then rebuild the first character in uppercase — `substring(0, 1)` grabs just that character, `substring(1)` grabs everything after it.*

Then, at **`[MARKER 1]`** in `main`, we'll call it:

```java
String cleanedName = cleanName(rawName);
System.out.println("Name: " + cleanedName);
```

*Calls our new method on the messy `rawName` field and prints the result.*

Run it. `"  jOHN   "` becomes `Name: John`. Notice what we *didn't* do: nothing here mutates `raw` — `strip()`, `toLowerCase()`, and `substring()` each hand back a brand-new `String`, exactly as immutability promises. If you forget to capture a return value (say, calling `trimmed.toLowerCase();` on its own line and never using the result), the original variable is silently unaffected — that's the classic immutability trap from the notes, and now you've seen why capturing every result matters.

---

## Part 2 — Splitting and rejoining a tag list

Next up: a comma-separated tags field, with inconsistent spacing around each entry. We'll split it apart, clean each piece, and join it back together neatly.

At **`[MARKER 7]`**, below `main`, we'll add:

```java
static String normalizeTags(String raw) {
    String[] parts = raw.split(",");
    String first = parts[0].strip();
    String second = parts[1].strip();
    String third = parts[2].strip();
    return String.join(", ", first, second, third);
}
```

*`split(",")` breaks the raw field into an array of three pieces; we `strip()` each one to remove the stray spaces, then `String.join` glues them back together with a consistent separator.*

At **`[MARKER 2]`** in `main`:

```java
String cleanedTags = normalizeTags(rawTags);
System.out.println("Tags: " + cleanedTags);
```

*Calls our tag normalizer and prints the result.*

Run it. `"fiction, Mystery ,  short story "` becomes `Tags: fiction, Mystery, short story`. This version assumes exactly three tags — fine for now, but a fragile assumption. Real input might have two tags, or five, or an empty one in the middle. You'll stress-test that assumption yourself in the exercises.

---

## Part 3 — Extracting and replacing pieces of a code

Our last raw field is a ticket code like `REQ-2024-00042-TEMP` — we want the leading prefix on its own, and we want that `-TEMP` marker swapped for `-FINAL` once a ticket is closed out.

At **`[MARKER 8]`**, below `main`, we'll add:

```java
static String extractCode(String raw) {
    int dashIndex = raw.indexOf('-');
    String prefix = raw.substring(0, dashIndex);
    String finalized = raw.replace("TEMP", "FINAL");
    return prefix + " | " + finalized;
}
```

*`indexOf('-')` finds the position of the first dash; `substring(0, dashIndex)` takes everything before it. `replace` swaps every occurrence of `"TEMP"` for `"FINAL"` — like `toLowerCase()` and `strip()`, it returns a new string rather than editing `raw`.*

At **`[MARKER 3]`** in `main`:

```java
String codeInfo = extractCode(rawCode);
System.out.println("Code: " + codeInfo);
```

*Calls our extractor and prints the result.*

Run it: `Code: REQ | REQ-2024-00042-FINAL`. Two indexing details worth double-checking against the notes here: `indexOf` returns `-1` if the character never appears (worth guarding against in real code — we didn't, on purpose, since we know our input has a dash), and `substring(begin, end)` — used back in Part 1, and implicitly here with just one argument — always excludes `end`.

---

## Part 4 — `==` vs `.equals()`: seeing the string pool

Time to prove, rather than just be told, why `==` on strings is treacherous. We'll compare two literals, then compare a literal against a string that was *built at runtime* — even though both hold identical characters.

At **`[MARKER 4]`** in `main`, we'll add:

```java
String literalJohn = "john";
String anotherLiteralJohn = "john";
System.out.println("two literals ==       : " + (literalJohn == anotherLiteralJohn));

String runtimeJohn = cleanedName.toLowerCase();
System.out.println("runtime == literal    : " + (runtimeJohn == literalJohn));
System.out.println("runtime .equals literal: " + runtimeJohn.equals(literalJohn));

String newJohn = new String("john");
System.out.println("new String == literal : " + (newJohn == literalJohn));
```

*Four comparisons: two literals, a runtime-built string against a literal, the same pair with `.equals()`, and an explicit `new String(...)` against a literal.*

Run it and read the four lines carefully:

- **`two literals ==`** prints `true` — `"john"` written twice in source code resolves to the *same* pooled object.
- **`runtime == literal`** prints `false` — `cleanedName.toLowerCase()` computes its result while the program runs; even though its characters are `"john"`, it's a fresh object, not the pooled one.
- **`runtime .equals literal`** prints `true` — same characters, which is all `.equals()` ever checks.
- **`new String == literal`** prints `false` — `new` always forces a distinct object on the heap, bypassing the pool entirely, on purpose.

This is exactly the pattern from the String Pool notes: literals share, everything computed — concatenation, `substring`, `toLowerCase`, user input, `new String(...)` — does not. It's why a quick test full of literals can pass with `==` and the same logic can fail once real data shows up. The habit: **`.equals()` for strings, every time.**

---

## Part 5 — A formatted report line

To finish, we'll combine everything we've cleaned into one readable line using `String.format`, instead of a long chain of `+`.

At **`[MARKER 5]`** in `main`, we'll add:

```java
String report = String.format("Report -> name: %s | tags: %s | code: %s", cleanedName, cleanedTags, codeInfo);
System.out.println(report);
```

*`%s` placeholders are substituted in order by the arguments that follow — no manual `+` concatenation needed.*

Run the whole program one more time, top to bottom, and read the full output: cleaned name, cleaned tags, extracted code, the four pool comparisons, and finally the one-line report tying it all together.

---

## Exercises

The training wheels come off — same concepts, new problems, no step-by-step.

1. **Initials extractor.** Given a full name like `"  ada   lovelace  "` (arbitrary spacing, arbitrary case), produce its initials as `"A.L."`. Think about what `split` gives you once the string is trimmed, and how you'd pull the first character of each piece.

2. **Filename sanitizer.** Given a messy title like `"My Report: Final??.docx"`, produce a safe filename by lowercasing it, replacing every space with an underscore, and removing (not just replacing — actually stripping out) characters like `:` and `?`. Decide which `String` methods chain together to get there.

3. **CSV field splitter that survives empty fields.** Part 2's `normalizeTags` assumed exactly three tags — a fragile assumption. Given a row like `"red,,blue,"`, a plain `split(",")` silently drops trailing empty fields. Look up `String.split`'s second (limit) parameter, and write a splitter that correctly reports **4** fields for that row (including the two empty ones), not 2.

4. **Palindrome check.** Given a phrase like `"A man a plan a canal Panama"`, determine whether it reads the same forwards and backwards once spaces and case are ignored. (No `StringBuilder` yet — that's next lab. `charAt` and a bit of index arithmetic will get you there, or look at what `new StringBuilder(s).reverse().toString()` would give you and decide for yourself whether it counts as cheating this early.)
