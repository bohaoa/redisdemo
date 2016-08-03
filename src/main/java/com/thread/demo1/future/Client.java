package com.thread.demo1.future;

public class Client {


	public Data request(final String querystr){
		final FutureData future = new FutureData();
		new Thread(){
			public void run(){
				RealData realData = new RealData(querystr);
				future.setRealData(realData);
			}
		}.start();
		return future;
		
	}
}
