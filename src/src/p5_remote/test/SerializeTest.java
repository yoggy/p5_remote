package p5_remote.test;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import p5_remote.utils.PImageUtils;
import processing.core.PImage;
import remote.Payload;

import com.google.protobuf.ByteString;

public class SerializeTest {

	final String name = "test_name_1234";
	final String filename = "src/remote/test/nike.jpg";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFileUtils() throws IOException {
		int size = FileUtils.size(filename);
		byte[] jpeg = FileUtils.read(filename);

		assert (jpeg != null);
		assert (jpeg.length == size);
	}

	@Test
	public void testSerialize() throws IOException {
		byte[] src_jpeg = FileUtils.read(filename);

		// build payload
		Payload.Data.Builder builder = Payload.Data.newBuilder();
		builder.setName(name);
		builder.setJpeg(ByteString.copyFrom(src_jpeg));
		Payload.Data src = builder.build();

		// serialize
		byte[] buf = src.toByteArray();

		// deserialize
		Payload.Data dst = Payload.Data.parseFrom(buf);

		// check payload
		assert (name.equals(dst.getName()));
		byte[] dst_jpeg = dst.getJpeg().toByteArray();

		for (int i = 0; i < src_jpeg.length; ++i) {
			assert (src_jpeg[i] == dst_jpeg[i]);
		}
	}

	@Test
	public void testPImage2Jpeg() throws IOException {
		byte[] src_jpeg = FileUtils.read(filename);

		PImage img = PImageUtils.toPImage(src_jpeg);
		PImage img2 = PImageUtils.toPImage(PImageUtils
				.toJpegByteArray(img, 100));

		for (int i = 0; i < img.pixels.length; ++i) {
			assert (img.pixels[i] == img2.pixels[i]);
		}
	}
}
