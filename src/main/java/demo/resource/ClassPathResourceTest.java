package demo.resource;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.FileCopyUtils;

public class ClassPathResourceTest {

	public static void main(String[] args) {
		
		Resource re = new ClassPathResource("resources/test1.txt");
		System.out.println(re.getFilename());
		
		EncodedResource er = new EncodedResource(re, "UTF-8");
		try {
			String s = FileCopyUtils.copyToString(er.getReader());
			System.out.println(s);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
