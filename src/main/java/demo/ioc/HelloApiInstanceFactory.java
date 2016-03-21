package demo.ioc;

import demo.ioc.helloworld.HelloApi;


public class HelloApiInstanceFactory {
    
    public HelloApi newInstance(String message) {
        return new HelloImpl2(message);
    }
    
}
