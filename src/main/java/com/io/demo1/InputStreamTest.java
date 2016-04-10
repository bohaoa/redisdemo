package com.io.demo1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;

public class InputStreamTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new InputStreamTest().test1();
	}

	/**
	 * 字符的读/写
	 */
	public void test1() {
		try {
			FileInputStream finput = new FileInputStream("/Users/wangmin/Documents/maven-3.2.3/13.txt");
			InputStreamReader in = new InputStreamReader(finput);
			BufferedReader br = new BufferedReader(in);
			 
			//System.out.println(br.readLine());
			String s;
			while ((s = br.readLine()) != null) {
				System.out.println(s);
			}
			in.close();
			br.close();
			
			OutputStreamWriter ow = new OutputStreamWriter(
					new FileOutputStream("/Users/wangmin/Documents/maven-3.2.3/12.txt"));
			ow.write("asdasaasdassa萨达萨说 ");
			ow.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
