package com.example.javaConfig;

import com.example.componentScan.MyService;
import com.example.xmlConfig.MyClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration//this annotation tells spring that this is a factory class which can create beans
@ImportResource({"classpath:Beans.xml"})//this line brings in the XML config, which may not be required here...
public class Configurer {

    @Bean("myBean")
    public MyBean getMyBean(MyService myService) {
        return new MyBean(myService);
    }
}
