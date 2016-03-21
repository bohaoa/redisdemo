package demo.ioc.helloworld;

public class HelloImpl implements HelloApi {

    @Override
    public void sayHello() {
        System.out.println("Hello World!");
    }
    
}
