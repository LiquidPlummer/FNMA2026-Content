# ANG-GCE-HELLO
ANG-GCE-HELLO is a simple Angular project we can use to explore the basics of the framework. We will look at a boilerplate Angular project, and discuss creating and building an projects.

If you clone this project and want to run it yourself, remember to get the node dependencies after cloning with the CLI command: `npm install`.

### Version Information
|  |  |
| ----------- | ------- |
| Angular CLI | 17.0.7  |
| Node        | 20.12.0 |
| npm         | 10.5.0  |


## New Project
The Angular CLI is how we will interact with our projects. We will use it to create projects, add components, modules, services, and other parts to our project, and finally build it for deployment.

Let's start with some basics, here is a command used to create a new Angular project:

```bash
ng new PROJECT-NAME
```
`ng` is shorthand for "Angular" and is the name of the CLI, the executable we are invoking to preform the work. `new` is what we want the CLI to do for us, which is generate a new boilerplate project. Finally, `PROJECT-NAME` is, clearly, the name we gave to the new project. This guided coding demo project was created with the command:

```bash
ng new ANG-GCE-HELLO
```

When you generate a new Angular project you get something similar to what we have in this example, except we have replaced the HTML in the file `app.component.html` and added two new components with a similar CLI command:

```bash
ng generate component child-one
ng generate component child-two
```

Later we will use the CLI to build and bundle the application.

## Project Files
In a new project we will find many files, which contain the basic code for a starter application. This pre-written code is called "boilerplate". 










## Bundling
Eventually, our project will get built and bundled. We can use different commands to do so, one will build it temporarily and launch it, the other will output a bundle.

```bash
ng build
```

This will create a folder and drop the bundle files into it. By default the destination is a folder called "dest". In that folder will be the bundle. Instead of having dozens or hundreds of files, the project is "bundled" into a handful of HTML and JS files, which are quicker to load into the browser. 

For convenience, we can also launch our application with the dev server, which builds a temporary version of the project and loads it into a browser:

```bash
ng serve --open
```

Go ahead and serve the project so we can explore it in a browser. After a moment your default browser should open and launch the SPA. Then, open your browser's dev tools (usually with the F12 key) and we can explore the HTML. 

If you build the project you should find the file `index.html` which is the same thing you would see in your browser's dev tools. This is the file containing the HTML your browser is rendering.

There are two important things in this file. The `<app-root>` and `<script>` tags.

```HTML
<body>
  <app-root></app-root>
  <script src="main.js" type="module"></script>
</body>
```
*Some HTML has been removed from this snippet for clarity.*

## Bootstrapping
Angular projects are single page applications, all the files in a project are bundled together at build-time. The bundle is sent to the browser, and the SPA is rendered. Bootstrapping refers to how each individual file comes together in the bundle, and how the browser executes the steps to show you the site.

Recall earlier that we served the application and looked at the HTML. The browser was rendering `index.html`, and We noted two important tags, `<script>` and `<app-root>`. 

### The HTML
In an Angular SPA we will write HTML in many files, and at runtime we will see much or all of it combined into a single HTML document. Here where we see the `<app-root>` tag 







Let's start with `index.html` and `main.ts`. Both of these files can be found under the `src/` folder. These files are generated automatically when you create a new angular project, and this is where the bootstrapping process begins.

`index.html` is a complete HTML document (but does not contain the HTML of the entire project). Take a look at the `<body>` section:

```HTML
<body>
  <app-root></app-root>
</body>
```

HTML is made up of tags which describe elements. The HTML document will be used to construct a DOM, which is the tree structure the HTML describes. Browsers interpret HTML and create a DOM. The only thing in the body of our project is `<app-root></app-root>` which are a pair of opening and closing tags representing the `App` component. In a moment we will go look at the files which make up that component, which include more HTML. In this way we are building up a larger DOM tree out of smaller sub-trees. `app-root` represents a sub-tree, more HTML nested within this element.

Next, let's consider `main.ts`:
```JS
bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
```
There are two main pillars to an Angular project, the HTML views and the TypeScript class. The HTML "templates" or "views" tell the browser where on the screen elements should be shown, the TypeScript class contains behaviors which tell the browser how to act. This code above invokes the angular `bootstrapApplication()` method, and passes it two arguments:

 - `AppComponent` which is a typescript class, this is the TypeScript code that goes with the `<app-component>` view.
 - `appConfig` which is a simple object containing configurations. We will discuss what this is another time.

And we're off! We have a bare bones HTML document which refers to other documents containing more HTML, and we have corresponding TypeScript classes to go with the nested HTML. These two things, an HTML template (or view) and a corresponding TypeScript class make up something called a "component" which is the building block of Angular projects.

## Components
Components are the basic building block of Angular Single Page Applications. This project contains three components:
 - `AppComponent`
 - `ChildOneComponent`
 - `ChildTwoComponent`

Each is made up of several files. Most importantly are the view and the class. The view is written in HTML, and the class is where we find TypeScript code.

We will explore components in greater detail in another demo. 

### Import vs Imports
In JavaScript and TypeScript we have the concept of "modules". At the top of any TS file in this project you will see one or more `import` statements.

```TypeScript
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
```
These are JS/TS modules, basically a language feature that lets us link files together so we can re-use code. 

The component classes in Angular projects are "decorated" with metadata. We will discuss this in more detail when we explore components, but for now take a look at the `imports` array in any of the component classes. This tells Angular to make those items available to the component.  

These two things are similar, and both use the word "import", but should not be confused. Finally, to add to the confusion there is an Angular feature called "modules" which has little to do with the JS/TS language feature of the same name.

So, keep in mind:

 - The `import` and `export` keywords are used for JS/TS modules, a language feature to link files together and share code
 - The `imports` array is used to tell Angular to provide dependencies to a class
 - Angular has "modules" too, which is a way to group similar components together

These concepts will be explored in greater detail in other demos.







## Conslusion
What we've seen here are the basic building blocks of a simple Angular SPA. We see how Angular references and bootstraps our syntax across many files, how the project gets assembled into a bundle of several files instead of tens or hundreds, and how we compose our SPA out of many smaller components. There's still a lot to see in future demos, like routing, modules, binding, services, and more!