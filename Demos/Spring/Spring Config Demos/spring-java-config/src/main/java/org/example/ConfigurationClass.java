package org.example;

import org.example.components.ComponentBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ComponentScan("org.example.components")
public class ConfigurationClass {
    @Bean(name = "Example")
    @Scope("singleton")
    //@Autowired
    public ExampleBean exampleBeanBuilder(DependentBean dependentBean, ComponentBean componentBean) {
        //Maybe I can do something here..
        return new ExampleBean(dependentBean, componentBean);
    }

    @Bean(name = "Example2")
    //This one is a singleton by default
    public ExampleBean2 exampleBean2Builder(DependentBean dependentBean) {
        return new ExampleBean2(dependentBean);
    }

    @Bean(name = "Dependency")
    @Scope("prototype")
    public DependentBean dependentBeanBuilder() {
        return new DependentBean();
    }
}
