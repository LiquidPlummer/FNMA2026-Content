# SPR-GCE-ANNO-CONFIG
In this example we will explore Component Scanning, a way to use annotations to configure a Spring app. Component scanning is one of several ways to configure Spring, along with Java configuration and XML configuration.

If you clone this project and want to run it yourself, remember to get the maven dependencies after cloning with the
CLI command: `mvn install`.

### Version Information
| Software       | Version |
|----------------|---------|
| SpringBoot     | 3.4.1   |
| Spring Web     | 6.1.14  |
| Java           | 21      |

## Components
In this context, components refers to beans. Components are classes which are marked with component scanning annotations, interpreted by Spring, and turned into beans. 

## @ComponentScan & @SpringBootApplication
Spring Boot is an opinionated way to start a Spring project, and one of its assumptions is that we would configure the application with component scanning. The `@ComponentScan` annotation is used to inform Spring about this. In Spring Boot applications we will see the `@SpringBootApplication`, which implies `@ComponentScan` in addition to other things. Either of these annotations can be applied to the class where `SpringApplication.run()` is called, enabling component scanning.

Either of these annotations can have an attribute that tells spring where to scan. We include the correct attribute in the annotation, and provide a list of locations. For `@SpringBootApplication` the attribute is `scanBasePackages`, for `@ComponentScan` the same attribute is called `basePackages`.

```Java
@SpringBootApplication(scanBasePackages = {
		"com.revature.configurations",
		"com.revature.controllers",
		"com.revature.repositories",
		"com.revature.services"
})
```

```Java
@ComponentScan(basePackages = {"com.example.javaConfig"})
```
Note the syntax here, comma separated strings inside curly braces. Each string is a package which should be scanned by Spring looking for beans defined with annotations.


## Stereotypes
The next annotations to lean are called the stereotype annotations, each describe a specific type of bean. These are applied to classes to indicate that Spring should proxy objects of that class as beans. These include `@Component`, `@Controller`, `@Service`, and `@Repository`.

### @Component
`@Component` is the generic stereotype, and doesn't imply any special behaviors. This one is for any bean which isn't one of the more specific ones below.

### @Controller
`@Controller` is the stereotype for web controllers. These classes contain request and error handlers which listen for and respond to HTTP requests.

### @RestController
`@RestController` isn't actually a stereotype itself, but it implies `@Controller`. `@RestController` is a combination which implies that the class is a `@Controller`, so Spring will scan and proxy it. It also implies `@ResponseBody`, and automatically applies that annotation to all methods in the class.

### @Service
`@Service` is a stereotype for service beans, these are beans which offer functionality to controllers. Services expose business logic to controllers, and often utilize repositories.

### @Repository
`@Repository` is an annotation applied to certain interfaces to tell spring to turn that interface into a repository bean. Repositories encapsulate persistence operations, allowing us to read and write to data sources (commonly relational databases).

## Explore the Code
Take a look at the classes in this project. You will see one of each stereotype, component, controller, service, and repository. There is also the `MyEntity` class which is required for `MyRepository`.

There's not much functionality here yet. If you run the application you should see several of the beans print to the console as their constructors are invoked. This is Spring initializing itself, and its Application Context (which is also the Bean Factory). When Spring is launching and initializing the context, it scans for the beans and begins proxying and wiring them.

## More Annotations!
So far we have only discussed the basics for getting our components scanned and proxied. There are numerous other annotations which inform Spring about other important configuration settings. We will explore these other annotations when we explore controllers, services, repositories, and entities.

## Mixing configurations
The different ways of configuring Spring aren't mutually exclusive, they can actually be mixed.

#### Add XML Config Into Annotation Config
Change the type of `ApplicationContext` to the more specific `ClassPathXmlApplicationContext`, and pass the file path to the constructor.
```Java
ApplicationContext ac = new ClassPathXmlApplicationContext("Beans.xml");
```

#### Add Java Config Into Annotation Config
With Java based configuration we mark a class with the `@Configuration` annotation, and we mark its methods with the `@Bean` annotation. All we have to do is tell Spring to scan that class, Spring will scan and realize it is a Java configuration class and will act accordingly.
```Java
//com.revature.Main

@SpringBootApplication(scanBasePackages = {
		"com.revature.javaconfig"
})
public class Main {
	public static void main(String[] args) {
		ApplicationContext ac = SpringApplication.run(Main.class, args);
	}
}
```

```Java
//com.revature.javaconfig.Configurer

package com.example.javaConfig;

@Configuration//this annotation tells spring that this is a factory class which can create beans
public class Configurer {

    @Bean("myBean")
    public MyBean getMyBean() {
        return new MyBean();
    }
}
```

```Java
public class MyBean {
    public MyBean() {
        System.out.println("MyBean constructor!");
    }
}
```