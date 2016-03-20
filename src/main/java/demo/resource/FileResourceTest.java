package demo.resource;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.FileCopyUtils;

public class FileResourceTest {

	public static void main(String[] args) {
		String filename = "Users/wangmin/Documents/workspace/demo/redisdemo/src/main/resources/resources/test1.txt";
		
		Resource re = new FileSystemResource(filename);
		System.out.println(re.getFilename());
		
		try {
			EncodedResource er = new EncodedResource(re, "UTF-8");
			String content = FileCopyUtils.copyToString(er.getReader());
			System.out.println(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
