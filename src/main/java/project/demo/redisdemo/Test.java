package project.demo.redisdemo;

import java.util.Vector;

public class Test {
	
	public static int num = 0;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		gctest1();
	}

	public static String test(String str){
		Vector v= new Vector();
		for(int i=0; i<10; i++){
			byte[] b = new byte[1024*1024];
			v.add(b);
			System.out.println(Runtime.getRuntime().freeMemory()/1024/1024+"M");
		}
		return "";
		
	}
	
	public static void gctest1(){
		byte[] b1,b2,b3,b4;
		b1 = new byte[1024*1024];
//		b2 = new byte[1024*1024];
//		b3 = new byte[1024*1024];
//		b4 = new byte[1024*1024];
	}
}
