package demo.resource;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import demo.resource.bean.ICar;

public class ApplicationContextTest {

	public static void main(String[] args) {
		new ApplicationContextTest().testApplicationContext1();
	}
	
	public void testApplicationContext2(){
		ApplicationContext ac = new ClassPathXmlApplicationContext("resources/applicationContext-resource.xml");
		ICar icar = ac.getBean(ICar.class);
		icar.hello();
	}
	
	public void testApplicationContext1(){
		ApplicationContext ac = new FileSystemXmlApplicationContext("src/main/resources/resources/applicationContext-resource.xml");
		ICar icar = ac.getBean(ICar.class);
		icar.hello();
	}
	
	public void testBeanFactory(){
		// 1. BeanFactory
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource res = resolver.getResource("resources/applicationContext-resource.xml");
		BeanFactory bf = new XmlBeanFactory(res);
		ICar icar = bf.getBean(ICar.class);
		icar.hello();
	}

}
