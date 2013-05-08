import p5_remote.*;

Remote remote;

public void setup() {
  size(320, 240);
  
  remote = new Remote(this, "test_name2", "127.0.0.1", 12345);
}

public void draw() {
  // draw something
  if (frameCount % 100 == 0)   background(random(255), random(255), random(255));

  int x = (int)random(width);
  int y = (int)random(height);
  int r = (int)random(30);

  noStroke();
  fill(random(255), random(255), random(255));
  ellipse(x, y, r, r);

  fill(255, 255, 255);
  textSize(32);
  text("client2", 5, 30);

  // publish to server
  remote.publish();
}

