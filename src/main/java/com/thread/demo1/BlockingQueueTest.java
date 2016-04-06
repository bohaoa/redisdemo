package com.thread.demo1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 阻塞队列 test
 * @author moetakara
 */
public class BlockingQueueTest {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		System.out.println("Enter base directoy...");///Users/moetakara/Downloads/test
		String directory = in.nextLine();
		System.out.println("Enter keyword...");  //Foundation
		String keyword = in.nextLine();
		
		final int FILE_QUEUE_SIZE = 10;
		final int SEARCH_THREADS = 100;
		
		BlockingQueue<File> queue = new ArrayBlockingQueue<File>(FILE_QUEUE_SIZE);
		
		FileEnumerationTask enumerator = new FileEnumerationTask(queue, new File(directory));
		new Thread(enumerator).start();
		for(int i=1; i<= SEARCH_THREADS; i++){
			new Thread(new SearchTask(queue, keyword)).start();
		}
		
	}
	
	static class FileEnumerationTask implements Runnable {
		
		public FileEnumerationTask(BlockingQueue<File> queue, File startingDirectory){
			this.queue = queue;
			this.startingDirectory = startingDirectory;
		}
		
		@Override
		public void run() {
			try{
				enumerate(startingDirectory);
				queue.put(DUMMY);
			} catch (InterruptedException ef){
				ef.printStackTrace();
			}
			
		}
		
		public void enumerate(File directory) throws InterruptedException{
			File[] files = directory.listFiles();
			for(File file : files){
				System.out.println(file.getName());
				if(file.isDirectory()) enumerate(file);
				else queue.put(file);
			}
		}
		
		public static File DUMMY = new File("");
		private BlockingQueue<File> queue;
		private File startingDirectory;
	}
	
	static class SearchTask implements Runnable {
		
		private BlockingQueue<File> queue;
		private String keyword;
		
		public SearchTask (BlockingQueue<File> queue, String keyword){
			this.queue = queue;
			this.keyword = keyword;
		}
		
		@Override
		public void run() {
			try{
				boolean done = false;
				while (! done){
					File file = queue.take();
					if(file == FileEnumerationTask.DUMMY){
						queue.put(file);
						done = true;
					}
					else {
						search(file);
					}
				}
			} catch (IOException ef1){
				ef1.printStackTrace();
			} catch (InterruptedException ef2){
				ef2.printStackTrace();
			} 
			
		}
		
		public void search(File file) throws IOException{
			Scanner in = new Scanner(new FileInputStream(file));
			int lineNumber = 0;
			while (in.hasNextLine()){
				lineNumber ++ ;
				String line = in.nextLine();
				if (line.contains(keyword)){
					System.out.printf("%s:%d:%s%n", file.getPath(), lineNumber, line);
				}
			}
			in.close();
		}
		
	}

}
