package p5_remote;

import processing.core.PApplet;
import processing.core.PImage;

public class RemoteServerTestMain extends PApplet {
	private static final long serialVersionUID = 4258434090200713047L;

	RemoteServer server;

	public void setup() {
		size(320, 240);
		server = new RemoteServer(this, 12345);
		server.start();
	}

	public void draw() {
		PImage img = server.getPImage("test_name");
		if (img != null) {
			image(img, 0, 0, width, height);
			
			// issue 1391 : https://github.com/processing/processing/issues/1391
			g.removeCache(img); 
		}
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "p5_remote.RemoteServerTestMain" });
	}
}
