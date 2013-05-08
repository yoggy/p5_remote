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

