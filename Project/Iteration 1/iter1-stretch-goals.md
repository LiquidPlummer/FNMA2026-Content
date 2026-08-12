# ERS Iteration 1 — Stretch Goals

These are optional extensions to Iteration 1. Pick the ones that interest you; several map directly onto features you will get "for free" in Spring during Iteration 2, and doing them by hand now means you will understand what Spring is doing later.

Each goal lists what to build and how you will know it is finished. Implementation choices are yours unless stated otherwise.

---

## 1. Encrypt Passwords

Right now your `users` table almost certainly stores passwords as readable text. Anyone with database access — or anyone who obtains a database backup — has every user's credentials. Fix this.

The correct approach is *hashing*, not encryption. Encryption is reversible by design; password hashing must not be. You will never need to recover a user's original password, only to confirm that a submitted password matches the one on file.

**Requirements**

- Use an established password hashing library. Reasonable options include jBCrypt (`org.mindrot:jbcrypt`), the Favre bcrypt library (`at.favre.lib:bcrypt`), or Spring Security's crypto module. Do not write your own hashing, and do not use raw MD5 or SHA-256.
- Hash the password during registration, before it reaches the DAO.
- Each user's hash must use a unique salt. Any reputable bcrypt library does this automatically and stores the salt inside the resulting hash string — you do not need a separate salt column.
- Rework your login flow. You cannot query for a matching password anymore, because the same password produces a different hash every time it is hashed. Instead: look the user up by username, then verify the submitted password against the stored hash.
- The password field must never appear in an API response. Check your JSON serialization for user objects, including nested users on a reimbursement's `author` and `resolver`.
- Your password column will need to be wide enough for the hash. A bcrypt hash is 60 characters.

**Note**

Use the library's own verification method (`BCrypt.checkpw`, `verifyer.verify`, or equivalent). Do not hash the submitted password and compare the two strings yourself — the salt is embedded in the stored hash, so a freshly generated hash will never match by string equality.

**Done when**

- Selecting directly from the `users` table shows no readable passwords.
- Two users who register with the identical password have visibly different stored hashes.
- Login still works, and login with a wrong password still fails.
- No endpoint anywhere returns a password or hash.

---

## 2. Validate Input

Your API currently trusts whatever it is handed. A client can submit a reimbursement for a negative amount, an empty description, or a nonexistent type, and your database will accept it. Add a validation layer that rejects bad input with a clear explanation of what was wrong.

Validation belongs in the service layer. The controller may do structural checks — did the body parse, is a required field present — but business rules are the service's responsibility. Never rely on the frontend for validation; anyone can bypass your HTML page and call the API directly.

**Requirements**

Enforce at minimum:

- **Amount** — required, greater than zero, no more than two decimal places, and below some sane upper bound you choose and document.
- **Description** — required, not blank or whitespace-only, with a maximum length that matches your column definition.
- **Type** — must be one of `TRAVEL`, `FOOD`, `LODGING`, `OTHER`. An unrecognized value is a client error, not a server error.
- **Username** — required, unique, minimum length, no whitespace.
- **Password** — required, with a documented complexity policy. Pick something defensible (for example: at least 8 characters, containing at least one letter and one digit) and state it in your error message.

Every rejection must return HTTP 400 with a message identifying which field failed and why. `"Bad request"` is not an acceptable message. Returning all validation failures at once rather than only the first is a worthwhile refinement.

**Done when**

- Submitting a reimbursement with amount `-50`, `0`, or `12.345` is rejected with a specific message.
- Submitting a blank or whitespace-only description is rejected.
- Submitting type `"PIZZA"` returns 400, not 500.
- Registering with a duplicate username returns a clear conflict message rather than a database constraint error.
- Attempting to edit an already-approved reimbursement is rejected.

---

## 3. Unit Test a Service Class

Add meaningful unit tests to one service class. This is about test quality, not coverage numbers — do not chase a percentage.

Choose a class with real decision-making in it. Your reimbursement service is usually the best candidate, since resolving a request involves several conditions: the request must exist, it must be `PENDING`, the acting user must be a manager, and the resolver must be recorded on success.

**Requirements**

- Tests must not touch the database. Mock the DAO (Mockito is the standard choice) so you are testing your service's logic in isolation.
- Cover the failure paths, not just the happy path. For a resolve operation that means: request not found, request already resolved, and acting user is not a manager.
- Assert on behavior, not just return values. When validation fails, verify that the DAO's update method was *never called*. A service that throws an exception after already writing to the database is still broken.
- Tests must pass with no database running and no server started.

**Note**

If you find you cannot mock the DAO, it is because your service constructs its own DAO internally (`private ReimbursementDAO dao = new ReimbursementDAO();`). Change the service to accept its DAO as a constructor parameter so tests can supply a fake one. This is dependency injection, and it is exactly the problem Spring solves for you in Iteration 2. Running into the problem yourself first is the point.

**Done when**

- Your test suite runs green with the database stopped.
- At least one test proves a DAO method is not called when input is invalid.
- Someone reading a test name can tell what scenario it covers without reading the body.

