# ANG-GCE-ROUTING
ANG-GCE-ROUTING is a simple Angular project we can use to explore "routing", the technique SPAs use to display different components. We will see how routing works to replace hypertext links, and how route guards work to control access.

If you clone this project and want to run it yourself, remember to get the node dependencies after cloning with the CLI command: `npm install`.

### Version Information
|  |  |
| ----------- | ------- |
| Angular CLI | 17.0.7  |
| Node        | 20.12.0 |
| npm         | 10.5.0  |

## Routing
In a traditional web site made up of multiple pages we would use the HTML anchor tag to create a hypertext link to another document. The browser would fetch that document, parse it into a DOM, and render it. Angular is a single page application framework, the whole site is in one single page. We can still use links to navigate around the site, but it works differently. In Angular we create "routes" which represent a set of components to insert into the DOM. Instead of fetching new HTML to create a new DOM, we modify the existing DOM to show different components. This is one of the core functionalities of SPAs.

### Routes

A `Route` is an object which has several properties. Take a look at `src/app/app.routes.ts`, you'll see the `routes` variable, which is of type `Routes` and holds an array of `Route` objects. Each of these must have a `path` and `component` property. In addition to these required properties we can add optional ones, such as `canActivate`, which is used for route guarding.

The `path` property is a URI string which represents a path to some content. When you click the button to navigate to a route you will see the URL changes to include this URI after the domain. Routing is tied into the browser's API in order to fully emulate traditional HTML navigation. We can actually modify the URL and cause the SPA to navigate to that route.

`component` is the property which tells Angular what content should be shown at that route. When we click a link that navigates us to the `two` path, Angular will then add the `RouteTwoComponent` component to the DOM, and render it. When we navigate to a different path, this component will be replaced with whatever that other route would show.

### Bootstrapping Routes
Recall that during the bootstrapping process `bootstrapApplication()` is called, passing `appConfig` as a parameter. `appConfig` in turn contains a `providers` array where we see the `provideRouter()` function taking in our `routes` array. We can define our routes in `app.routes.ts` and they will be used by Angular's routing system.

### Router Links
Now that we have our routes defined and bootstrapped, we just need to create links which cause the routing system to navigate to our routes. We do this with the `routerLink` attribute. Take a look at `app.component.html`, you'll see three `button` elements. Each of these has a `routerLink` attribute with a value which corresponds to one of our routes. We can add this attribute to many different types of elements including buttons, paragraphs, anchors, and images:

```HTML
<button routerLink="one">Click here!</button>
<p routerLink="one">Click here!</p>
<a routerLink="one">Click here!</a>
<img routerLink="one" src="ClickHere.png">
```
Each of these elements will act like a link, invoking the router when clicked. `routerLink` is is basically a click event listener which invokes the angular router with a path. `routerLink` requires the component to have the `RouterModule` dependency available to it. Take a look at `app.component.ts` and you'll see `RouterModule` in the `imports` array. You'll also see `RouterOutlet` which is another important piece of this system.

### Router Outlet
Now we know how to define routes and how to invoke the Angular router. We know that when we do invoke the router Angular will modify the DOM in order to add components and render them on screen. The last thing to do is define where in the DOM tree the components will be inserted, and thus where on screen they are shown. We simply add a `<router-outlet>` element, which is a placeholder indicating where the components will be inserted. In this project the outlet is at the very bottom of `app.component.html`, and the components will be rendered below the rest of the elements.

We can have multiple outlets which render different routes. If we wanted to do that we just need to define a name for the outlet and specify that outlet in the `Route` object. For example:
```HTML
<router-outlet name="routerName"></router-outlet>
```

```ts
export const routes: Routes = [
    {path: 'path-URI', component: SomeComponent, outlet:'routerName'}
];
```
The `name` attribute on the `<router-outlet>` element should match the `outlet` property of the `Route` object.

## Route Guards
Earlier it was mentioned that we could just type a route into the URL bar to access it. What if we didn't want someone to be able to access a specific route? Whether we are typing the route, or clicking a `routerLink` we will often want to control access to these routes. We can do this with route guards. 

A route guard is just a function which is invoked when we try to navigate to a route. Commonly they will return `true` or `false` which allows or denies access respectively. We could also return a `UrlTree` which causes different behavior. See the Angular docs for more info on this: https://angular.io/api/router/CanActivateFn. In this demo we will stick with returning a boolean value from our route guards.

First let's take a look at the `routes` array in `app.routes.ts` again. Each of the routes has a property `canActivate` with an array. That array is a set of guards, and each must return true for Angular to complete the navigation. Route one has `routeLockGuard`, two uses `RouteLockService`, and three `canOpenRoute`. All three route guards use basically the same logic, but use some slightly different syntax spread across two files: `route-lock.guard.ts` and `route-lock.service.ts`. Each one invokes a funciton of type `CanActivateFn` and either returns `true` or `false`. The only thing necessary to have a working route guard is to have a funciton which conforms to the `CanActivateFn` signature. We have three such functions in this demo. All three use the `locks` object in the `RouteLockService`. A service is not required for route guards, but is a convienent way for us to make this state available in several places.

### Route 1
Route one's guard `routeLockGuard` is a function found in `route-lock.guard.ts`. You can create route guards with the Angular CLI command `ng generate guard GUARD-NAME`, which is how this file was created. This function, like the others, just returns the value in the `locks` object inside the `RouterLockService` class found in `route-lock.service.ts`.

We inject the `RouteLockService` here because it holds the `locks` object, which indicates if the route guard returns `true` or `false`.

### Route 2
This time we add `RouteLockService` to the `canActivate` array. `RouteLockService` is a class, not a function, but it contains a method called `canActivate` which conforms to the `CanActivateFn` signature. Angular will invoke this method when we try to use that route. The logic for this guard is exactly the same as above, except we don't need to inject anything. This is the service we injected earlier, and contains the state we are interested in.

### Route 3
Lastly for route 3 we see the `canOpenRoute` listed in the `canActivate` array. This is a funciton like `routeLockGuard` above. It is defined in `route-lock.service.ts`, but is outside of the `RouteLockService` class. Scroll all the way down to find it. Just like for route 1 we have a function which conforms to the `CanActivateFn` signature. Similarly we need to inject the service here so we can access the `locks` array.

### Services vs. Guards
We use the CLI command `ng generate guard GUARD-NAME` in order to have Angular create a route guard for us. Try it in a project and you will see a new file get created with a funciton conforming to the `CanActivateFn` signature. Actually, there are several different types of guards with different signatures, but this is not important to us right now. See the Angular docs for info about the other guard types. We don't actually need to create this file, however. All that matters is that we have a function which conforms to the proper signature. As we have seen that function can be anywhere, including inside a service.