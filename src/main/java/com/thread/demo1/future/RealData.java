package com.thread.demo1.future;

public class RealData implements Data {

	
	protected final String result;
	
	public RealData(String param){
		System.out.println("执行业务开始。。。");
		StringBuffer sb = new StringBuffer();
		 
			sb.append(param);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		result = sb.toString();
		System.out.println("执行业务结束。。。");
	}
	
	@Override
	public String getResult() {
		return result;
	}

}