---

## 4. Global Exception Handling

Your controllers are probably littered with try/catch blocks that each build their own error response, and your error bodies are inconsistent — some plain strings, some JSON, some raw stack traces. Centralize this.

The goal is that a controller method contains only the happy path. It calls the service, gets a result, returns it. If something goes wrong, the service throws, and a single registered handler turns that exception into the correct HTTP response.

**Requirements**

- Define a small hierarchy of custom exceptions that describe *what went wrong in business terms*, not what HTTP status to return. Something like: resource not found, invalid input, unauthenticated, forbidden, conflicting state.
- Register exception handlers once at application startup using Javalin's `app.exception(...)`. Each handler maps one exception type to one status code and response body.
- Every error response must share the same JSON shape. Include at least a timestamp, the status code, a human-readable message, and the request path. A field-level detail array is a good addition if you are also doing goal #2.
- Include a catch-all handler for `Exception`. It must return a generic 500 message and log the full stack trace server-side.
- Never expose stack traces, SQL text, or database error messages to the client. Those are reconnaissance material for an attacker and meaningless to a legitimate user.

**Done when**

- No controller method contains a try/catch block.
- Every error your API can produce comes back in the same JSON shape.
- Deliberately throwing an unexpected exception (a null dereference, say) returns your JSON 500 body rather than Javalin's default HTML error page.
- Requesting a reimbursement ID that does not exist returns 404, not 500.

---

## 5. Session Management with JWT Cookies

Your API currently has no idea who is calling it between requests. Most implementations work around this by passing a user ID in the URL or request body, which means any client can claim to be any user simply by changing a number. Replace that with signed tokens.

On successful login, the server issues a JSON Web Token identifying the user. The browser sends it back automatically on every subsequent request. The server verifies the signature and knows who is calling — without storing any session state.

**Requirements**

- On login, generate a JWT containing the user's ID, username, and role, plus an expiration time. One hour is a reasonable default.
- Sign the token with a secret key read from configuration or an environment variable. Do not hardcode the secret in your source, and do not commit it. Auth0's `java-jwt` and `jjwt` are both standard library choices.
- Return the token as an **HttpOnly** cookie, not in the response body. HttpOnly means client-side JavaScript cannot read it, which limits the damage of a cross-site scripting bug. Set `SameSite` and a path. Leave `Secure` off for local HTTP development.
- Add a `before` handler that reads the cookie, verifies the signature and expiration, and attaches the authenticated user to the Javalin context for downstream handlers to use.
- Logout must clear the cookie by overwriting it with an immediately-expiring value. There is no server-side session to destroy.
- Remove every place where a user identifies themselves via request body or path parameter. The token is now the only source of truth for caller identity.

**Notes**

A JWT is *signed, not encrypted*. Anyone holding the token can decode and read its contents — paste one into jwt.io and see for yourself. The signature guarantees the payload was not altered; it does not keep the payload private. Never put anything sensitive in a token.

Two gotchas that will cost you an afternoon if you do not know them in advance:

- Browser `fetch` does not send cookies on cross-origin requests unless you set `credentials: 'include'`.
- A server sending `Access-Control-Allow-Credentials: true` may not use `*` as its allowed origin. You must name the frontend's origin explicitly.

**Done when**

- Logging in sets a cookie you can see in your browser's dev tools, flagged HttpOnly.
- Requests to protected endpoints succeed with no user ID supplied by the client.
- Altering a single character of the token causes the request to be rejected.
- An expired token is rejected.
- Logging out causes subsequent protected requests to fail.

---

## 6. Role-Based Authorization

**Depends on goal #5.** Authorization is meaningless without reliable authentication.

Once the server knows who is calling, it must enforce what they are allowed to do. Currently nothing stops an employee from calling the approve endpoint directly and resolving their own reimbursement.

The aim is *declarative* authorization: each route states the role it requires, and a single piece of middleware enforces it. Scattering `if (user.getRole() != MANAGER)` checks through your controllers is what you are replacing.

**Requirements**

- Define the access rules for every endpoint. At minimum: registration and login are open to everyone; submitting, viewing, and editing one's own reimbursements requires any authenticated user; viewing all reimbursements and approving or denying them requires `MANAGER`.
- Attach the required role to the route definition rather than checking inside the handler. Javalin's mechanism for this changed between major versions — check the docs for the version in your `pom.xml`, as 5.x uses an `AccessManager` while 6.x uses route roles with a `beforeMatched` handler.
- Distinguish 401 from 403. **401 Unauthorized** means the caller is not authenticated — no token, or an invalid one. **403 Forbidden** means the caller is authenticated but lacks permission. Returning the wrong one makes debugging your own frontend much harder.
- Managers retain full employee capabilities. A manager can submit and edit their own reimbursements exactly as an employee does. Do not build a system where the manager role subtracts abilities.

**Note**

