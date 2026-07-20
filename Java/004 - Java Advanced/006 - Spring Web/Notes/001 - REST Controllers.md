# REST Controllers

A **REST API** exposes an application's capabilities over HTTP: resources named by URLs, actions expressed by methods (GET reads, POST creates, PUT/PATCH updates, DELETE removes), data carried as JSON, outcomes signaled by status codes. In Spring Web (`spring-boot-starter-web`), the class that receives those requests is a **controller** — annotated methods mapped to routes, with the framework handling everything between the network socket and our method call. This is inversion of control at its most visible: Spring catches the request and calls *us*.

---

## A Controller

```java
@RestController
@RequestMapping("/api/invoices")                       // base path for the whole class
public class InvoiceController {

    private final InvoiceService service;              // injected, as ever

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @GetMapping                                        // GET /api/invoices
    public List<InvoiceResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")                               // GET /api/invoices/42
    public InvoiceResponse one(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping                                       // POST /api/invoices
    @ResponseStatus(HttpStatus.CREATED)                // 201, not the default 200
    public InvoiceResponse create(@RequestBody CreateInvoiceRequest request) {
        return service.create(request);
    }

    @DeleteMapping("/{id}")                            // DELETE /api/invoices/42
    @ResponseStatus(HttpStatus.NO_CONTENT)             // 204: done, nothing to say
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
```

*A full resource controller: routes declared in annotations, HTTP plumbing nowhere in sight.*

**`@RestController`** is a stereotype (a bean, component-scanned like any other) that additionally means *return values are the response body, serialized to JSON*. The **`@XxxMapping`** annotations bind methods to verb-plus-path combinations, composing with the class-level `@RequestMapping`. Behind the curtain, one servlet — the **DispatcherServlet** — receives every request, consults these mappings (discovered reflectively at startup, of course), converts inputs, invokes our method on a worker thread from a pool (the Concurrency topic, running in production), and converts the return value back. Jackson's `ObjectMapper` does both conversions — the reflective field-walking serializer we sketched in the Reflection topic, industrial edition.

---

## RESTful Shape

Routing is a design language, and the conventions carry real information:

- **URLs name resources — nouns, plural**: `/api/invoices`, `/api/invoices/42`, `/api/customers/7/invoices` (nesting for ownership). Not `/getInvoice`, not verbs — the *method* is the verb.
- **Status codes carry the outcome**: 200 OK (read/update), **201 Created** (POST that creates), **204 No Content** (successful nothing-to-return), 400 (malformed request), **404** (no such resource), 409 (conflict), 500 (our bug). `@ResponseStatus` sets the success code; error codes come from exception handling (next lesson).
- **GET is safe and repeatable** — never mutate on GET; caches and crawlers assume it.

Following the conventions isn't pedantry: clients, tooling, and the next developer all navigate by them — an API that returns 200 with `{"error": "not found"}` breaks every assumption downstream.

---

## Trying It

Boot the application and the endpoints are live on 8080:

```bash
curl http://localhost:8080/api/invoices
curl http://localhost:8080/api/invoices/42
curl -X POST http://localhost:8080/api/invoices \
     -H "Content-Type: application/json" \
     -d '{"customer": "Ada Corp", "total": 150.00}'
curl -i -X DELETE http://localhost:8080/api/invoices/42      # -i shows the 204
```

*The API exercised from the command line — `curl` (or Postman/HTTPie) is the REST developer's REPL.*

Controller tests get a dedicated slice, symmetric with `@DataJpaTest`: **`@WebMvcTest`** boots only the web layer, mocks the service (`@MockitoBean`), and drives requests through `MockMvc` without a real network — the Unit Testing topic's isolation discipline applied to HTTP:

```java
@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean InvoiceService service;

    @Test
    void missingInvoiceIs404() throws Exception {
        when(service.findById(99L)).thenThrow(new InvoiceNotFoundException(99L));

        mvc.perform(get("/api/invoices/99"))
           .andExpect(status().isNotFound());
    }
}
```

*A web-slice test: stubbed service, simulated request, assertion on the HTTP contract itself.*

The controller above quietly assumed several things we haven't covered: how `@PathVariable` and `@RequestBody` conversion actually work and what else can be bound (query params, headers), how invalid input gets rejected, and how that `InvoiceNotFoundException` becomes a 404. Request and response handling in full — next.
