package com.thread.demo1.callablefuture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class FutureTest {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		System.out.println("Enter base directoy...");///Users/moetakara/Downloads/test
		String directory = in.nextLine();
		System.out.println("Enter keyword...");  //Foundation
		String keyword = in.nextLine();
		
		MatchCounter counter = new MatchCounter(new File(directory), keyword);
		FutureTask<Integer> task = new FutureTask<Integer>(counter);
		new Thread(task).start();
		try{
			System.out.println(task.get() + " matching files.");
		} catch (ExecutionException e){
			e.printStackTrace();
		} catch (InterruptedException e){
			e.printStackTrace();
		}

	}
	
	static class MatchCounter implements Callable<Integer> {
		
		private File directory;
		private String keyword;
		private int count;
		
		public MatchCounter (File directory, String keyword){
			this.directory = directory;
			this.keyword = keyword;
		}
		
		@Override
		public Integer call() throws Exception {
			count = 0;
			try{
				File[] files = directory.listFiles();
				ArrayList<Future<Integer>> results = new ArrayList<Future<Integer>>();
				
				for(File file : files){
					System.out.println(file.isDirectory());
					System.out.println("文件名："+file.getName());
					if(file.isDirectory()){
						MatchCounter counter = new MatchCounter(file, keyword);
						FutureTask<Integer> task = new FutureTask<Integer>(counter);
						results.add(task);
						new Thread(task).start();
					} else {
						if(search(file)){
							count++;
						}
					}
					System.out.println("文件遍历次数");
				}
				
				System.out.println("zhixingwan...");
				for(Future<Integer> result: results){
					try{
						
						count += result.get();
						System.out.println("执行类累加");
					} catch (ExecutionException e){
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e){
				
			}
			return count;
		}
		

		public boolean search(File file) throws IOException{
			Scanner in = new Scanner(new FileInputStream(file));
			boolean found = false;
			while (!found && in.hasNextLine()){
				String line = in.nextLine();
				if (line.contains(keyword)){
					found = true;
				}
			}
			in.close();
			return found;
		}
	}

}
