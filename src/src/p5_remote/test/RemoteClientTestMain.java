package p5_remote.test;

import p5_remote.Remote;
import processing.core.PApplet;
import processing.core.PVector;

public class RemoteClientTestMain extends PApplet {
	private static final long serialVersionUID = 4258434090200713048L;

	Remote remote;
	SampleStopWatch stop_watch;

	public void setup() {
		size(640, 480);
		remote = new Remote(this, "test_name", "127.0.0.1", 12345);
		remote.setPublishScale(0.5f);
		
		stop_watch = new SampleStopWatch();
		stop_watch.center.x = width / 2;
		stop_watch.center.y = height / 2;
		stop_watch.radius = height / 2 - 30;
	}

	public void draw() {
		background(0, 0, 64);

		stop_watch.draw();

		stroke(255, 255, 255);
		strokeWeight(2);

		text("frameCount=" + frameCount, 10, 16);
		text("frameRate=" + frameRate, 10, 32);
		text("publishFPS=" + remote.getPublishFps(), 10, 48);
		text("publishBPS=" + remote.getPublishBpsStr(), 10, 64);
		text("lastPublishStatus=" + remote.getLastPublishStatus(), 10, 80);

		remote.publish();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "p5_remote.test.RemoteClientTestMain" });
	}

	class SampleStopWatch {
		public PVector center = new PVector();
		public int radius = 0;

		void draw_clock_line(float s, float e, float angle) {
			float vx = sin(angle * 2 * PI);
			float vy = -cos(angle * 2 * PI);
			float sp_x = center.x + s * radius * vx;
			float sp_y = center.y + s * radius * vy;
			float ep_x = center.x + e * radius * vx;
			float ep_y = center.y + e * radius * vy;
			line(sp_x, sp_y, ep_x, ep_y);
		}

		void draw() {
			stroke(255);
			noFill();

			strokeWeight(10);
			ellipse(center.x, center.y, radius * 2, radius * 2);
			strokeWeight(5);
			for (int i = 0; i < 12; ++i) {
				draw_clock_line(0.9f, 1.0f, i / 12.0f);
			}
			
			long t = System.currentTimeMillis();
			int m = (int)(t / 1000 / 60) % 60;
			int s = (int)(t / 1000)  % 60;
			int ss = (int)(t % 1000);

			float angle_m = m / 60.0f + s / 60.0f / 60.0f;
			strokeWeight(15);
			draw_clock_line(0.0f, 0.5f, angle_m);

			float angle_s = s / 60.0f + ss / 60.0f / 1000.0f;
			strokeWeight(10);
			draw_clock_line(0.0f, 0.7f, angle_s);
			
			strokeWeight(5);
			float angle_ss = ss / 1000.0f;
			draw_clock_line(0.0f, 0.80f, angle_ss);
		}
	}
}
