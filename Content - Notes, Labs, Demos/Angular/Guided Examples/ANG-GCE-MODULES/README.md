# ANG-GCE-MODULES
ANG-GCE-MODULES is a simple angular project we can use to explore Angular's "Feature Modules". We will see how modules can be used to group dependencies which are needed across many closely related components. 

If you clone this project and want to run it yourself, remember to get the node dependencies after cloning with the CLI command: `npm install`.

### Version Information
|  |  |
| ----------- | ------- |
| Angular CLI | 17.0.7  |
| Node        | 20.12.0 |
| npm         | 10.5.0  |

## Feature Modules
Recall from ANG-GCE-HELLO that each time we nest one component within another, the parent component must be "aware" of that child. The same goes for any other things the component depends on, such as services. In ANG-GCE-HELLO we made the parent aware of the child by including that child's TypeScript class in the `imports` array of the parent. When our application starts to get more complex, these arrays can grow to be very large. It's easy to make a mistake here and tedious to keep them correct. When we are working with closely related components (all part of similar functionality in our app) it makes sense to handle these dependencies as a set. We can group dependencies in a module, including services, pipes, components, or even other modules. Those dependencies can then be exported with the `exports` array, making them available anywhere the module is available. We can then include that module in an `imports` array instead of any of the individual dependencies.

Take a look at the `imports` array in the `@Component` decorator of the `AppComponent` class. Nested within this component are the three pokemon components (seen in `app.component.html`), but none of them are included in this array. Instead we have the `PokemonModule`. 

In the folder `src/app/pokemon/` we see `pokemonModule.ts`. This is the module class. Just like a component class, it is a TypeScript class with a "decorator" which tells Angular what to do with it. The `@NgModule` decorator contains an `imports` array, which works exactly the same as those in the component decorators. We also see an `exports` array which makes those items available anywhere this module is available.

### JS/TS Imports
Don't confuse these arrays with `import` and `export` statements commonly found in JavaScript and TypeScript files. These are similar mechanisms, but one is part of JavaScript and the other is part of Angular.

```JS
import { PikachuComponent } from './pikachu/pikachu.component';
```
This statement is a JavaScript `import` statement, which simply links this file with the referenced one in order to share the PikachuComponent class code. That class is appended with the `export` keyword:

```JS
export class PokemonModule { }
```
The `imports` and `exports` arrays do something very similar, but this is specific to Angular. You'll see that these two systems work together. JS imports link the files together so that the code present in one file is also available in the other. It is necessary to `export` the code from one file and then `import` it into another before we can even include that class in the `imports` array. 

Keep in mind that there are two important systems working in tandem with very similar keywords that do very similar things.

## Standalone Components & Dependency Bloat
Prior to Angular version 17 all projects had at least one module, the root module, which was required. All components had to be part of a module. The old module system led to "dependency bloat". This has led to a new paradigm from V17 onward, "standalone components". Components no longer need to be part of a module, and a new angular project won't include a root module. We can and should still use modules where appropriate.

The root module used to be part of the bootstrapping process, thus there have been some minor changes to how that process works. To explore a non-standalone project create a new project and use the `--no-standalone` flag:

`ng new --no-standalone PROJECT-NAME`

To read more about standalone components, see the [Angular Docs](https://angular.io/guide/standalone-components).

## Conclusion
Angular modules are decorated classes used to combine dependencies like components, pipes, services, and other modules. Prior to Angular version 17 all components had to be part of a module, but this is no longer the case. Now components can be "standalone", but we can still use "feature modules" to group closely related functionality. 

The most important part of the module is the `@NgModule` decorator and it's properties like `imports` and `exports`. There are use cases for adding code to the class itself, but it is very common for the class to simply be empty.