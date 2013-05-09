package p5_remote.utils;

public class BPSCounter {
	String name = "BPSCounter";

	float bps = 0.0f;
	int count = 0;
	int total_bytes = 0;
	int check_count = 100;
	long start_time = 0L;

	public BPSCounter() {
	}

	public BPSCounter(String name) {
		this.name = name;
	}

	public int getCheckCount() {
		return check_count;
	}

	public void setCheckCount(int val) {
		this.check_count = val;
	}

	public void clear() {
		this.bps = 0.0f;
		this.count = 0;
		this.total_bytes = 0;
		this.start_time = 0L;
	}

	public void check(int size) {
		if (start_time == 0L)
			start_time = System.currentTimeMillis();

		total_bytes += size;
		count ++;
		
		if (count == check_count) {
			long diff = System.currentTimeMillis() - start_time;

			float t = diff / 1000.0f;
			bps = total_bytes * 8 / t; // converty byte -> bits
			
			start_time = System.currentTimeMillis();
			count = 0;
			total_bytes = 0;
		}
	}

	public float getBPS() {
		return bps;
	}

	public String getBpsStr() {
		return convertBPS2Str(getBPS());
	}

	public static String convertBPS2Str(float bps) {
		int l = (int)Math.log10(bps);
		
		String result = String.format("%.2fbps", bps);
		if (3 <= l && l < 6) {
			result = String.format("%.2fKbps", bps/1000.0f);
		}
		else if (6 <= l) {
			result = String.format("%.2fMbps", bps/1000.0f/1000.0f);
		}
		return result;
	}
}
