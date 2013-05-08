import p5_remote.*;

Remote remote;

public void setup() {
  size(320, 240);
  
  remote = new Remote(this, "test_name3", "127.0.0.1", 12345);
}

public void draw() {
  // draw something
  if (frameCount % 100 == 0)   background(random(255), random(255), random(255));

  int x0 = (int)random(width);
  int y0 = (int)random(height);
  
  int x1 = (int)random(width);
  int y1 = (int)random(height);

  stroke(random(255), random(255), random(255));
  strokeWeight(random(10));
  line(x0, y0, x1, y1);

  fill(255, 255, 255);
  textSize(32);
  text("client3", 5, 30);

  // publish to server
  remote.publish();
}

