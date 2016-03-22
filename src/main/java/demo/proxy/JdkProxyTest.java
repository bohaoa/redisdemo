package demo.proxy;

import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import demo.ioc.helloworld.HelloApi;
import demo.ioc.helloworld.HelloImpl;

public class JdkProxyTest {

	@Test
	public void testProxy() throws Throwable {
		HelloApi helloApi = new HelloImpl();
		MyInvocationHandler ih = new MyInvocationHandler(helloApi);
		HelloApi proxyObejct =  (HelloApi)ih.getProxy();
		proxyObejct.sayHello();
		
//		byte[] classFile = sun.misc.ProxyGenerator.generateProxyClass("$Proxy11", HelloImpl.class.getInterfaces());
//		FileOutputStream out = null;  
//        
//        try {  
//            out = new FileOutputStream("/temp");  
//            out.write(classFile);  
//            out.flush();  
//        } catch (Exception e) {  
//            e.printStackTrace();  
//        } finally {  
//            try {  
//                out.close();  
//            } catch (IOException e) {  
//                e.printStackTrace();  
//            }  
//        }  
	}
}
