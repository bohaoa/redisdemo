package com.reflection.demo1;

import java.lang.reflect.Method;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
			Class c = Class.forName("com.reflection.demo1.MyObject");
			
			Method[] methods = c.getMethods();
			for(Method method : methods){
				System.out.println(method.getName());
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
