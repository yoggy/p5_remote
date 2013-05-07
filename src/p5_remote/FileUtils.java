package p5_remote;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtils {

	public static byte[] read(String filename) throws IOException {
		FileInputStream fis = new FileInputStream(filename);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		byte [] buf = new byte[4 * 1024];
		while(true) {
			int size = fis.read(buf);
			if (size <= 0) break;
			
			bos.write(buf, 0, size);
		}
		
		fis.close();
		
		byte [] result = bos.toByteArray();
		bos.close();
		
		return result;
	}

	public static int size(String filename) {
		File f = new File(filename);
		if (!f.exists()) return -1;
		
		return (int)f.length();
	}

}
