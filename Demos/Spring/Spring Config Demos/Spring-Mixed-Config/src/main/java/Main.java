import com.example.componentScan.MyComponent;
import com.example.componentScan.MyService;
import com.example.javaConfig.MyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("Beans.xml");

        MyComponent mc = ac.getBean(MyComponent.class);
        //mc.test();

        MyService ms = ac.getBean(MyService.class);
        //ms.test();

        MyBean mb = ac.getBean(MyBean.class);
        mb.test();
    }
}
