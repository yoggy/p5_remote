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

import com.google.protobuf.InvalidProtocolBufferException;

import processing.core.PApplet;
import processing.core.PImage;
import remote.Payload;

class RemoteClientThread extends Thread {
	RemoteServer parent;
	Socket socket;
	InputStream is;
	OutputStream os;

	public RemoteClientThread(RemoteServer parent, Socket socket) {
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
				decode(payload, read_size);

			} catch (Exception e) {
				break;
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
			e.printStackTrace();
			return false;
		}

		// decode to PImage
		String name = pd.getName();
		PImage pimage = PImageUtils.toPImage(pd.getJpeg().toByteArray());
		parent.setPImage(name, pimage);

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
				RemoteClientThread t = new RemoteClientThread(parent, client);
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

	public RemoteServer(PApplet papplet, int listen_port) {
		this.papplet = papplet;
		this.listen_port = listen_port;
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
