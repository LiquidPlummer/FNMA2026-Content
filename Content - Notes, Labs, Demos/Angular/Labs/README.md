# Angular

- [Components](001%20-%20Components/)
- [Data Binding](002%20-%20Data%20Binding/)
- [Parent-Child Communication](003%20-%20Parent-Child%20Communication/)
- [Signals](004%20-%20Signals/)
- [Services](005%20-%20Services/)
- [Built-in Control Flow](006%20-%20Built-in%20Control%20Flow/)
- [Pipes](007%20-%20Pipes/)
- [Lifecycle Hooks](008%20-%20Lifecycle%20Hooks/)
- [HttpClient](009%20-%20HttpClient/)
- [Navigation and Routing](010%20-%20Navigation%20and%20Routing/)
- [Route Guards](011%20-%20Route%20Guards/)

---

Each lab is a standalone Angular project. Open its folder, run `npm install`, then `ng serve`. No lab
depends on the folder from a previous lab — where one builds on an earlier idea, its starter code
ships that state already built.

## Lab summaries

### 1. Components

**Concepts:** `ng generate component`, `@Component` metadata (`selector`, `templateUrl`, `styleUrl`,
`imports`), nesting by selector, interpolation with `{{ }}`.

**Steps:** Render a class property in the shell's template, generate two child components, give each
its own properties, then nest both in the shell by importing them and using their selectors.

### 2. Data Binding

**Concepts:** interpolation, property binding (`[value]`, `[disabled]`), event binding (`(click)`,
`(input)`), two-way binding with `[(ngModel)]` and `FormsModule`.

**Steps:** Build a text field and button the long way — a `[value]` binding plus an `(input)` handler —
then replace that pair with `[(ngModel)]` to see what the two-way syntax does for you.

### 3. Parent/Child Communication

**Concepts:** `@Input()`, `@Output()` with `EventEmitter`, data down / events up.

**Steps:** Nest a child in a parent, declare an input and bind the parent's object into it, add a
`[(ngModel)]` field for local edits, then emit the edit upward through an output the parent handles.

### 4. Signals

**Concepts:** `signal()`, `.set()`, `.update()`, `computed()`, and the signal forms `input()`,
`output()`, `model()`.

**Steps:** Take Lab 3's finished code and convert it piece by piece — state to signals, decorators to
signal functions, template reads to function calls — with no change in behaviour. Ends with a
`computed()` and a two-way bound `model()`.

### 5. Services

**Concepts:** `@Injectable({ providedIn: 'root' })`, `inject()`, sharing state between unrelated
components.

**Steps:** Start from two siblings that each hold their own copy of the same state and disagree with
each other. Generate a service, move the signal and its methods into it, inject it into both, and
confirm one shared instance fixes the mismatch.

### 6. Built-in Control Flow

**Concepts:** `@for` with `track`, `@empty`, `@if`/`@else if`/`@else`, `@switch`/`@case`/`@default`.

**Steps:** Render a hardcoded array with `@for`, branch on seat counts with an `@if` chain, branch on
course format with `@switch`, then add an `@empty` block and buttons that clear and restore the list.

### 7. Pipes

**Concepts:** `titlecase`, `uppercase`, `date`, `currency`, `json`, pipe arguments, chaining, and a
custom pipe implementing `PipeTransform`.

**Steps:** Import and apply each built-in pipe to the raw list from Lab 6, pass format arguments with
`:`, then generate a custom `initials` pipe and chain it with `uppercase`.

### 8. Lifecycle Hooks

**Concepts:** `ngOnInit`, `ngOnChanges` with `SimpleChanges`, `ngOnDestroy`, and hook order across
parent and child.

**Steps:** Log the constructor and each hook to the console and watch the order. Start a `setInterval`
in `ngOnInit`, watch it keep ticking after the component is destroyed, then fix the leak with
`clearInterval` in `ngOnDestroy`.

### 9. HttpClient

**Concepts:** `provideHttpClient()`, injecting `HttpClient`, `get<T>()`, the `async` pipe, and one
`catchError`.

**Steps:** Register the HTTP provider, replace a hardcoded array with a GET against a static JSON file
in `public/`, unwrap the observable with `async`, then break the URL on purpose to exercise the error
path. No server to run.

### 10. Navigation & Routing

**Concepts:** the routes array, `provideRouter()`, `<router-outlet>`, `routerLink`, a `:id` param read
via `ActivatedRoute`, and a wildcard 404 route.

**Steps:** Define routes for three components, add the outlet and a nav bar, link into a parameterised
detail route with `[routerLink]="['/courses', id]"`, read the param from the route snapshot, then
generate a 404 component and wire the `'**'` route last.

### 11. Route Guards

**Concepts:** a functional `CanActivate` guard, `inject()` inside a guard, `inject(Router)` and
`createUrlTree()` for redirecting.

**Steps:** Show that a hidden link isn't a protected route by pasting the URL directly. Generate a
guard, read the session service's `isLoggedIn` signal, wire it into the detail route with
`canActivate`, then upgrade a bare `false` into a redirect.
