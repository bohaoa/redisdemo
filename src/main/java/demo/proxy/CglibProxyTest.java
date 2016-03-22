package demo.proxy;

import java.lang.reflect.Method;

import org.junit.Test;

import demo.ioc.helloworld.HelloApi;
import demo.ioc.helloworld.HelloImpl;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CglibProxyTest {

	@Test
	public void testProxy() throws Throwable {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(HelloImpl.class);
		enhancer.setCallback(new MethodInterceptorImpl());
		HelloApi my = (HelloImpl) enhancer.create();
		my.sayHello();
	}

	private static class MethodInterceptorImpl implements MethodInterceptor {
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			System.out.println(method);
			proxy.invokeSuper(obj, args);
			System.out.println(method);
			return null;
		}
	}
}
