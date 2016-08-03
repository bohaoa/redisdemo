package com.thread.demo1.future;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Client client = new Client();
		Data data = client.request("name");
		System.out.println("请求完毕！");
		
		System.out.println("业务等待时间！");
		try {
			Thread.sleep(2000);
			System.out.println("主线程运行结束！");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(data.getResult());
		

	}

}
