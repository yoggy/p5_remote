package p5_remote;

public class FPSCounter {
	String name = "FPSCounter";

	float fps = 0.0f;
	int count = 0;
	int check_count = 100;
	long start_time = 0L;

	public FPSCounter() {
	}

	public FPSCounter(String name) {
		this.name = name;
	}

	public int getCheckCount() {
		return check_count;
	}

	public void setCheckCount(int val) {
		this.check_count = val;
	}

	public void clear() {
		this.fps = 0.0f;
		this.count = 0;
		this.start_time = 0L;
	}

	public void check() {
		if (start_time == 0L)
			start_time = System.currentTimeMillis();

		count++;
		
		if (count == check_count) {
			long diff = System.currentTimeMillis() - start_time;

			float t = diff / 1000.0f / (float) check_count;
			fps = 1.0f / t;

			start_time = System.currentTimeMillis();
			count = 0;
		}
	}

	public float getFPS() {
		return fps;
	}
}
