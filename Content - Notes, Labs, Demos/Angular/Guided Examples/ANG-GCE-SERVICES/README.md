# ANG-GCE-SERVICES
In this project we will look at Angular's services, which are classes with shared code. Services are injectable into the components and modules that rely on them. We can share functions, values, and features which are used across different parts of our SPA.

If you clone this project and want to run it yourself, remember to get the node dependencies after cloning with the CLI command: `npm install`.

### Version Information
|  |  |
| ----------- | ------- |
| Angular CLI | 17.0.7  |
| Node        | 20.12.0 |
| npm         | 10.5.0  |

## Services
In Angular we can create a service with the CLI command `ng generate service SERVICE-NAME`. This will generate a new file in your project called `SERVICE-NAME.service.ts` which contains the `ServiceNameService` class. Take a look at `calculator.service.ts` in this project where we find the `CalculatorService` class.

Services are typically classes, and contain the code we would want to share across our SPA. We can use these services to store and share information, or to expose functionality to multiple components across our application. These classes will get instantiated into objects and provided wherever needed. Usually a service is a singleton, so the service object's state is the same whever it is used, except in the case of using a component's `imports` array. We will discuss that below.

Service classes are decorated with the `@Injectable()` decorator. This hooks into Angular's Dependency Injection system.

## Dependency Injection
Dependency Injection (DI) is a technique where instead of instantiating dependencies inside a class, they are instantiated elsewhere and injected into the class. In this case DI is handled for us by Angular. There are two important parts to this: 
 - Provide/supply the dependency
 - Inject/consume the dependency

### Providing/Supplying Services
There are several methods to provide dependencies in Angular, though now there is one preferred method when it comes to services.

### Imports Arrays
We can provide a dependency in any `imports` array, just like a component, module, pipe, etc. Sometimes we may see a service provided in this way, but this is deprecated in newer standalone projects. We can see an example of this in `adder.component.ts`, though the import is commented out. 

```TypeScript
@Component({
  selector: 'app-adder',
  imports: [CalculatorService],
  templateUrl: './adder.component.html',
  styleUrl: './adder.component.css'
})
```
There is one major caveat to this method: when importing a service in a component's `imports` array it will not be a singleton. Each component that `imports` a service this way gets a unique instance. This means we can't share data and our behaviors could suffer from side-effects.

The same is true of the `imports` array in an Angular module. We could import and export the service, which would provide it anywhere the module is provided. Services imported into a module this way are singletons, and their state is shared across multiple components.

### ApplicationConfig Providers Array
Similar to the imports arrays, there is the `providers` array in the `ApplicationConfig` class, located in `app.config.ts` by default. Including the service class here will result in the singleton being available globally. 

```TypeScript
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    CalculatorService
  ]
};
```

### ProvidedIn
This last method is the preferred way to provide a service to components. Service classes are decorated with the `@Injectable` decorator. Take a look at the `CalculatorService` class in `calculator.service.ts`, at the very top we see:

```TypeScript
@Injectable({
  providedIn: 'root'
})
export class CalculatorService {
    // ...
}
```

Here Angular is using an `Injector` to provide the service, and because we are using the `'root'` injector it is provided globally. This will provide the singleton whever needed, and state will be shared with all consumers of the service. Both this method as well as the `ApplicationConfig` method above provide the singleton globally, but this method is preferred sue to Angular's optimizations including "tree-shaking".

We could create our own injectors, but the [Angular docs](https://angular.dev/guide/di/dependency-injection) tell us this is not commonly necessary. For the most part we can just create a service with the CLI using the command `ng generate service SERVICE-NAME` which will create the class, decorate it with `Injectable`, and reference the `'root'` injector.

Once provided, we still need to inject or consume the service.

### Injecting / Consuming Services
Once provided we need to consume the dependency. The service in this case is now available to us, it's just a matter of storing the reference to it. There are two simple methods for this: `constructor()` and `inject()`.

### Constructor Method
This method is very similar to the syntax we may be familiar with in other languages and frameworks, an example can be seen in `multiplier.component.ts`:

```TypeScript
export class MultiplierComponent {
  calculator: CalculatorService;

  constructor(calculator: CalculatorService) {
    this.calculator = calculator;
  }
}
```
We have a local reference of type `CalculatorService`, then in the constructor we take in an object of that type and assign it to the local reference variable. When building this component Angular will check for a provided service of that type, and if it finds one it passes in a reference to it. 

### Inject() Function Method
We can also use the `inject()` function, simply call `inject()` and pass it the name of the service class. Again, angular will check for a provided service of that type, and if one is found a reference to it is returned. An example can be seen in `divider.component.ts`:

```typescript
export class DividerComponent {
  calculator: CalculatorService = inject(CalculatorService);
}
```

## CalculatorService
The `CalculatorService` in this project provides four methods, `add()`, `subtract()`, `multiply()`, and `divide()`. It also shares two variables, `a` and `b`. Variables `a` and `b` are assigned values from the `AppComponent` view, our two input elements are each bound to the variables in the service:

```HTML
a: <input class="numberInput" type="number" min="-9" max="9" [(ngModel)]="this.calculator.a">
b: <input class="numberInput" type="number" min="-9" max="9" [(ngModel)]="this.calculator.b">
```

If we change the values in those inputs, the state of the service changes accordingly. This state is then used by the different components, which call a corresponding method. Take a look at `adder.component.html` and see that we have three one-way data bindings in the view: 

```TypeScript
<p>{{this.calculator.a}} + {{this.calculator.b}} = {{this.calculator.add()}}</p>
```

We display `a` and `b`, then we call `add()` and display the result. All four of the arithmetic components `AdderComponent`, `SubtractorComponent`, `MultiplierComponent`, and `DividerComponent` are binding to the same `a` and `b` in the service, and can access the shared methods there as well.