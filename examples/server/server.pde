import p5_remote.*;

RemoteServer server;
PImage img;

void setup() {
  size(640, 480);
  server = new RemoteServer(this, 12345);
  server.start();
}

void draw() {
  background(64, 64, 64);
  
  img = server.getPImage("test_name1");
  if (img != null) {
    image(img, 0, 0, width / 2, height / 2);
    g.removeCache(img); // issue 1391 : https://github.com/processing/processing/issues/1391
  }
  
  img = server.getPImage("test_name2");
  if (img != null) {
    image(img, width / 2, 0, width / 2, height / 2);
    g.removeCache(img);
  }

  img = server.getPImage("test_name3");
  if (img != null) {
    image(img, 0, height / 2, width / 2, height / 2);
    g.removeCache(img);
  }

  img = server.getPImage("test_name4");
  if (img != null) {
    image(img, width / 2, height / 2, width / 2, height / 2);
    g.removeCache(img);
  }
}

