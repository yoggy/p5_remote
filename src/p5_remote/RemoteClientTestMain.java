package p5_remote;

import processing.core.PApplet;

public class RemoteClientTestMain extends PApplet {
	private static final long serialVersionUID = 4258434090200713048L;

	Remote remote;

	public void setup() {
		size(320, 240);
		remote = new Remote(this, "test_name", "127.0.0.1", 12345);
	}

	public void draw() {
		background(0, 0, 64);
		
		stroke(255,255,255);
		strokeWeight(2);
		
		text("frameCount=" + frameCount, 30, 30);
		text("frameRate=" + frameRate, 30, 60);
		
		remote.publish();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "p5_remote.RemoteClientTestMain" });
	}
}
