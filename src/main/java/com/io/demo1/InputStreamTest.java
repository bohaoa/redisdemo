package com.io.demo1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InputStreamTest {

	public static void main(String[] args) {
		// new
		// InputStreamTest().testbyteRead("/Users/moetakara/Downloads/test/TEST1");
		// new
		// InputStreamTest().testbyteWrite("/Users/moetakara/Downloads/test/TEST2");
		// new
		// InputStreamTest().testBufferByteRead("/Users/moetakara/Downloads/test/TEST1");
		// new
		// InputStreamTest().testBufferByteWrite("/Users/moetakara/Downloads/test/TEST3");
		// new
		// InputStreamTest().testStringRead("/Users/moetakara/Downloads/test/TEST1");
		// new
		// InputStreamTest().testStringWrite("/Users/moetakara/Downloads/test/TEST4");
		// new
		// InputStreamTest().testNioWrite("/Users/moetakara/Downloads/test/TEST5");
		//new InputStreamTest().testNioRead("/Users/moetakara/Downloads/test/TEST1");
		new InputStreamTest().testFiles("/Users/moetakara/Downloads/test/TEST1");
	}

	/**
	 * 普通字节的读
	 */
	public void testbyteRead(String path) {
		try {
			InputStream finput = new FileInputStream(path);

			// 单个字节的读取
			int ch = 0;
			while ((ch = finput.read()) != -1) {
				System.out.println((char) ch);
			}

			// 字节数组读取
			// byte[] b = new byte[1024];
			// int len = 0;
			// while ((len = finput.read(b))!= -1) {
			// System.out.println(new String(b, 0, len));
			// }

			// 标准大小数组读取
			// byte[] t = new byte[finput.available()];
			// finput.read(t);
			// System.out.println(new String(t));

			finput.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 字节流的写入操作
	 * 
	 * @param path
	 */
	public void testbyteWrite(String path) {
		try {
			OutputStream os = new FileOutputStream(path);
			os.write("1111111111111\r\nsadasa阿达撒".getBytes());

			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 带缓冲的字节读取
	 * 
	 * @param path
	 */
	public void testBufferByteRead(String path) {
		try {
			InputStream fi = new BufferedInputStream(new FileInputStream(path));
			byte[] t = new byte[fi.available()];
			fi.read(t);
			System.out.println(new String(t));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 带缓冲带字节写入
	 * 
	 * @param path
	 */
	public void testBufferByteWrite(String path) {
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(path));
			os.write("1111111111111\r\nsadasa阿达撒".getBytes());

			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 字符的读入
	 * 
	 * @param path
	 */
	public void testStringRead(String path) {
		try {
			// InputStreamReader ir = new InputStreamReader(new
			// FileInputStream(path));
			// char[] c = new char[1024];
			// int len = ir.read(c);
			// System.out.println(new String(c, 0, len));

			// 带缓冲带字符读
			// BufferedReader br = new BufferedReader(new InputStreamReader(new
			// FileInputStream(path)));
			// String s;
			// while ((s = br.readLine()) != null){
			// System.out.println(s);
			// }
			// br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 字符带写入
	 * 
	 * @param path
	 */
	public void testStringWrite(String path) {
		try {
			// OutputStreamWriter osw = new OutputStreamWriter(new
			// FileOutputStream(path));
			// osw.write("asdasdsada\nsadassaaaaa");
			//
			// osw.flush();
			// osw.close();

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
			bw.write("tetawdasdsassasasasaa\nsadasassasadas");

			bw.flush();
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * nio写入操作
	 * 
	 * @param path
	 */
	public void testNioWrite(String path) {
		try {
			FileChannel fileChannel = new FileOutputStream(path).getChannel();
			ByteBuffer bb = ByteBuffer.allocate(1024);
			bb.put("23123123123\r\n13123".getBytes());
			bb.flip();
			fileChannel.write(bb);
			fileChannel.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * nio读写复制
	 * 
	 * @param path
	 */
	public void testNioRead(String path) {
		try {
			FileChannel fc = new FileInputStream(path).getChannel();
			ByteBuffer bb = ByteBuffer.allocate(1024);
			fc.read(bb);

			FileChannel fileChannel = new FileOutputStream("/Users/moetakara/Downloads/test/TEST6").getChannel();
			bb.flip();
			fileChannel.write(bb);
			fileChannel.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * files的操作
	 * FileSystems
	 * Paths
	 * @param path
	 */
	public void testFiles(String path){
		try {
			//file -> path, URl->path   
			Path p = FileSystems.getDefault().getPath(path);
			
			BufferedReader br = Files.newBufferedReader(Paths.get(path));
			String s;
			while ((s = br.readLine()) != null){
				System.out.println(s);
			}
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

}
