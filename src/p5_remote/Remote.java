package p5_remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import processing.core.PApplet;
import processing.core.PImage;
import remote.Payload;

import com.google.protobuf.ByteString;

public class Remote {
	PApplet papplet;
	String host;
	int port;
	String name = "remote.publisher.default_name";
	int jpeg_quality = 90;
	byte [] jpeg_data;

	Socket socket;
	InputStream is;
	OutputStream os;
	
	public Remote(PApplet papplet, String name, String host, int port) {
		this.papplet = papplet;
		this.name = name;
		this.host = host;
		this.port = port;
	}

	public int getJpegQuality() {
		return jpeg_quality;
	}

	public void setJpegQuality(int jpeg_quality) {
		this.jpeg_quality = jpeg_quality;
	}

	public void publish() {
		// create jpeg byte array
		PImage img = papplet.get();
		img.updatePixels();
		jpeg_data = PImageUtils.toJpegByteArray(img, jpeg_quality);
		
		// serialize to Payload
		Payload.Data.Builder builder = Payload.Data.newBuilder();
		builder.setName(name);
		builder.setJpeg(ByteString.copyFrom(jpeg_data));
		byte [] payload = builder.build().toByteArray();

		// create packet data
		ByteBuffer bb = ByteBuffer.allocate(4 + 4 + payload.length).order(ByteOrder.LITTLE_ENDIAN);
		bb.put("REMT".getBytes(), 0, 4);
		bb.putInt((int)payload.length);
		bb.put(payload, 0, payload.length);

		// send packet
		if (!isOpenSocket()) openSocket();
		try {
			// write header
			os.write(bb.array(), 0, bb.capacity());
			os.flush();
		}
		catch(Exception e) {
			closeSocket();
			e.printStackTrace();
		}
	}

	private boolean isOpenSocket() {
		if (socket == null || os == null || is == null) return false;
		return true;
	}

	private void openSocket() {
		try {
			socket = new Socket(host, port);
			is = socket.getInputStream();
			os = socket.getOutputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void closeSocket() {
		try {
			is.close();
			is = null;
		}
		catch(Exception e) {
		}
		
		try {
			os.close();
			os = null;
		}
		catch(Exception e) {
		}

		try {
			socket.close();
			socket = null;
		}
		catch(Exception e) {
		}
	}
}