Role checks and ownership checks are different things, and you need both. "Is this user an employee?" does not answer "is this *their* reimbursement?" An employee editing another employee's pending request passes the role check and must still be rejected. Role checks belong in your middleware; ownership checks belong in the service layer, where the record is available to compare against.

**Done when**

- An employee calling an approve endpoint receives 403.
- A request with no cookie at all receives 401, not 403.
- An employee attempting to edit a different employee's pending reimbursement receives 403.
- A manager can successfully submit and edit their own reimbursement.
- No controller method contains a manual role comparison.

---

## 7. Connection Pooling with HikariCP

Every DAO method in your application currently opens a brand-new database connection and throws it away. Establishing a connection involves a TCP handshake, authentication, and session setup — it is one of the most expensive things your application does, and you are doing it on every single query.

A connection pool opens a fixed set of connections at startup and lends them out. Borrowing from the pool is effectively free.

**Requirements**

- Add the HikariCP dependency (`com.zaxxer:HikariCP`) and check Maven Central for the current version. Version 5.x and later require Java 11 or newer.
- Replace `DriverManager.getConnection(...)` in your connection factory with a single, statically initialized `HikariDataSource`. Exactly one pool should exist for the entire application — creating a pool per request defeats the purpose entirely and is worse than what you started with.
- Read the JDBC URL, username, and password from environment variables or a properties file. Do not hardcode credentials, and add the properties file to `.gitignore`.
- Configure a maximum pool size, a connection timeout, and a leak detection threshold.
- Audit every DAO method to confirm connections are obtained in a try-with-resources block. This was good practice before; it is now mandatory.

Configuration is the only part worth showing:

```java
HikariConfig config = new HikariConfig();
config.setJdbcUrl(System.getenv("DB_URL"));
config.setUsername(System.getenv("DB_USER"));
config.setPassword(System.getenv("DB_PASSWORD"));
config.setMaximumPoolSize(10);
config.setLeakDetectionThreshold(5000);
```

The rest of your DAO code does not change at all.

**Note**

`dataSource.getConnection()` returns a proxy object, and calling `close()` on it *returns the connection to the pool* rather than closing it. The method signature you call is identical, but the meaning has inverted. This makes leaks far more dramatic: a connection you fail to close is gone from the pool permanently, and once the pool is exhausted your application hangs rather than erroring.

Prove this to yourself. Set `maximumPoolSize` to 2, remove the try-with-resources from one DAO method, and call that endpoint three times. The third request will hang until the connection timeout expires. Then turn leak detection back on and watch it print a stack trace naming the exact method that borrowed and never returned. Restore your code afterward.

**Done when**

- `DriverManager` appears nowhere in your codebase.
- Exactly one `HikariDataSource` is created, at startup.
- No credentials are present in committed source.
- The application handles repeated requests to every endpoint without the pool degrading.
- You have observed the leak detector firing, and understand what it was telling you.

---

## 8. Pagination on Collection Endpoints

Your "get all reimbursements" endpoint returns every row in the table. That is fine with the twenty records in your test data and catastrophic with fifty thousand. Add pagination so clients request a bounded slice.

**Requirements**

- Accept `page` and `size` as query parameters, with sensible defaults applied when they are absent — the endpoint must still work when called with no parameters at all.
- Enforce a maximum page size. Without a cap, a client requesting `size=999999` has simply bypassed your pagination.
- Implement the slicing in SQL with `LIMIT` and `OFFSET`. Do not fetch every row and trim the list in Java; that does none of the work pagination exists to do.
- Return a response envelope, not a bare array. Include the page contents plus the current page number, page size, total element count, and total page count. The total requires a second `COUNT` query against the same filter conditions.
- Accept a `sort` parameter naming the column to order by, and a direction.
- Pagination must compose with your existing filters. Filtering by status and department, then paginating the result, has to work as one query.
- Invalid parameters — negative page numbers, unknown sort columns, non-numeric values — return 400 with a useful message.

**Notes**

Two things here are genuinely easy to get wrong.

**You cannot bind a column name as a prepared statement parameter.** JDBC parameters substitute *values*, not identifiers, so `ORDER BY ?` will not work. The temptation is to concatenate the user's input into the SQL string, which hands any caller direct SQL injection. The correct approach is to validate the requested sort field against an explicit allowlist of permitted column names, and reject anything not on it. This is the one place in this project where string-building SQL is unavoidable, and it is exactly why the allowlist is mandatory.

**Your sort must be deterministic.** If you order by a non-unique column — `status`, say — the database is free to return tied rows in any order it likes, and it may order them differently between two queries. The visible symptom is a record appearing on both page 1 and page 2 while another never appears at all. Always append a unique tiebreaker such as `id` as the final sort term.

**Done when**

- Requesting page 1 and page 2 returns disjoint sets of records with no duplicates and no omissions.
- Requesting a page beyond the end returns an empty array with correct metadata, not an error.
- The response includes enough metadata for a client to render page controls.
- Requesting `sort=password` or `sort=1;DROP TABLE users` returns 400 rather than executing.
- Pagination and status filtering work together in a single request.
