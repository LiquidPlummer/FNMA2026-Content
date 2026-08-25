# Request & Response Handling

A controller method's parameters and return value are a *declaration* of the HTTP exchange: annotations state where each input comes from, and the return (plus exception mappings) states what goes back. This lesson inventories the binding annotations, adds validation, and closes the loop on turning exceptions into proper error responses.

---

## Binding Inputs

Each piece of a request has an annotation that delivers it as a typed parameter:

```java
@GetMapping("/api/invoices/{id}")                       // path segment
public InvoiceResponse one(@PathVariable Long id) { ... }

@GetMapping("/api/invoices")                            // query string: ?status=PAID&page=0
public Page<InvoiceResponse> search(
        @RequestParam(defaultValue = "PENDING") InvoiceStatus status,
        @RequestParam(required = false) String customer,
        Pageable pageable) {                            // page/size/sort params — bound automatically
    ...
}

@PostMapping("/api/invoices")                           // JSON body
public InvoiceResponse create(@RequestBody CreateInvoiceRequest request) { ... }

@GetMapping("/api/reports")
public Report report(@RequestHeader("X-Tenant-Id") String tenant) { ... }   // header
```

*The binding kit: `@PathVariable` for URL segments, `@RequestParam` for the query string, `@RequestBody` for JSON, `@RequestHeader` for headers.*

Conversion is automatic and typed — `"42"` becomes `Long`, `"PAID"` becomes the enum (via `valueOf`, so case matters), and a failed conversion is a 400 before our code runs. Conventions that keep APIs sane: **path variables identify** the resource, **query params filter/paginate/sort** optional aspects (`required = false` and `defaultValue` mark them honestly), and **the body carries the payload** for POST/PUT — exactly one `@RequestBody`, deserialized by Jackson into whatever class we declare (the DTO story of the next lesson).

---

## Validation at the Door

Deserialization only proves the JSON parsed — not that `total` is positive or `customer` non-blank. **Bean Validation** (`spring-boot-starter-validation`) declares the rules on the class, and `@Valid` enforces them during binding:

```java
public record CreateInvoiceRequest(
        @NotBlank(message = "customer is required")
        String customer,

        @NotNull @Positive(message = "total must be positive")
        BigDecimal total,

        @Size(max = 500)
        String memo) {}

@PostMapping
public InvoiceResponse create(@Valid @RequestBody CreateInvoiceRequest request) { ... }
```

*Constraint annotations on the request record; `@Valid` triggers them — failures become a 400 automatically.*

This is the fail-fast validation doctrine from Unit 2, relocated to the system boundary where it belongs: reject bad input in the doorway, with field-level messages, before any business logic runs. The standard constraints (`@NotNull`, `@NotBlank`, `@Positive`, `@Size`, `@Email`, `@Past`...) cover most needs; custom constraints are — no surprise — an annotation plus a validator class.

---

## Shaping Responses

Returning a value serializes it with an implied 200 (or the `@ResponseStatus` override). When status, headers, or conditional responses need runtime control, **`ResponseEntity<T>`** is the explicit builder:

```java
@PostMapping
public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest req) {
    InvoiceResponse created = service.create(req);
    return ResponseEntity
            .created(URI.create("/api/invoices/" + created.id()))   // 201 + Location header
            .body(created);
}
```

*`ResponseEntity`: full control of the status line, headers, and body when the fixed annotations aren't enough.*

---

## Exceptions Become Status Codes

The missing piece from last lesson: service code throws domain exceptions (`InvoiceNotFoundException` — the custom-exception discipline from Unit 3), and something must translate them to HTTP. That something is a **`@RestControllerAdvice`** — a global exception-to-response mapping, kept out of every controller:

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvoiceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(InvoiceNotFoundException e) {
        return new ApiError("NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)      // @Valid failures land here
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalid(MethodArgumentNotValidException e) {
        var details = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();                                         // streams, naturally
        return new ApiError("VALIDATION_FAILED", String.join("; ", details));
    }
}

public record ApiError(String code, String message) {}
```

*Centralized translation: each exception type maps to a status and a consistent error body, API-wide.*

This completes the exception architecture Unit 3 designed: methods throw in domain terms, nothing catches mid-flight, and the *boundary* translates — here, into status codes and a uniform error shape. Two disciplines finish the job: never leak internals (stack traces, SQL, class names) into error bodies — log the details (with the exception object, preserving the cause chain), return the sanitized summary; and give every error the same envelope (`code`/`message`), so clients parse one format.

Requests in, validation, responses out, errors mapped. What's conspicuously absent is *what type* travels in those bodies — we've been writing `InvoiceResponse`, not `Invoice`, without justification. That distinction — DTOs versus entities, and the mapping between them — is next.
