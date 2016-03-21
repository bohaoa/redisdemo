package demo.ioc;

import demo.ioc.helloworld.HelloApi;

public class HelloImpl2 implements HelloApi {

    private String message;
    
    public HelloImpl2() {
        this.message = "111111Hello World!";
    }

    public HelloImpl2(String message) {
        this.message = message;
    }
    
    @Override
    public void sayHello() {
        System.out.println(message);
    }
    
}
