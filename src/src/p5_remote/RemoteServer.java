package p5_remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import com.google.protobuf.InvalidProtocolBufferException;

import p5_remote.utils.BPSCounter;
import p5_remote.utils.FPSCounter;
import p5_remote.utils.PImageUtils;
import processing.core.PApplet;
import processing.core.PImage;
import remote.Payload;

class RemoteChildThread extends Thread {
	RemoteServer parent;
	Socket socket;
	InputStream is;
	OutputStream os;
	
	FPSCounter fps_counter = new FPSCounter("update_fps");
	BPSCounter bps_counter = new BPSCounter("receive_data_bps");
	
	public RemoteChildThread(RemoteServer parent, Socket socket) {
		this.parent = parent;
		this.socket = socket;
		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		byte[] header_buf = new byte[4];
		byte[] payload_size_buf = new byte[4];
		int payload_size;
		byte[] payload;

		while (true) {
			try {
				// read header (4bytes)
				is.read(header_buf);
				if (header_buf[0] != 'R' || header_buf[1] != 'E'
						|| header_buf[2] != 'M' || header_buf[3] != 'T') {
					break;
				}

				// read payload size (4bytes)
				is.read(payload_size_buf);
				ByteBuffer bb = ByteBuffer.wrap(payload_size_buf).order(
						ByteOrder.LITTLE_ENDIAN);
				payload_size = bb.getInt();

				// read payload
				payload = new byte[payload_size];
				int read_size = 0;
				while (true) {
					int s = is.read(payload, read_size, payload.length
							- read_size);
					if (s <= 0)
						break;
					read_size += s;
					if (read_size >= payload.length)
						break;
				}

				// decode
				boolean rv = decode(payload, read_size);
				if (rv == false) break; // close connection if failed decoding
				
			} catch (Exception e) {
				break;
			}
			
			try {
				sleep(parent.getReadIntervalTime());
			} catch (InterruptedException e) {
			}
		}

		try {
			is.close();
			os.close();
			socket.close();
		} catch (Exception e) {
		}
	}

	private boolean decode(byte[] payload, int size) {
		// deserialize
		Payload.Data pd;
		try {
			pd = Payload.Data.parseFrom(payload);
		} catch (InvalidProtocolBufferException e) {
			System.err.println("InvalidProtocolBufferException : payload.length=" + payload.length);
			e.printStackTrace();
			return false;
		}

		// decode to PImage
		String name = pd.getName();
		PImage pimage = PImageUtils.toPImage(pd.getJpeg().toByteArray());
		parent.setPImage(name, pimage);

		// set update time
		parent.setLastUpdateTime(name, System.currentTimeMillis());
		
		// for debug...
		fps_counter.check();
		parent.setFps(name, fps_counter.getFPS());
		bps_counter.check(payload.length);
		parent.setBps(name, bps_counter.getBPS());
		
		return true;
	}
}

class RemoteServerThread extends Thread {
	RemoteServer parent;
	ServerSocket server_socket;
	boolean break_flag = false;

	public RemoteServerThread(RemoteServer parent, ServerSocket server_socket) {
		this.parent = parent;
		this.server_socket = server_socket;
	}

	@Override
	public void run() {
		while (!break_flag) {
			try {
				Socket client = server_socket.accept();
				RemoteChildThread t = new RemoteChildThread(parent, client);
				t.start();
			} catch (IOException e) {
				break_flag = true;
			}
		}
	}
}

public class RemoteServer {
	PApplet papplet;
	int listen_port;

	RemoteServerThread thread;
	
	HashMap<String, PImage> pimage_map = new HashMap<String, PImage>();
	HashMap<String, Float>  fps_map = new HashMap<String, Float>();
	HashMap<String, Float>  bps_map = new HashMap<String, Float>();
	HashMap<String, Long>   last_update_time_map = new HashMap<String, Long>();

	int timeout = 1000;
	int read_interval_time = 10;
	
	public RemoteServer(PApplet papplet, int listen_port) {
		this.papplet = papplet;
		this.listen_port = listen_port;
	}

	public void setTimeout(int ms) {
		this.timeout = ms;
	}

	public void setReadIntervalTime(int ms) {
		this.read_interval_time = ms;
	}
	
	public int getReadIntervalTime() {
		return this.read_interval_time;
	}
	
	public boolean isActive(String name) {
		long t = getLastUpdateTime(name);
		if (t == 0) return false;
		
		long diff = System.currentTimeMillis() - t;
		if (diff > timeout) return false;
		
		return true;
	}

	protected void setLastUpdateTime(String name, Long time) {
		last_update_time_map.put(name, time);
	}
	
	public long getLastUpdateTime(String name) {
		if (!last_update_time_map.containsKey(name)) return 0L;
		return last_update_time_map.get(name);
	}
	
	protected void setFps(String name, float fps) {
		fps_map.put(name, fps);
	}

	public float getFps(String name) {
		if (!fps_map.containsKey(name)) return 0.0f;
		if (!isActive(name)) return 0.0f;
		return fps_map.get(name);
	}

	public float getAverageFps() {
		float total_fps = 0;
		String names [] = getNames();
		if (names == null || names.length == 0) return 0.0f;
		
		for (int i = 0; i < names.length; ++i) {
			total_fps += getFps(names[i]);
		}
		return total_fps / names.length;
	}

	protected void setBps(String name, float bps) {
		bps_map.put(name, bps);
	}

	public float getBps(String name) {
		if (!bps_map.containsKey(name)) return 0.0f;
		if (!isActive(name)) return 0.0f;
		return bps_map.get(name);
	}

	public String getBpsStr(String name) {
		return BPSCounter.convertBPS2Str(getBps(name));
	}

	public float getTotalBps() {
		float total_bps = 0;
		String names [] = getNames();
		
		for (int i = 0; i < names.length; ++i) {
			total_bps += getBps(names[i]);
		}
		return total_bps;
	}
	
	public String getTotalBpsStr() {
		return BPSCounter.convertBPS2Str(getTotalBps());
	}

	public String [] getActiveConnectionNames() {
		String names [] = getNames();
		Vector<String> active_names = new Vector<String>();
		
		for (int i = 0; i < names.length; ++i) {
			if (isActive(names[i])) {
				active_names.add(names[i]);
			}
		}
		return active_names.toArray(new String[active_names.size()]);
	}
	
	public int getActiveConnectionNum() {
		return getActiveConnectionNames().length;
	}

	public int getLietenPort() {
		return listen_port;
	}

	public boolean start() {
		ServerSocket s;
		try {
			s = new ServerSocket(listen_port, 100);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		thread = new RemoteServerThread(this, s);
		thread.start();

		return true;
	}

	public String[] getNames() {
		Set<String> keys = pimage_map.keySet();
		if (keys.size() == 0)
			return new String[0];

		return keys.toArray(new String[keys.size()]);
	}

	public PImage getPImage(String name) {
		if (!pimage_map.containsKey(name))
			return null;

		return pimage_map.get(name);
	}

	public int getPimageSize() {
		return pimage_map.size();
	}

	protected void setPImage(String name, PImage pimage) {
		if (pimage == null)
			return;
		pimage_map.put(name, pimage);
	}

}
