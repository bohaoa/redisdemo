package com.thread.demo1.callablefuture;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class CallableAndFuture {

	public static void main(String[] args) {
		new CallableAndFuture().test2();
	}
	
	public void test0(){
		Callable<Integer> call = new Callable<Integer>(){
			@Override
			public Integer call() throws Exception {
				return new Random().nextInt();
			}
		};
		FutureTask<Integer> task = new FutureTask<Integer>(call);
		new Thread(task).start();
		
		try {
			System.out.println(task.get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void test (){
		ExecutorService threadPool = Executors.newSingleThreadExecutor();
		Future<Integer> future = threadPool.submit(new Callable<Integer>(){
			@Override
			public Integer call() throws Exception {
				return new Random().nextInt();
			}
		});
		
		try {
			System.out.println(future.get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void test1(){
		ExecutorService threadPool = Executors.newCachedThreadPool();
		CompletionService cs = new ExecutorCompletionService<Integer>(threadPool);
		for(int i=0; i<5; i++){
			final int takeID = i;
			cs.submit(new Callable<Integer>(){
				@Override
				public Integer call() throws Exception {
					return takeID;
				}
			});
		}
		try {
			for(int i=0; i<5; i++){
				System.out.println(cs.take().get());
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void test2(){
		ArrayList<Future<Integer>> results = new ArrayList<Future<Integer>>();
		for(int i=0; i<5; i++){
			final int endiD = i;
			FutureTask<Integer> future = new FutureTask<Integer>(new Callable<Integer>(){
				@Override
				public Integer call() throws Exception {
					return endiD;
				}
			});
			results.add(future);
			new Thread(future).start();
			
		}
		try {
			for(int i=0; i<results.size(); i++){
				Future<Integer> f = results.get(i);
				System.out.println(f.get());
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
