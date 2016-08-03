package com.reflection.demo1;

import java.lang.reflect.Method;

public class MyObject {
	
	private String value;
	
	public void setValue(String value){
		this.value = value;
	}
	
	public String getValue(){
		return this.value;
	}

	public static void main(String[] args) {
		Method[] methods = MyObject.class.getMethods();
		for(Method method : methods){
			System.out.println(method.getName());
		}

	}

}
