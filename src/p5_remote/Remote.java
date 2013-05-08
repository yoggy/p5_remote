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

class RemoteClientThread extends Thread{
	String host;
	int port;

	Socket socket;
	InputStream is;
	OutputStream os;

	boolean break_flag = false;
	boolean last_publish_status = false;

	int retry_interval_time = 1000;
	
	byte [] payload = null;
	boolean update_payload = false;
	
	FPSCounter fps_counter = new FPSCounter("publish_fps");
	
	public RemoteClientThread(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public boolean getLastPublishStatus() {
		return last_publish_status;
	}

	public float getPublishFps() {
		return fps_counter.getFPS();
	}

	public void run() {
		while(!break_flag) {
			// check payload
			if (payload == null) {
				last_publish_status = false;
				fps_counter.clear();
				try {
					sleep(retry_interval_time);
				} catch (InterruptedException e) {
				}
				continue;
			}
			
			// check socket status
			if (!openSocket()) {
				last_publish_status = false;
				fps_counter.clear();
				try {
					sleep(retry_interval_time);
				} catch (InterruptedException e) {
				}
				continue;
			}
			
			// payload update check
			if (update_payload == false) {
				try {
					sleep(1);
				} catch (InterruptedException e) {
				}
				continue;
			}
			
			// send packet
			try {
				// build packet data
				ByteBuffer bb = ByteBuffer.allocate(4 + 4 + payload.length).order(ByteOrder.LITTLE_ENDIAN);
				bb.put("REMT".getBytes(), 0, 4);
				bb.putInt((int)payload.length);
				bb.put(payload, 0, payload.length);

				// write packet
				os.write(bb.array(), 0, bb.capacity());
				os.flush();
				
				last_publish_status = true;
				update_payload = false;
				fps_counter.check();
			}
			catch(Exception e) {
				closeSocket();
				e.printStackTrace();
				last_publish_status = false;
				fps_counter.clear();
			}
			System.gc();
		}
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
		update_payload = true;
	}

	private boolean isOpenSocket() {
		if (socket == null || os == null || is == null) return false;
		return true;
	}

	private boolean openSocket() {
		if (isOpenSocket()) return true;

		try {
			socket = new Socket(host, port);
			is = socket.getInputStream();
			os = socket.getOutputStream();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
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

public class Remote {
	PApplet papplet;
	String name = "remote.publisher.default_name";
	int jpeg_quality = 90;
	RemoteClientThread thread;	
	
	public Remote(PApplet papplet, String name, String host, int port) {
		this.papplet = papplet;
		this.name = name;
		this.thread = new RemoteClientThread(host, port);
		this.thread.start();
	}

	public int getJpegQuality() {
		return jpeg_quality;
	}

	public void setJpegQuality(int jpeg_quality) {
		this.jpeg_quality = jpeg_quality;
	}

	public boolean getLastPublishStatus() {
		return thread.getLastPublishStatus();
	}
	
	public float getPublishFps() {
		return thread.getPublishFps();
	}
	
	public void publish() {
		// create jpeg byte array
		PImage img = papplet.get();
		img.updatePixels();
		byte [] jpeg_data = PImageUtils.toJpegByteArray(img, jpeg_quality);
		
		// serialize to Payload
		Payload.Data.Builder builder = Payload.Data.newBuilder();
		builder.setName(name);
		builder.setJpeg(ByteString.copyFrom(jpeg_data));
		byte [] payload = builder.build().toByteArray();

		// set payload
		thread.setPayload(payload);		
	}
}
