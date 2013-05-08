import p5_remote.*;

Remote remote;
SampleStopWatch stop_watch;

public void setup() {
  size(320, 240);
  
  remote = new Remote(this, "test_name4", "127.0.0.1", 12345);

  stop_watch = new SampleStopWatch();
  stop_watch.center.x = width / 2;
  stop_watch.center.y = height / 2;
  stop_watch.radius = height / 2 - 30;
}

public void draw() {
  // draw something
  background(128, 96, 64);

  stop_watch.draw();

  stroke(255, 255, 255);

  textSize(32);
  text("client4", 5, 30);

  // publish to server
  remote.publish();
}

