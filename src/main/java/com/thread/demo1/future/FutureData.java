package com.thread.demo1.future;

public class FutureData implements Data {

	protected RealData realData = null;
	protected boolean isReady = false;

	public void setRealData(RealData realData){
		if(isReady){
			return;
		}
		this.realData = realData;
		isReady = true;
		notifyAll();
	}
	
	@Override
	public String getResult() {
		while(!isReady){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return realData.result;
	}

}
