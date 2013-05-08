import p5_remote.*;

Remote remote;
int font_size = 1;

public void setup() {
  size(320, 240);
  
  remote = new Remote(this, "test_name1", "127.0.0.1", 12345);
}

public void draw() {
  // draw something
  background(0, 0, 64);

  fill(255, 255, 255);

  textSize(32);
  text("client1", 5, 30);

  textSize(font_size);
  text("frameCount=" + frameCount, 5, font_size * 2);
  text("frameRate=" + frameRate, 5, font_size * 3);

  font_size ++;
  if (font_size == 100) font_size = 1;

  // publish to server
  remote.publish();
}

