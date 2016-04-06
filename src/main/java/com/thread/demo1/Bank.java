package com.thread.demo1;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * 第一个：使用排它锁
 * 第二个：使用读／写锁  http://www.cnblogs.com/dolphin0520/p/3932906.html
 * @author moetakara
 *
 */
public class Bank {

	private final double[] accounts;
	
	public Bank(int n, double initialBalance){
		accounts = new double[n];
		for(int i=0; i<accounts.length; i++) {
			accounts[i] = initialBalance;
		} 
	}
	
	public void transfer(int from, int to, double amount){
		wl.lock();
		try {
			while(accounts[from] < amount) {
				wl.newCondition().await();
			}
			System.out.print(Thread.currentThread());
			accounts[from] -= amount;
			System.out.printf(" %10.2f from %d to %d", amount, from, to);
			accounts[to] += amount;
			System.out.printf(" Total Balance: %10.2f%n", getTotalBalance());
			
			wl.newCondition().signalAll();
		} catch (Exception ef){
			ef.printStackTrace();
		} finally {
			wl.unlock();
		}
	}
	
	public double getTotalBalance(){
		rl.lock();
		try {
			double sum = 0;
			for(double a: accounts){
				sum += a;
			}
			return sum;
		}  finally {
			rl.unlock();
		}
	}
	
	public int size(){
		return accounts.length;
	}
	
	
	//改造
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private WriteLock wl = rwl.writeLock();
	private ReadLock rl = rwl.readLock();
}
