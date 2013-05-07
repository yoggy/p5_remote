import p5_remote.*;

Remote remote;

public void setup() {
  size(320, 240);
  remote = new Remote(this, "test_name", "127.0.0.1", 12345);
}

public void draw() {
  background(0, 0, 64);

  stroke(255, 255, 255);
  strokeWeight(2);

  text("frameCount=" + frameCount, 30, 30);
  text("frameRate=" + frameRate, 30, 60);

  remote.publish();
}

