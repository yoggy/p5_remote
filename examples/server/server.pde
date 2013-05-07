import p5_remote.*;

RemoteServer server;

void setup() {
  size(320, 240);
  server = new RemoteServer(this, 12345);
  server.start();
}

void draw() {
  PImage img = server.getPImage("test_name");
  if (img != null) {
    image(img, 0, 0);
    g.removeCache(img);
  }
}

